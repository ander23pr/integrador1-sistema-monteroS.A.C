package com.montero.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Reserva;
import com.montero.app.service.ReservaService;

/**
 * Controlador público de verificación de tickets.
 *
 * Esta es la pantalla a la que apunta el código QR impreso en el ticket.
 * A propósito NO requiere sesión iniciada: quien verifica un ticket (personal
 * de embarque, el propio pasajero, etc.) no tiene por qué estar logueado en
 * el sistema. Por eso también se evita mostrar aquí datos sensibles completos
 * (DNI, nombre completo) — solo lo necesario para confirmar que el ticket es
 * válido y a qué viaje corresponde.
 */
@Controller
public class TicketverificacionController {

    private static final Logger logger = LoggerFactory.getLogger(TicketverificacionController.class);

    private final ReservaService reservaService;

    public TicketverificacionController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/ticket/verificar/{codigo}")
    public String verificarTicket(@PathVariable String codigo, Model model) {
        model.addAttribute("codigo", codigo);

        Long reservaId = extraerIdDesdeCodigo(codigo);
        if (reservaId == null) {
            logger.warn("Código de ticket con formato inválido: {}", codigo);
            model.addAttribute("esValido", false);
            model.addAttribute("motivo", "El código de este ticket no tiene un formato reconocido.");
            return "reserva/ticket_verificacion";
        }

        try {
            Reserva reserva = reservaService.obtenerReservaPorId(reservaId);
            boolean pagoConfirmado = reserva.getEstado() == EstadoReserva.PAGADO;

            model.addAttribute("esValido", pagoConfirmado);
            model.addAttribute("reserva", reserva);
            model.addAttribute("nombrePasajeroCorto", nombreEnmascarado(reserva));

            if (!pagoConfirmado) {
                model.addAttribute("motivo",
                        reserva.getEstado() == EstadoReserva.CANCELADO
                                ? "Esta reserva fue cancelada."
                                : "Esta reserva aún no tiene el pago confirmado.");
            }
        } catch (IllegalArgumentException excepcionReservaNoEncontrada) {
            logger.warn("Ticket escaneado no corresponde a ninguna reserva. Código: {}", codigo);
            model.addAttribute("esValido", false);
            model.addAttribute("motivo", "No se encontró ningún ticket con este código.");
        }

        return "reserva/ticket_verificacion";
    }

    /**
     * El código impreso tiene el formato "MNT-{id}". Se extrae el id numérico
     * de forma defensiva por si el QR fue alterado o el formato cambia a futuro.
     */
    private Long extraerIdDesdeCodigo(String codigo) {
        if (codigo == null || !codigo.toUpperCase().startsWith("MNT-")) {
            return null;
        }
        try {
            return Long.parseLong(codigo.substring(4));
        } catch (NumberFormatException excepcionFormatoInvalido) {
            return null;
        }
    }

    /**
     * Muestra nombre + inicial del apellido (ej. "María G.") en vez del nombre
     * completo, ya que esta pantalla es pública y no requiere autenticación.
     */
    private String nombreEnmascarado(Reserva reserva) {
        String nombres = reserva.getNombresPasajero();
        String apellidos = reserva.getApellidosPasajero();
        String primerNombre = (nombres != null && !nombres.isBlank()) ? nombres.trim().split(" ")[0] : "";
        String inicialApellido = (apellidos != null && !apellidos.isBlank()) ? apellidos.trim().substring(0, 1) + "." : "";
        return (primerNombre + " " + inicialApellido).trim();
    }
}
