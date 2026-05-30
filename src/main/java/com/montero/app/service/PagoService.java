package com.montero.app.service;

import com.montero.app.dto.PagoYapeDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.MetodoPago;
import com.montero.app.model.Pago;
import com.montero.app.model.Reserva;
import com.montero.app.repository.PagoRepository;
import com.montero.app.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio encargado de gestionar los pagos simulados.
 */
@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    /**
     * Procesa la simulación de pago con Yape.
     * Cambia el estado de la reserva y guarda la entidad Pago.
     */
    @Transactional
    public Pago procesarPagoYape(Long reservaId, PagoYapeDTO pagoYapeDTO) {
        Reserva reserva = reservaService.obtenerReservaPorId(reservaId);

        // Validar que la reserva esté en estado PENDIENTE
        if (!reserva.getEstado().equals(EstadoReserva.PENDIENTE)) {
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

        return pagoGuardado;
    }

    public Optional<Pago> obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }
}
