package com.montero.app.controller;

import com.montero.app.model.Reserva;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Viaje;
import com.montero.app.model.Usuario;
import com.montero.app.model.Pago;
import com.montero.app.repository.ViajeRepository;
import com.montero.app.repository.UsuarioRepository;
import com.montero.app.repository.ReservaRepository;
import com.montero.app.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador para el panel de administración.
 * Maneja todas las rutas bajo /admin/
 * Solo accesible por usuarios con rol ADMIN
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    /**
     * Dashboard principal del administrador
     * Muestra estadísticas generales del sistema
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Viajes programados para hoy
        LocalDate hoy = LocalDate.now();
        long viajesHoy = viajeRepository.countByFechaSalida(hoy);

        // Total de usuarios con rol USER (excluir ADMIN)
        long usuariosTotal = usuarioRepository.countByRol("USER");

        // Total de reservas
        long reservasTotal = reservaRepository.count();

        // Total de pagos
        long pagosTotal = pagoRepository.count();

        // Añadir atributos al modelo
        model.addAttribute("viajesHoy", viajesHoy);
        model.addAttribute("totalUsuarios", usuariosTotal);
        model.addAttribute("reservasTotal", reservasTotal);
        model.addAttribute("pagosTotal", pagosTotal);

        return "admin/dashboard";
    }

    /**
     * Página de viajes y horarios (listado dinámico)
     */
    @GetMapping("/viajes")
    public String viajes(Model model) {
        List<Viaje> viajes = viajeRepository.findAll();
        java.util.Map<Long, Integer> vendidosPorViaje = new java.util.HashMap<>();

        for (Viaje v : viajes) {
            List<Reserva> reservas = reservaRepository.findByViajeId(v.getId());
            int totalAsientosVendidos = 0;
            for (Reserva r : reservas) {
                if (r.getEstado() != EstadoReserva.CANCELADO) {
                    totalAsientosVendidos += r.getListaNumerosAsientos().size();
                }
            }
            vendidosPorViaje.put(v.getId(), totalAsientosVendidos);
        }

        model.addAttribute("viajes", viajes);
        model.addAttribute("vendidosPorViaje", vendidosPorViaje);
        return "admin/viajes";
    }

    /**
     * Guardar o editar un viaje
     */
    @PostMapping("/viajes/guardar")
    public String guardarViaje(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino,
            @RequestParam("fechaSalida") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @RequestParam("horaSalida") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaSalida,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("asientosTotales") Integer asientosTotales,
            RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje;
            if (id != null) {
                viaje = viajeRepository.findById(id).orElse(new Viaje());
            } else {
                viaje = new Viaje();
            }
            viaje.setOrigen(origen);
            viaje.setDestino(destino);
            viaje.setFechaSalida(fechaSalida);
            viaje.setHoraSalida(horaSalida);
            viaje.setPrecio(precio);
            viaje.setAsientosTotales(asientosTotales);
            viajeRepository.save(viaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Viaje guardado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar el viaje: " + e.getMessage());
        }
        return "redirect:/admin/viajes";
    }

    /**
     * Cancelar un viaje
     */
    @PostMapping("/viajes/{id}/cancelar")
    public String cancelarViaje(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Viaje viaje = viajeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado"));
            viaje.setEstado("CANCELADO");
            viajeRepository.save(viaje);
            redirectAttributes.addFlashAttribute("mensajeExito", "Viaje cancelado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cancelar el viaje");
        }
        return "redirect:/admin/viajes";
    }

    /**
     * Eliminar un viaje
     */
    @GetMapping("/viajes/eliminar/{id}")
    public String eliminarViaje(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            viajeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Viaje eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el viaje");
        }
        return "redirect:/admin/viajes";
    }

    /**
     * Página de usuarios (listado dinámico de ROLE_USER)
     */
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        List<Usuario> listUsuarios = usuarioRepository.findByRol("USER");
        long totalUsuarios = listUsuarios.size();
        long bloqueados = listUsuarios.stream().filter(u -> Boolean.FALSE.equals(u.getActivo())).count();
        java.util.Map<Long, Integer> reservasPorUsuario = new java.util.HashMap<>();

        for (Usuario u : listUsuarios) {
            reservasPorUsuario.put(u.getId(), reservaRepository.findByUsuarioId(u.getId()).size());
        }

        model.addAttribute("usuarios", listUsuarios);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("bloqueados", bloqueados);
        model.addAttribute("reservasPorUsuario", reservasPorUsuario);
        return "admin/usuarios";
    }

    /**
     * Bloquear un usuario
     */
    @PostMapping("/usuarios/{id}/bloquear")
    public String bloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario bloqueado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al bloquear el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Desbloquear un usuario
     */
    @PostMapping("/usuarios/{id}/desbloquear")
    public String desbloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desbloqueado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al desbloquear el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Eliminar un usuario
     */
    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Página de reservas (listado dinámico con datos de usuario y viaje)
     */
    @GetMapping("/reservas")
    public String reservas(Model model) {
        List<Reserva> listReservas = reservaRepository.findAll();
        model.addAttribute("reservas", listReservas);
        model.addAttribute("total", listReservas.size());
        return "admin/reservas";
    }

    /**
     * Cancelar una reserva
     */
    @PostMapping("/reservas/{id}/cancelar")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            reserva.setEstado(EstadoReserva.CANCELADO);
            reservaRepository.save(reserva);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reserva cancelada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cancelar la reserva");
        }
        return "redirect:/admin/reservas";
    }

    /**
     * Página de pagos (listado dinámico, solo lectura para auditoría)
     */
    @GetMapping("/pagos")
    public String pagos(Model model) {
        List<Pago> listPagos = pagoRepository.findAll();
        LocalDate hoy = LocalDate.now();
        BigDecimal recaudadoHoy = BigDecimal.ZERO;

        for (Pago p : listPagos) {
            if (p.getFechaPago() != null && p.getFechaPago().toLocalDate().equals(hoy)) {
                if (p.getReserva() != null) {
                    recaudadoHoy = recaudadoHoy.add(p.getReserva().getPrecioTotal());
                }
            }
        }

        long pendientes = reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                .count();

        model.addAttribute("pagos", listPagos);
        model.addAttribute("recaudadoHoy", recaudadoHoy);
        model.addAttribute("pendientes", pendientes);
        return "admin/pagos";
    }
}