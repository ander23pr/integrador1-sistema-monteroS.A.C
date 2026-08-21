package com.montero.app.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.montero.app.model.EstadoReserva;
import com.montero.app.model.MetodoPago;
import com.montero.app.model.Notificacion;
import com.montero.app.model.Pago;
import com.montero.app.model.Reserva;
import com.montero.app.model.Usuario;
import com.montero.app.model.Viaje;
import com.montero.app.repository.PagoRepository;
import com.montero.app.repository.ReservaRepository;
import com.montero.app.repository.UsuarioRepository;
import com.montero.app.repository.ViajeRepository;
import com.montero.app.service.NotificacionService;

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

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("viajesHoy", viajeRepository.countByFechaSalida(hoy));
        model.addAttribute("totalUsuarios", usuarioRepository.countByRol("USER"));
        model.addAttribute("reservasTotal", reservaRepository.count());
        model.addAttribute("pagosTotal", pagoRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        LocalDate hoy = LocalDate.now();

        // --- Ingresos del mes actual ---
        List<Pago> pagosMes = pagoRepository.findAllWithReservaAndViajeByMes(hoy.getMonthValue(), hoy.getYear());
        BigDecimal ingresosMes = pagosMes.stream()
                .map(p -> p.getReserva().getPrecioTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- Pasajes vendidos en el mes ---
        List<Reserva> reservasPagadasMes = reservaRepository.findByEstadoAndMesWithViaje(
                EstadoReserva.PAGADO, hoy.getMonthValue(), hoy.getYear());
        int pasajesVendidosMes = reservasPagadasMes.stream()
                .mapToInt(r -> r.getListaNumerosAsientos().size())
                .sum();

        // --- Ocupación promedio ---
        int totalAsientosDisponibles = 0;
        int totalAsientosVendidos = 0;
        java.util.HashSet<Long> viajeIds = new java.util.HashSet<>();
        for (Reserva r : reservasPagadasMes) {
            if (viajeIds.add(r.getViaje().getId())) {
                totalAsientosDisponibles += r.getViaje().getAsientosTotales();
            }
            totalAsientosVendidos += r.getListaNumerosAsientos().size();
        }
        int ocupacionPromedio = totalAsientosDisponibles > 0
                ? totalAsientosVendidos * 100 / totalAsientosDisponibles
                : 0;

        // --- Tasa de cancelación ---
        long totalReservas = reservaRepository.count();
        long canceladas = reservaRepository.countByEstado(EstadoReserva.CANCELADO);
        int tasaCancelacion = totalReservas > 0 ? (int) (canceladas * 100 / totalReservas) : 0;

        // --- Ingresos mensuales (últimos 7 meses) ---
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        List<Map<String, Object>> datosMensuales = new ArrayList<>();
        List<BigDecimal> ingresosPorMes = new ArrayList<>();
        BigDecimal maxIngreso = BigDecimal.ZERO;

        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusMonths(i);
            List<Pago> pagos = pagoRepository.findAllWithReservaAndViajeByMes(
                    fecha.getMonthValue(), fecha.getYear());
            BigDecimal ingreso = pagos.stream()
                    .map(p -> p.getReserva().getPrecioTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            ingresosPorMes.add(ingreso);
            if (ingreso.compareTo(maxIngreso) > 0) maxIngreso = ingreso;
        }

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.minusMonths(6 - i);
            BigDecimal ingreso = ingresosPorMes.get(i);
            int alturaPct = maxIngreso.compareTo(BigDecimal.ZERO) > 0
                    ? ingreso.multiply(BigDecimal.valueOf(100)).divide(maxIngreso, 0, RoundingMode.HALF_UP).intValue()
                    : 0;
            Map<String, Object> m = new HashMap<>();
            m.put("mes", meses[fecha.getMonthValue() - 1]);
            m.put("alturaPct", Math.max(alturaPct, 1));
            datosMensuales.add(m);
        }

        // --- Distribución de métodos de pago ---
        List<Object[]> metodosData = pagoRepository.countGroupedByMetodoPago();
        long totalPagos = metodosData.stream().mapToLong(row -> (Long) row[1]).sum();

        Map<String, String> colores = Map.of(
                "YAPE", "#15803D",
                "TARJETA", "#1D4ED8",
                "PAGO_EFECTIVO", "#B45309",
                "PAGO_QR", "#6B21A8",
                "TRANSFERENCIA", "#0B1120"
        );
        Map<String, String> nombres = Map.of(
                "YAPE", "Yape",
                "TARJETA", "Tarjeta",
                "PAGO_EFECTIVO", "Efectivo",
                "PAGO_QR", "Pago QR",
                "TRANSFERENCIA", "Transferencia"
        );

        List<Map<String, Object>> metodosPago = new ArrayList<>();
        for (Object[] row : metodosData) {
            String metodo = ((MetodoPago) row[0]).name();
            long cantidad = (Long) row[1];
            int porcentaje = totalPagos > 0 ? (int) (cantidad * 100 / totalPagos) : 0;
            Map<String, Object> m = new HashMap<>();
            m.put("metodo", nombres.getOrDefault(metodo, metodo));
            m.put("porcentaje", porcentaje);
            m.put("cantidad", cantidad);
            m.put("color", colores.getOrDefault(metodo, "#71717A"));
            metodosPago.add(m);
        }

        // --- Top 5 rutas con mayor recaudación ---
        List<Pago> todosPagos = pagoRepository.findAllWithReservaAndViaje();
        Map<String, BigDecimal> recaudacionPorRuta = new LinkedHashMap<>();

        for (Pago p : todosPagos) {
            String ruta = p.getReserva().getViaje().getOrigen() + " \u2192 " + p.getReserva().getViaje().getDestino();
            recaudacionPorRuta.merge(ruta, p.getReserva().getPrecioTotal(), BigDecimal::add);
        }

        BigDecimal totalRecaudacion = recaudacionPorRuta.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> topRutas = recaudacionPorRuta.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ruta", e.getKey());
                    m.put("monto", e.getValue());
                    m.put("porcentaje", totalRecaudacion.compareTo(BigDecimal.ZERO) > 0
                            ? e.getValue().multiply(BigDecimal.valueOf(100)).divide(totalRecaudacion, 0, RoundingMode.HALF_UP).intValue()
                            : 0);
                    return m;
                })
                .collect(Collectors.toList());

        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("pasajesVendidosMes", pasajesVendidosMes);
        model.addAttribute("ocupacionPromedio", ocupacionPromedio);
        model.addAttribute("tasaCancelacion", tasaCancelacion);
        model.addAttribute("datosMensuales", datosMensuales);
        model.addAttribute("metodosPago", metodosPago);
        model.addAttribute("topRutas", topRutas);
        return "admin/reportes";
    }

    @GetMapping("/viajes")
    public String viajes(Model model,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Viaje> viajePage = viajeRepository.findAllOrdered(pageable);

        Map<Long, Integer> vendidosPorViaje = new HashMap<>();
        for (Viaje v : viajePage.getContent()) {
            List<Reserva> reservas = reservaRepository.findByViajeId(v.getId());
            int totalAsientosVendidos = 0;
            for (Reserva r : reservas) {
                if (r.getEstado() != EstadoReserva.CANCELADO) {
                    totalAsientosVendidos += r.getListaNumerosAsientos().size();
                }
            }
            vendidosPorViaje.put(v.getId(), totalAsientosVendidos);
        }

        model.addAttribute("viajesPage", viajePage);
        model.addAttribute("vendidosPorViaje", vendidosPorViaje);
        return "admin/viajes";
    }

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

    @GetMapping("/viajes/eliminar/{id}")
    public String eliminarViaje(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        List<Reserva> reservas = reservaRepository.findByViajeId(id);
        if (!reservas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                "No es posible eliminar este viaje porque tiene " + reservas.size() +
                " reservas asociadas. Puede cancelarlo para evitar nuevas ventas y conservar el historial.");
            return "redirect:/admin/viajes";
        }
        try {
            viajeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Viaje eliminado correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar viaje id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el viaje");
        }
        return "redirect:/admin/viajes";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        List<Usuario> allUsuarios = usuarioRepository.findByRol("USER");
        long totalUsuarios = allUsuarios.size();
        long bloqueados = allUsuarios.stream().filter(u -> Boolean.FALSE.equals(u.getActivo())).count();

        Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> usuarioPage = usuarioRepository.findByRolOrdered("USER", pageable);

        Map<Long, Integer> reservasPorUsuario = new HashMap<>();
        for (Usuario u : usuarioPage.getContent()) {
            reservasPorUsuario.put(u.getId(), reservaRepository.findByUsuarioId(u.getId()).size());
        }

        model.addAttribute("usuariosPage", usuarioPage);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("bloqueados", bloqueados);
        model.addAttribute("reservasPorUsuario", reservasPorUsuario);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/bloquear")
    public String bloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            //El controlador busca al usuario
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                    //lo guarda en la base de datos con el atributo activo en false
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario bloqueado correctamente");
        } catch (Exception e) {
            log.error("Error al bloquear usuario id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al bloquear el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/desbloquear")
    public String desbloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desbloqueado correctamente");
        } catch (Exception e) {
            log.error("Error al desbloquear usuario id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al desbloquear el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        List<Reserva> reservas = reservaRepository.findByUsuarioId(id);
        if (!reservas.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                "No es posible eliminar este usuario porque tiene " + reservas.size() +
                " reservas en su historial. Puede bloquear la cuenta para deshabilitar el acceso.");
            return "redirect:/admin/usuarios";
        }
        try {
            usuarioRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Usuario eliminado correctamente");
        } catch (Exception e) {
            log.error("Error al eliminar usuario id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/reservas")
    public String reservas(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reserva> reservaPage = reservaRepository.findAllWithViaje(pageable);
        model.addAttribute("reservasPage", reservaPage);
        return "admin/reservas";
    }

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

    @Transactional(readOnly = true)
    @GetMapping("/pagos")
    public String pagos(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
        List<Pago> allPagos = pagoRepository.findAllWithReserva();
        LocalDate hoy = LocalDate.now();
        BigDecimal recaudadoHoy = BigDecimal.ZERO;

        for (Pago p : allPagos) {
            if (p.getFechaPago() != null && p.getFechaPago().toLocalDate().equals(hoy)) {
                recaudadoHoy = recaudadoHoy.add(p.getReserva().getPrecioTotal());
            }
        }

        long pendientes = reservaRepository.countByEstado(EstadoReserva.PENDIENTE);

        Pageable pageable = PageRequest.of(page, size);
        Page<Pago> pagoPage = pagoRepository.findAllWithReserva(pageable);

        model.addAttribute("pagosPage", pagoPage);
        model.addAttribute("recaudadoHoy", recaudadoHoy);
        model.addAttribute("pendientes", pendientes);
        return "admin/pagos";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(Model model) {
        Map<String, List<Notificacion>> agrupadas = notificacionService.obtenerNotificacionesAdminAgrupadas();
        model.addAttribute("notificacionesAgrupadas", agrupadas);
        return "admin/notificaciones";
    }

    @PostMapping("/notificaciones/marcar-todo-leido")
    public String marcarTodoLeido() {
        notificacionService.marcarTodasComoLeidasAdmin();
        return "redirect:/admin/notificaciones";
    }

    @PostMapping("/notificaciones/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        try {
            notificacionService.eliminarNotificacionAdmin(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}