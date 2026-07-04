package com.montero.app.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.montero.app.dto.PasajeroRequestDTO;
import com.montero.app.dto.ReservaRequestDTO;
import com.montero.app.model.Reserva;
import com.montero.app.model.Usuario;
import com.montero.app.model.Viaje;
import com.montero.app.service.PagoService;
import com.montero.app.service.ReservaService;
import com.montero.app.service.ViajeService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * “El ReservaController se encarga de administrar todo el flujo de reservas dentro del sistema.
 * Y actúa como intermediario entre las vistas desarrolladas con Thymeleaf, la lógica de negocio implementada en los Services,
 * y la base de datos.
 */
@Controller
@RequestMapping(path = {"/reservas", ""})
public class ReservaController {

    private final ViajeService viajeService;
    private final ReservaService reservaService;
    private final PagoService pagoService;

    private static final Logger log = LoggerFactory.getLogger(ReservaController.class);

    public ReservaController(ViajeService viajeService, ReservaService reservaService, PagoService pagoService) {
        this.viajeService = viajeService;
        this.reservaService = reservaService;
        this.pagoService = pagoService;
    }

    /**
     * Muestra el historial de viajes del usuario autenticado.
     */
    @GetMapping("/historial")
    public String mostrarHistorial(@RequestParam(value = "busqueda", required = false) String busqueda,
                                   @RequestParam(value = "filtro", required = false, defaultValue = "todos") String filtro,
                                   @RequestParam(value = "orden", required = false, defaultValue = "fecha_desc") String orden,
                                   Model model,
                                   HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        List<Reserva> reservas = reservaService.obtenerHistorialPorUsuario(usuarioSesion.getId(), busqueda, filtro, orden);
        model.addAttribute("reservas", reservas);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("filtro", filtro);
        model.addAttribute("orden", orden);
        return "historial_viajes";
    }

    /**
     * Este método se encarga de cargar la vista de selección de asientos.
     */
    @GetMapping("/viaje/{id}/asientos")
    public String mostrarSeleccionAsientos(@PathVariable("id") Long viajeId,
                                           @RequestParam(value = "origen", required = false) String origen,
                                           @RequestParam(value = "destino", required = false) String destino,
                                           @RequestParam(value = "fecha", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                           @RequestParam(value = "fechaRetorno", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRetorno,
                                           Model model,
                                           HttpSession session) {

        Viaje viaje = viajeService.obtenerViajePorId(viajeId);
        List<Integer> asientosOcupados = viajeService.obtenerAsientosOcupados(viajeId);

        model.addAttribute("viaje", viaje);
        model.addAttribute("asientosOcupados", asientosOcupados); 
        model.addAttribute("esRetorno", false);
        
        ReservaRequestDTO reservaDTO = new ReservaRequestDTO();
        reservaDTO.setViajeId(viajeId);
        reservaDTO.setOrigenSeleccionado(origen != null ? origen : viaje.getOrigen());
        reservaDTO.setDestinoSeleccionado(destino != null ? destino : viaje.getDestino());
        reservaDTO.setFechaSeleccionada(fecha != null ? fecha : viaje.getFechaSalida());

        if (fechaRetorno != null) {
            reservaDTO.setFechaRetorno(fechaRetorno);
            reservaDTO.setOrigenRetorno(destino != null ? destino : viaje.getDestino());
            reservaDTO.setDestinoRetorno(origen != null ? origen : viaje.getOrigen());
        }

        model.addAttribute("reservaDTO", reservaDTO);
        model.addAttribute("fechaSeleccionada", reservaDTO.getFechaSeleccionada());

        return "seleccion_asientos";
    }

    @GetMapping("/viaje/{id}/asientos-retorno")
    public String mostrarSeleccionAsientosRetorno(@PathVariable("id") Long viajeId,
                                                   @RequestParam(value = "origen", required = false) String origen,
                                                   @RequestParam(value = "destino", required = false) String destino,
                                                   @RequestParam(value = "fecha", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                                   Model model,
                                                   HttpSession session) {

        Viaje viaje = viajeService.obtenerViajePorId(viajeId);
        List<Integer> asientosOcupados = viajeService.obtenerAsientosOcupados(viajeId);

        model.addAttribute("viaje", viaje);
        model.addAttribute("asientosOcupados", asientosOcupados);
        model.addAttribute("esRetorno", true);

        ReservaRequestDTO reservaDTO = new ReservaRequestDTO();
        reservaDTO.setViajeId(viajeId);
        reservaDTO.setOrigenSeleccionado(origen != null ? origen : viaje.getOrigen());
        reservaDTO.setDestinoSeleccionado(destino != null ? destino : viaje.getDestino());
        reservaDTO.setFechaSeleccionada(fecha != null ? fecha : viaje.getFechaSalida());

        model.addAttribute("reservaDTO", reservaDTO);
        model.addAttribute("fechaSeleccionada", reservaDTO.getFechaSeleccionada());

        return "seleccion_asientos";
    }

    @GetMapping("/pasajero")
    public String mostrarFormularioPasajero(Model model, HttpSession session) {
        ReservaRequestDTO dto = (ReservaRequestDTO) session.getAttribute("reservaSession");
        if (dto == null) {
            return "redirect:/viajes";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        PasajeroRequestDTO pasajeroDTO = new PasajeroRequestDTO();
        pasajeroDTO.setNombresPasajero(dto.getNombresPasajero());
        pasajeroDTO.setApellidosPasajero(dto.getApellidosPasajero());
        pasajeroDTO.setDniPasajero(dto.getDniPasajero());
        pasajeroDTO.setEmailPasajero(dto.getEmailPasajero());
        pasajeroDTO.setTelefonoPasajero(dto.getTelefonoPasajero());
        model.addAttribute("pasajeroDTO", pasajeroDTO);
        model.addAttribute("usuarioLogueado", usuario);
        return "pasajero";
    }

    @PostMapping("/pasajero")
    public String guardarPasajero(@Valid @ModelAttribute("pasajeroDTO") PasajeroRequestDTO pasajeroDTO,
                                  BindingResult bindingResult,
                                  Model model,
                                  HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "pasajero";
        }
        ReservaRequestDTO dto = (ReservaRequestDTO) session.getAttribute("reservaSession");
        if (dto == null) {
            return "redirect:/viajes";
        }
        dto.setNombresPasajero(pasajeroDTO.getNombresPasajero());
        dto.setApellidosPasajero(pasajeroDTO.getApellidosPasajero());
        dto.setDniPasajero(pasajeroDTO.getDniPasajero());
        dto.setEmailPasajero(pasajeroDTO.getEmailPasajero());
        dto.setTelefonoPasajero(pasajeroDTO.getTelefonoPasajero());
        session.setAttribute("reservaSession", dto);
        return "redirect:/reservas/resumen";
    }

    /**
     * Almacena los datos del formulario en sesión sin crear la reserva aún.
     * La reserva se persiste solo al confirmar desde el resumen.
     */
    @PostMapping("/iniciar")
    public String iniciarReserva(@Valid @ModelAttribute("reservaDTO") ReservaRequestDTO reservaDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        ReservaRequestDTO sesionDTO = (ReservaRequestDTO) session.getAttribute("reservaSession");

        if (sesionDTO == null) {
            // Primer paso: selección de asientos de ida
            if (bindingResult.hasErrors()) {
                Viaje viaje = viajeService.obtenerViajePorId(reservaDTO.getViajeId());
                model.addAttribute("viaje", viaje);
                model.addAttribute("asientosOcupados", viajeService.obtenerAsientosOcupados(viaje.getId()));
                model.addAttribute("fechaSeleccionada",
                        reservaDTO.getFechaSeleccionada() != null ? reservaDTO.getFechaSeleccionada() : viaje.getFechaSalida());
                return "seleccion_asientos";
            }
            session.setAttribute("reservaSession", reservaDTO);

            if (reservaDTO.getFechaRetorno() != null) {
                return "redirect:/viajes/buscar?origen=" + reservaDTO.getDestinoSeleccionado()
                        + "&destino=" + reservaDTO.getOrigenSeleccionado()
                        + "&fecha=" + reservaDTO.getFechaRetorno()
                        + "&fechaRetorno=" + reservaDTO.getFechaRetorno()
                        + "&modo=retorno";
            }
            return "redirect:/reservas/pasajero";
        }

        // Segundo paso: selección de asientos de retorno
        if (reservaDTO.getNumerosAsientos() == null || reservaDTO.getNumerosAsientos().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un asiento para el retorno.");
            return "redirect:/reservas/viaje/" + reservaDTO.getViajeId() + "/asientos-retorno";
        }
        if (reservaDTO.getViajeId() == null) {
            redirectAttributes.addFlashAttribute("error", "El viaje es obligatorio.");
            return "redirect:/viajes";
        }

        sesionDTO.setViajeRetornoId(reservaDTO.getViajeId());
        sesionDTO.setNumerosAsientosRetorno(reservaDTO.getNumerosAsientos());
        sesionDTO.setOrigenRetorno(reservaDTO.getOrigenSeleccionado());
        sesionDTO.setDestinoRetorno(reservaDTO.getDestinoSeleccionado());
        sesionDTO.setFechaRetorno(reservaDTO.getFechaSeleccionada());
        session.setAttribute("reservaSession", sesionDTO);

        return "redirect:/reservas/pasajero";
    }

    /**
     * Muestra el resumen de la reserva (para el flujo nuevo: datos desde sesión).
     */
    @GetMapping("/resumen")
    public String mostrarResumenSesion(Model model, HttpSession session) {
        ReservaRequestDTO dto = (ReservaRequestDTO) session.getAttribute("reservaSession");
        if (dto == null) {
            return "redirect:/viajes";
        }

        Viaje viajeIda = viajeService.obtenerViajePorId(dto.getViajeId());

        Reserva reserva = new Reserva();
        reserva.setViaje(viajeIda);
        reserva.setNombresPasajero(dto.getNombresPasajero());
        reserva.setApellidosPasajero(dto.getApellidosPasajero());
        reserva.setDniPasajero(dto.getDniPasajero());
        reserva.setEmailPasajero(dto.getEmailPasajero());

        String asientosStr = dto.getNumerosAsientos().stream()
                .map(String::valueOf).collect(Collectors.joining(","));
        reserva.setNumerosAsientos(asientosStr);
        reserva.setNumeroAsiento(dto.getNumerosAsientos().get(0));

        BigDecimal precioTotal = viajeIda.getPrecio().multiply(BigDecimal.valueOf(dto.getNumerosAsientos().size()));

        if (dto.getViajeRetornoId() != null) {
            Viaje viajeRetorno = viajeService.obtenerViajePorId(dto.getViajeRetornoId());
            reserva.setViajeRetorno(viajeRetorno);

            String asientosRetStr = dto.getNumerosAsientosRetorno().stream()
                    .map(String::valueOf).collect(Collectors.joining(","));
            reserva.setNumerosAsientosRetorno(asientosRetStr);

            BigDecimal precioRetorno = viajeRetorno.getPrecio()
                    .multiply(BigDecimal.valueOf(dto.getNumerosAsientosRetorno().size()));
            reserva.setPrecioRetorno(precioRetorno);
            precioTotal = precioTotal.add(precioRetorno);
        }

        reserva.setPrecioTotal(precioTotal);
        model.addAttribute("reserva", reserva);
        model.addAttribute("desdeSesion", true);

        return "resumen_de_reserva";
    }

    /**
     * Crea la reserva en la base de datos y redirige al pago.
     */
    @PostMapping("/confirmar")
    public String confirmarReserva(HttpSession session, RedirectAttributes redirectAttributes) {
        ReservaRequestDTO dto = (ReservaRequestDTO) session.getAttribute("reservaSession");
        if (dto == null) {
            return "redirect:/viajes";
        }

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");

        try {
            Reserva reserva = reservaService.iniciarReserva(dto, usuarioSesion);
            session.removeAttribute("reservaSession");
            return "redirect:/pagos/yape/" + reserva.getId();
        } catch (Exception e) {
            log.error("Error al confirmar reserva: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservas/resumen";
        }
    }

    /**
     * Muestra el resumen de la reserva ya persistida (flujo legacy desde pago).
     */
    @GetMapping("/{id}/resumen")
    public String mostrarResumen(@PathVariable("id") Long id, Model model) {
        Reserva reserva = reservaService.obtenerReservaPorId(id);
        model.addAttribute("reserva", reserva);
        return "resumen_de_reserva";
    }

    /**
     * Muestra la pantalla de éxito (confirmación) luego del pago.
     */
/** Aquí el sistema recupera nuevamente la información de la reserva y también consulta el método de pago utilizado mediante 
 * el PagoService.*/

/** Finalmente, se envía toda esa información a la vista ‘confirmacion_de_pago.html’ para mostrar un resumen final de la reserva 
 * y el estado del pago. */

    @GetMapping("/{id}/cancelar")
    public String cancelarReserva(@PathVariable("id") Long id) {
        try {
            reservaService.cancelarReserva(id);
        } catch (Exception e) {
            // Si ya está pagada o no existe, igual redirigimos
        }
        return "redirect:/viajes";
    }

    @GetMapping("/{id}/confirmacion")
    public String mostrarConfirmacion(@PathVariable("id") Long id, Model model,
                                       jakarta.servlet.http.HttpServletRequest request) {
        Reserva reserva = reservaService.obtenerReservaPorId(id);
        model.addAttribute("reserva", reserva);
        model.addAttribute("metodoPago",
                pagoService.obtenerPagoPorReserva(id).map(p -> p.getMetodoPago().name()).orElse("PENDIENTE"));

        // URL pública que el QR codificará: al escanearlo, cualquiera puede verificar
        // el ticket contra la base de datos real (no contra datos "congelados" en el QR).
        String urlVerificacion = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromContextPath(request)
                .path("/ticket/verificar/MNT-" + reserva.getId())
                .toUriString();
        model.addAttribute("urlVerificacion", urlVerificacion);

        // El QR se genera en el servidor (ZXing) y se envía ya como imagen lista,
        // para que siempre aparezca sin depender de librerías externas del navegador.
        String qrCodeBase64 = com.montero.app.util.QrCodeGenerator.generarComoBase64(urlVerificacion, 400);
        model.addAttribute("qrCodeBase64", qrCodeBase64);

        return "confirmacion_de_pago"; // Vista Thymeleaf existente
    }
}