package com.montero.app.service;

import com.montero.app.model.Alerta;
import com.montero.app.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;

    // Método para listar todas las alertas de la base de datos
    public List<Alerta> obtenerTodas() {
        return alertaRepository.findAll();
    }

    // Método para guardar o crear una nueva alerta
    public Alerta guardarAlerta(Alerta alerta) {
        return alertaRepository.save(alerta);
    }

    // Método para buscar alertas pendientes
    public List<Alerta> obtenerPendientes() {
        return alertaRepository.findByEstado("PENDIENTE");
    }
}