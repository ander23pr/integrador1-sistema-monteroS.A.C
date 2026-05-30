package com.montero.app.controller;

import com.montero.app.model.Viaje;
import com.montero.app.service.ViajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la visualización y búsqueda de viajes (Etapa 2).
 */
@Controller
@RequestMapping("/viajes")
public class ViajeController {

    @Autowired
    private ViajeService viajeService;

    /**
     * Muestra el listado de todos los viajes disponibles.
     */
    @GetMapping
    public String listarViajes(Model model) {
        model.addAttribute("viajes", viajeService.obtenerTodos());
        return "busqueda_rutas"; // Vista Thymeleaf existente
    }

    /**
     * Procesa la búsqueda de viajes por origen, destino y fecha.
     */
    @GetMapping("/buscar")
    public String buscarViajes(
            @RequestParam("origen") String origen,
            @RequestParam("destino") String destino,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "fechaRetorno", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRetorno,
            Model model) {

        List<Viaje> resultados = viajeService.buscarViajes(origen, destino, fecha);

        // Enviamos los resultados a la vista
        model.addAttribute("viajes", resultados);

        // Criterios de búsqueda para mostrar en el resumen de seleccion_pasaje
        model.addAttribute("origenBuscado", origen);
        model.addAttribute("destinoBuscado", destino);
        model.addAttribute("fechaBuscada", fecha);
        // Fecha de retorno es opcional; puede ser null
        model.addAttribute("fechaRetornoBuscada", fechaRetorno);

        return "seleccion_pasaje";
    }
}
