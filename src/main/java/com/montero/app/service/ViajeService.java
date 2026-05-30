package com.montero.app.service;

import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.repository.ReservaRepository;
import com.montero.app.repository.ViajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio de Viajes.
 * Corresponde a la Etapa 2 del desarrollo.
 */
@Service
public class ViajeService {

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Busca viajes disponibles según origen, destino y fecha.
     */
    public List<Viaje> buscarViajes(String origen, String destino, LocalDate fechaSalida) {
        return viajeRepository.findByOrigenAndDestinoAndFechaSalida(origen, destino, fechaSalida);
    }

    /**
     * Obtiene todos los viajes disponibles (útil para el listado inicial).
     */
    public List<Viaje> obtenerTodos() {
        return viajeRepository.findAll();
    }

    /**
     * Obtiene los detalles de un viaje específico por su ID.
     */
    public Viaje obtenerViajePorId(Long id) {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado con ID: " + id));
    }

    /**
     * Obtiene la lista de números de asiento que ya se encuentran ocupados (PENDIENTES o PAGADOS).
     * @param viajeId El ID del viaje.
     * @return Lista de números de asiento ocupados.
     */
    public List<Integer> obtenerAsientosOcupados(Long viajeId) {
        List<Reserva> reservas = reservaRepository.findByViajeId(viajeId);
        
        // Filtramos las reservas canceladas, ya que esos asientos vuelven a estar libres
        return reservas.stream()
                .filter(r -> !r.getEstado().name().equals("CANCELADO"))
                .flatMap(r -> r.getListaNumerosAsientos().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
