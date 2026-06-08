package com.montero.app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montero.app.dto.ReservaRequestDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.repository.ReservaRepository;

/**
 * Servicio encargado de la lógica de negocio de las Reservas.
 */
// Su función principal es validar, procesar y registrar una reserva antes de guardarla en la base de datos.

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ViajeService viajeService;

    /**
     * Inicia una nueva reserva en estado PENDIENTE, validando la disponibilidad del asiento.
     */
    @Transactional
    public Reserva iniciarReserva(ReservaRequestDTO dto) {

        if (dto.getViajeId() == null) {
            throw new IllegalArgumentException("No se encontró el viaje seleccionado. Por favor recargue la página e intente nuevamente.");
        }

        if (dto.getDniPasajero() == null || dto.getDniPasajero().isBlank()) {
            throw new IllegalArgumentException("El DNI del pasajero es obligatorio.");
        }

        // 1. Obtener el viaje seleccionado y los asientos enviados desde el formulario
        Viaje viaje = viajeService.obtenerViajePorId(dto.getViajeId());
        List<Integer> asientosSeleccionados = dto.getNumerosAsientos();

        // 2. Validar que el usuario haya seleccionado al menos un asiento
        if (asientosSeleccionados == null || asientosSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un asiento.");
        }

        // 3. Validar que no se seleccionen más de 5 asientos
        long asientosUnicos = asientosSeleccionados.stream().distinct().count();
        if (asientosUnicos > 5) {
            throw new IllegalArgumentException("No se pueden reservar más de 5 asientos por operación.");
        }

        // 4. Validar que los asientos existan dentro del rango permitido del bus
        boolean asientoFueraDeRango = asientosSeleccionados.stream()
                .anyMatch(numero -> numero == null || numero < 1 || numero > viaje.getAsientosTotales());
        if (asientoFueraDeRango) {
            throw new IllegalArgumentException("Uno o más asientos son inválidos o están fuera de rango.");
        }

        // 5. Consultar qué asientos ya están ocupados para ese viaje
        List<Integer> asientosOcupados = viajeService.obtenerAsientosOcupados(viaje.getId());

        // 6. Validar que los asientos seleccionados no estén reservados
        boolean hayAsientoOcupado = asientosSeleccionados.stream().anyMatch(asientosOcupados::contains);
        if (hayAsientoOcupado) {
            throw new IllegalStateException("Uno o más asientos seleccionados ya se encuentran ocupados. Por favor, seleccione otros.");
        }

        // 7. Validar límite máximo de reservas activas por pasajero
        String dniPasajero = dto.getDniPasajero();
        long reservasActivasDni = reservaRepository.findByViajeId(viaje.getId()).stream()
                .filter(r -> !r.getEstado().name().equals("CANCELADO"))
                .filter(r -> dniPasajero.equals(r.getDniPasajero()))
                .count();
        if (reservasActivasDni >= 5) {
            throw new IllegalStateException("Se ha alcanzado el máximo de 5 reservas por pasajero para este viaje.");
        }

        // 8. Crear la nueva reserva con estado PENDIENTE y calcular el precio total
        Reserva reserva = new Reserva();

         // 9. Asociar la reserva con el viaje seleccionado
        reserva.setViaje(viaje);

         // 10. Ordenar y almacenar los asientos seleccionados
        List<Integer> asientosOrdenados = asientosSeleccionados.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        reserva.setNumeroAsiento(asientosOrdenados.get(0));
        reserva.setNumerosAsientos(
                asientosOrdenados.stream().map(String::valueOf).collect(Collectors.joining(","))
        );

        // 11. Registrar el estado y fecha de creación de la reserva
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(LocalDateTime.now());

        // 12. Calcular el precio total según la cantidad de asientos
        reserva.setPrecioTotal(viaje.getPrecio().multiply(BigDecimal.valueOf(asientosOrdenados.size())));

        // 13. Guardar los datos del pasajero en la reserva
        reserva.setNombresPasajero(dto.getNombresPasajero());
        reserva.setApellidosPasajero(dto.getApellidosPasajero());
        reserva.setDniPasajero(dto.getDniPasajero());
        reserva.setEmailPasajero(dto.getEmailPasajero());

        // 14. Guardar finalmente la reserva en la base de datos
        return reservaRepository.save(reserva);
    }

    /**
     * Obtiene una reserva por su ID para mostrar el resumen.
     */
    public Reserva obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
    }
}
