package com.montero.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Reserva;

// Este Repository es la capa encargada de comunicarse directamente con la base de datos

// permite utilizar automáticamente operaciones CRUD como guardar, editar, eliminar y buscar registros sin 
// necesidad de escribir SQL manualmente.

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r JOIN FETCH r.viaje ORDER BY r.id DESC")
    Page<Reserva> findAllWithViaje(Pageable pageable);

    @Query("SELECT r FROM Reserva r JOIN FETCH r.viaje LEFT JOIN FETCH r.viajeRetorno WHERE r.id = :id")
    java.util.Optional<Reserva> findByIdWithViaje(@Param("id") Long id);

    // Método para obtener todas las reservas de un viaje,
    // útil para saber qué asientos ya están ocupados.
    List<Reserva> findByViajeId(Long viajeId);

    // Método para obtener el historial de reservas de un usuario autenticado.
    @EntityGraph(attributePaths = "viaje")
    List<Reserva> findByUsuarioId(Long usuarioId);

    // Método para obtener las reservas pagadas cuyo viaje sale en una fecha específica,
    // usado por el recordatorio automático de viajes próximos.
    @EntityGraph(attributePaths = {"viaje", "usuario"})
    List<Reserva> findByEstadoAndViaje_FechaSalida(EstadoReserva estado, LocalDate fechaSalida);

    long countByEstado(EstadoReserva estado);

    @Query("SELECT r FROM Reserva r JOIN FETCH r.viaje WHERE r.estado = :estado AND MONTH(r.fechaCreacion) = :month AND YEAR(r.fechaCreacion) = :year")
    List<Reserva> findByEstadoAndMesWithViaje(@Param("estado") EstadoReserva estado, @Param("month") int month, @Param("year") int year);
}