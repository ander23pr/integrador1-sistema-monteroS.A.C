package com.montero.app.repository;

import com.montero.app.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    
    // Método para buscar viajes basados en los criterios del buscador del usuario
    List<Viaje> findByOrigenAndDestinoAndFechaSalida(String origen, String destino, LocalDate fechaSalida);
    
    // Método para contar viajes programados para una fecha específica
    long countByFechaSalida(LocalDate fechaSalida);
}
