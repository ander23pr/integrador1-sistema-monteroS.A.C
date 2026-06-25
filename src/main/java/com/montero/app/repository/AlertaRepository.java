package com.montero.app.repository;

import com.montero.app.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Este método buscará automáticamente las alertas por su estado (ej. "PENDIENTE")
    List<Alerta> findByEstado(String estado);
}