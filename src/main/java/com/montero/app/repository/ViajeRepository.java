package com.montero.app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montero.app.model.Viaje;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    
    @Query("SELECT v FROM Viaje v WHERE v.origen = :origen AND v.destino = :destino AND v.fechaSalida = :fecha AND v.estado <> 'CANCELADO'")
    List<Viaje> findDisponibles(@Param("origen") String origen,
                                @Param("destino") String destino,
                                @Param("fecha") LocalDate fecha);

    @Query("SELECT v FROM Viaje v WHERE v.estado <> 'CANCELADO' ORDER BY v.fechaSalida DESC, v.horaSalida DESC")
    List<Viaje> findAllDisponibles();

    // Método para contar viajes programados para una fecha específica
    long countByFechaSalida(LocalDate fechaSalida);

    // Método paginado con orden descendente por fecha de salida
    @Query("SELECT v FROM Viaje v ORDER BY v.fechaSalida DESC, v.horaSalida DESC")
    Page<Viaje> findAllOrdered(Pageable pageable);

    List<Viaje> findByFechaSalidaBetweenOrderByFechaSalida(LocalDate inicio, LocalDate fin);
}
