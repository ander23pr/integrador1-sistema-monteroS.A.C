package com.montero.app.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.montero.app.dto.ReservaRequestDTO;
import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.service.PagoService;
import com.montero.app.service.ReservaService;
import com.montero.app.service.ViajeService;

import jakarta.validation.Valid;

/**
 * “El ReservaController se encarga de administrar todo el flujo de reservas dentro del sistema.
 * Y actúa como intermediario entre las vistas desarrolladas con Thymeleaf, la lógica de negocio implementada en los Services,
 * y la base de datos.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ViajeService viajeService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private PagoService pagoService;

    /**
     * Este método se encarga de cargar la vista de selección de asientos.
     */
    @GetMapping("/viaje/{id}/asientos")
    public String mostrarSeleccionAsientos(@PathVariable("id") Long viajeId,
                                           @RequestParam(value = "origen", required = false) String origen,
                                           @RequestParam(value = "destino", required = false) String destino,
                                           @RequestParam(value = "fecha", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                           Model model) {

        //* ViajeService, obtiene los datos completos del viaje y también consulta qué asientos ya se encuentran ocupados. */   
        Viaje viaje = viajeService.obtenerViajePorId(viajeId);
        List<Integer> asientosOcupados = viajeService.obtenerAsientosOcupados(viajeId);

        model.addAttribute("viaje", viaje);

        /** toda esa información se envía al objeto Model para que la vista Thymeleaf pueda renderizar dinámicamente:la ruta,la fecha,
            el horario y los asientos disponibles. */
        model.addAttribute("asientosOcupados", asientosOcupados); 
        
        // se crea un DTO que sirve para mantener temporalmente los datos seleccionados por el usuario durante el flujo de reserva, y 
        // el método retorna la vista ‘seleccion_asientos.html’.
        ReservaRequestDTO reservaDTO = new ReservaRequestDTO();
        reservaDTO.setViajeId(viajeId);
        reservaDTO.setOrigenSeleccionado(origen != null ? origen : viaje.getOrigen());
        reservaDTO.setDestinoSeleccionado(destino != null ? destino : viaje.getDestino());
        reservaDTO.setFechaSeleccionada(fecha != null ? fecha : viaje.getFechaSalida());
        model.addAttribute("reservaDTO", reservaDTO);
        model.addAttribute("fechaSeleccionada", reservaDTO.getFechaSeleccionada());

        return "seleccion_asientos"; // Vista Thymeleaf existente
    }

    /**
     * Este método se encarga de procesar la reserva enviada desde el formulario de selección de asientos.
     */

    /** Primero, el sistema valida automáticamente los datos ingresados por el usuario, como nombres, DNI y asientos seleccionados. */
    @PostMapping("/iniciar")
    public String iniciarReserva(@Valid @ModelAttribute("reservaDTO") ReservaRequestDTO reservaDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Si hay errores de validación (por ejemplo, faltan apellidos o el DNI no tiene 8 dígitos)
        // el controlador vuelve a cargar la vista de selección de asientos manteniendo la información del viaje y mostrando los mensajes de error correspondientes.

        if (bindingResult.hasErrors()) {
            // Recargamos los datos del viaje para que la vista no falle al renderizarse de nuevo
            Viaje viaje = viajeService.obtenerViajePorId(reservaDTO.getViajeId());
            model.addAttribute("viaje", viaje);
            model.addAttribute("asientosOcupados", viajeService.obtenerAsientosOcupados(viaje.getId()));
            model.addAttribute("fechaSeleccionada",
                    reservaDTO.getFechaSeleccionada() != null ? reservaDTO.getFechaSeleccionada() : viaje.getFechaSalida());
            return "seleccion_asientos"; 
        }

        try {
            // Intentamos crear la reserva
            Reserva reserva = reservaService.iniciarReserva(reservaDTO);
            
            // Si tiene éxito, redirigimos a la vista de resumen de ESA reserva específica
            return "redirect:/reservas/" + reserva.getId() + "/resumen";
            
        } catch (Exception e) {
            // Si falla la lógica de negocio (ej. asiento fue ocupado por otra persona al mismo tiempo)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addAttribute("origen", reservaDTO.getOrigenSeleccionado());
            redirectAttributes.addAttribute("destino", reservaDTO.getDestinoSeleccionado());
            redirectAttributes.addAttribute("fecha", reservaDTO.getFechaSeleccionada());
            return "redirect:/reservas/viaje/" + reservaDTO.getViajeId() + "/asientos";
        }
    }

    /**
     * Muestra el resumen de la reserva antes de pasar a la pasarela de pagos.
     */

    /** recibe el identificador de la reserva, consulta la información correspondiente mediante el ReservaService y envía los datos 
     * a la vista ‘resumen_de_reserva.html’. */
    @GetMapping("/{id}/resumen")
    public String mostrarResumen(@PathVariable("id") Long id, Model model) {
        Reserva reserva = reservaService.obtenerReservaPorId(id);
        model.addAttribute("reserva", reserva);
        return "resumen_de_reserva"; // Vista Thymeleaf existente
    }

    /**
     * Muestra la pantalla de éxito (confirmación) luego del pago.
     */
/** Aquí el sistema recupera nuevamente la información de la reserva y también consulta el método de pago utilizado mediante 
 * el PagoService.*/

/** Finalmente, se envía toda esa información a la vista ‘confirmacion_de_pago.html’ para mostrar un resumen final de la reserva 
 * y el estado del pago. */

    @GetMapping("/{id}/confirmacion")
    public String mostrarConfirmacion(@PathVariable("id") Long id, Model model) {
        Reserva reserva = reservaService.obtenerReservaPorId(id);
        model.addAttribute("reserva", reserva);
        model.addAttribute("metodoPago",
                pagoService.obtenerPagoPorReserva(id).map(p -> p.getMetodoPago().name()).orElse("PENDIENTE"));
        return "confirmacion_de_pago"; // Vista Thymeleaf existente
    }
}

