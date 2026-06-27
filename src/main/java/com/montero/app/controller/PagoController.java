package com.montero.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.montero.app.dto.PagoYapeDTO;
import com.montero.app.model.Reserva;
import com.montero.app.service.PagoService;
import com.montero.app.service.ReservaService;

import jakarta.validation.Valid;

/**
 * Controlador que gestiona los flujos de pago simulado.
 */
@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ReservaService reservaService;

    /**
     * Muestra la pantalla para procesar el pago con Yape.
     */
    @GetMapping("/yape/{reservaId}")
    public String mostrarPagoYape(@PathVariable("reservaId") Long reservaId, Model model) {
        Reserva reserva = reservaService.obtenerReservaPorId(reservaId);
        
        // Evitamos que intenten pagar una reserva que ya está pagada
        if(reserva.getEstado().name().equals("PAGADO")) {
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        }

        model.addAttribute("reserva", reserva);
        // Objeto para respaldar el formulario Thymeleaf
        model.addAttribute("pagoYapeDTO", new PagoYapeDTO());
        
        return "procesar_pago_yape"; 
    }

    /**
     * Procesa la simulación de pago enviada por el formulario Yape.
     */
    @PostMapping("/yape/procesar/{reservaId}")
    public String procesarPagoYape(@PathVariable("reservaId") Long reservaId,
                                   @Valid @ModelAttribute("pagoYapeDTO") PagoYapeDTO pagoYapeDTO,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        // Si la validación DTO falla (ej. teléfono no tiene 9 dígitos)
        if (bindingResult.hasErrors()) {
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "procesar_pago_yape";
        }

        try {
            // Intentar procesar el pago y cambiar el estado
            pagoService.procesarPagoYape(reservaId, pagoYapeDTO);

            // Bandera de un solo uso: activa el toast de éxito en la vista de confirmación
            redirectAttributes.addFlashAttribute("mostrarToastReserva", true);

            // Si el pago es exitoso, redirigimos a la confirmación final
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        } catch (Exception e) {
            // Si hay un error de negocio (ej. reserva cancelada)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "procesar_pago_yape";
        }
    }
}
