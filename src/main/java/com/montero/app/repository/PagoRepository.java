package com.montero.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.montero.app.model.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Método para encontrar el pago asociado a una reserva específica
    Optional<Pago> findByReservaId(Long reservaId);

    @Query("SELECT p FROM Pago p JOIN FETCH p.reserva ORDER BY p.id DESC")
    Page<Pago> findAllWithReserva(Pageable pageable);

    @Query("SELECT p FROM Pago p JOIN FETCH p.reserva ORDER BY p.id DESC")
    List<Pago> findAllWithReserva();

    @Query("SELECT p.metodoPago, COUNT(p) FROM Pago p GROUP BY p.metodoPago")
    List<Object[]> countGroupedByMetodoPago();

    @Query("SELECT p FROM Pago p JOIN FETCH p.reserva r JOIN FETCH r.viaje WHERE MONTH(p.fechaPago) = :month AND YEAR(p.fechaPago) = :year")
    List<Pago> findAllWithReservaAndViajeByMes(@Param("month") int month, @Param("year") int year);

    @Query("SELECT p FROM Pago p JOIN FETCH p.reserva r JOIN FETCH r.viaje ORDER BY p.fechaPago")
    List<Pago> findAllWithReservaAndViaje();
}
