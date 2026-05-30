package com.montero.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.montero.app.model.Reserva;

// Este Repository es la capa encargada de comunicarse directamente con la base de datos

// permite utilizar automáticamente operaciones CRUD como guardar, editar, eliminar y buscar registros sin 
// necesidad de escribir SQL manualmente.

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Método para obtener todas las reservas de un viaje,
    // útil para saber qué asientos ya están ocupados.
    List<Reserva> findByViajeId(Long viajeId);
}
