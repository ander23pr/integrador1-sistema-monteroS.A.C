package com.montero.app.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montero.app.dto.PagoYapeDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.MetodoPago;
import com.montero.app.model.Pago;
import com.montero.app.model.Reserva;
import com.montero.app.repository.PagoRepository;
import com.montero.app.repository.ReservaRepository;

/**
 * Servicio encargado de gestionar los pagos simulados.
 */
@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final NotificacionService notificacionService;

    public PagoService(PagoRepository pagoRepository, ReservaRepository reservaRepository,
                        NotificacionService notificacionService) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.notificacionService = notificacionService;
    }

    private void validarReservaPendiente(Long reservaId, Reserva reserva) {
        if (!reserva.getEstado().equals(EstadoReserva.PENDIENTE)) {
            logger.warn("Intento de pago para reserva no en estado PENDIENTE. Reserva ID: {}, Estado actual: {}",
                    reservaId, reserva.getEstado());
            throw new IllegalStateException("La reserva no se encuentra en estado PENDIENTE o ya fue procesada.");
        }
    }

    private Pago finalizarPago(Reserva reserva, MetodoPago metodo, Pago pago) {
        pago.setReserva(reserva);
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());

        Pago pagoGuardado = pagoRepository.save(pago);

        reserva.setEstado(EstadoReserva.PAGADO);
        reservaRepository.save(reserva);

        notificacionService.crearNotificacionConfirmacionReserva(reserva);
        notificacionService.crearNotificacionNuevaVenta(reserva, metodo);

        logger.info("Pago procesado exitosamente. Reserva ID: {}, Método: {}", reserva.getId(), metodo);
        return pagoGuardado;
    }

    @Transactional
    public Pago procesarPagoYape(Long reservaId, PagoYapeDTO pagoYapeDTO) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + reservaId));
        validarReservaPendiente(reservaId, reserva);

        Pago pago = new Pago();
        pago.setNumeroTelefono(pagoYapeDTO.getNumeroTelefono());
        pago.setCodigoAprobacion(pagoYapeDTO.getCodigoAprobacion());

        return finalizarPago(reserva, MetodoPago.YAPE, pago);
    }

    @Transactional
    public Pago procesarPagoTarjeta(Long reservaId, String numeroTarjeta, String fechaExpiracion, String cvv, String nombreTitular) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + reservaId));
        validarReservaPendiente(reservaId, reserva);

        if (numeroTarjeta == null || numeroTarjeta.replace(" ", "").length() < 13) {
            throw new IllegalArgumentException("Número de tarjeta inválido.");
        }
        if (nombreTitular == null || nombreTitular.isBlank()) {
            throw new IllegalArgumentException("El nombre del titular es obligatorio.");
        }

        Pago pago = new Pago();
        pago.setNombreTitular(nombreTitular);
        pago.setCodigoAprobacion("TARJ-" + LocalDateTime.now().getSecond());

        return finalizarPago(reserva, MetodoPago.TARJETA, pago);
    }

    @Transactional
    public Pago procesarPagoEfectivo(Long reservaId, String correo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + reservaId));
        validarReservaPendiente(reservaId, reserva);

        if (correo == null || !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Correo electrónico inválido.");
        }

        Pago pago = new Pago();
        pago.setCorreo(correo);
        pago.setCodigoAprobacion("CIP-" + System.currentTimeMillis());

        return finalizarPago(reserva, MetodoPago.PAGO_EFECTIVO, pago);
    }

    @Transactional
    public Pago procesarPagoQR(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + reservaId));
        validarReservaPendiente(reservaId, reserva);

        Pago pago = new Pago();
        pago.setCodigoAprobacion("QR-" + System.currentTimeMillis());

        return finalizarPago(reserva, MetodoPago.PAGO_QR, pago);
    }

    public Optional<Pago> obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }
}