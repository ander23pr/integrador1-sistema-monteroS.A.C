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

    public PagoService(PagoRepository pagoRepository, ReservaRepository reservaRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Procesa la simulación de pago con Yape.
     * Cambia el estado de la reserva y guarda la entidad Pago.
     */
    @Transactional
    public Pago procesarPagoYape(Long reservaId, PagoYapeDTO pagoYapeDTO) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + reservaId));

        // Validar que la reserva esté en estado PENDIENTE
        if (!reserva.getEstado().equals(EstadoReserva.PENDIENTE)) {
            logger.warn("Intento de pago para reserva no en estado PENDIENTE. Reserva ID: {}, Estado actual: {}", 
                    reservaId, reserva.getEstado());
            throw new IllegalStateException("La reserva no se encuentra en estado PENDIENTE o ya fue procesada.");
        }

        // 1. Crear el registro de pago
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMetodoPago(MetodoPago.YAPE);
        pago.setNumeroTelefono(pagoYapeDTO.getNumeroTelefono());
        pago.setCodigoAprobacion(pagoYapeDTO.getCodigoAprobacion());
        pago.setFechaPago(LocalDateTime.now());

        // 2. Guardar el pago
        Pago pagoGuardado = pagoRepository.save(pago);

        // 3. Actualizar el estado de la reserva a PAGADO
        reserva.setEstado(EstadoReserva.PAGADO);
        reservaRepository.save(reserva);

        logger.info("Pago procesado exitosamente. Reserva ID: {}, Método: YAPE, Teléfono: {}", 
                reservaId, pagoYapeDTO.getNumeroTelefono());
        return pagoGuardado;
    }

    public Optional<Pago> obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }
}
