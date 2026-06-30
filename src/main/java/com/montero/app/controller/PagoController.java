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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.montero.app.dto.PagoYapeDTO;
import com.montero.app.model.Reserva;
import com.montero.app.service.PagoService;
import com.montero.app.service.ReservaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ReservaService reservaService;

    private String redirigirSiPagado(Long reservaId, Model model) {
        Reserva reserva = reservaService.obtenerReservaPorId(reservaId);
        if (reserva.getEstado().name().equals("PAGADO")) {
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        }
        model.addAttribute("reserva", reserva);
        return null;
    }

    @GetMapping("/yape/{reservaId}")
    public String mostrarPagoYape(@PathVariable("reservaId") Long reservaId, Model model) {
        String redir = redirigirSiPagado(reservaId, model);
        if (redir != null) return redir;
        model.addAttribute("pagoYapeDTO", new PagoYapeDTO());
        return "metodos_pago/procesar_pago_yape";
    }

    @PostMapping("/yape/procesar/{reservaId}")
    public String procesarPagoYape(@PathVariable("reservaId") Long reservaId,
                                   @Valid @ModelAttribute("pagoYapeDTO") PagoYapeDTO pagoYapeDTO,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "metodos_pago/procesar_pago_yape";
        }
        try {
            pagoService.procesarPagoYape(reservaId, pagoYapeDTO);
            redirectAttributes.addFlashAttribute("mostrarToastReserva", true);
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "metodos_pago/procesar_pago_yape";
        }
    }

    @GetMapping("/tarjeta/{reservaId}")
    public String mostrarPagoTarjeta(@PathVariable("reservaId") Long reservaId, Model model) {
        String redir = redirigirSiPagado(reservaId, model);
        if (redir != null) return redir;
        return "metodos_pago/procesar_pago_tarjeta";
    }

    @PostMapping("/tarjeta/procesar/{reservaId}")
    public String procesarPagoTarjeta(@PathVariable("reservaId") Long reservaId,
                                      @RequestParam("numeroTarjeta") String numeroTarjeta,
                                      @RequestParam(value = "fechaExpiracion", required = false) String fechaExpiracion,
                                      @RequestParam(value = "cvv", required = false) String cvv,
                                      @RequestParam("nombreTitular") String nombreTitular,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            pagoService.procesarPagoTarjeta(reservaId, numeroTarjeta, fechaExpiracion, cvv, nombreTitular);
            redirectAttributes.addFlashAttribute("mostrarToastReserva", true);
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "metodos_pago/procesar_pago_tarjeta";
        }
    }

    @GetMapping("/pagoefectivo/{reservaId}")
    public String mostrarPagoEfectivo(@PathVariable("reservaId") Long reservaId, Model model) {
        String redir = redirigirSiPagado(reservaId, model);
        if (redir != null) return redir;
        return "metodos_pago/procesar_pagoefectivo";
    }

    @PostMapping("/pagoefectivo/procesar/{reservaId}")
    public String procesarPagoEfectivo(@PathVariable("reservaId") Long reservaId,
                                       @RequestParam("correo") String correo,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            pagoService.procesarPagoEfectivo(reservaId, correo);
            redirectAttributes.addFlashAttribute("mostrarToastReserva", true);
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("reserva", reservaService.obtenerReservaPorId(reservaId));
            return "metodos_pago/procesar_pagoefectivo";
        }
    }

    @GetMapping("/qr/{reservaId}")
    public String mostrarPagoQR(@PathVariable("reservaId") Long reservaId, Model model) {
        String redir = redirigirSiPagado(reservaId, model);
        if (redir != null) return redir;
        return "metodos_pago/procesar_pagoqr";
    }

    @PostMapping("/qr/procesar/{reservaId}")
    public String procesarPagoQR(@PathVariable("reservaId") Long reservaId,
                              RedirectAttributes redirectAttributes) {
        try {
            pagoService.procesarPagoQR(reservaId);
            redirectAttributes.addFlashAttribute("mostrarToastReserva", true);
            return "redirect:/reservas/" + reservaId + "/confirmacion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pagos/qr/" + reservaId;
        }
    }
}