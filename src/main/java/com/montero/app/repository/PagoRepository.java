package com.montero.app.repository;

import com.montero.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // Método para encontrar el pago asociado a una reserva específica
    Optional<Pago> findByReservaId(Long reservaId);
}
