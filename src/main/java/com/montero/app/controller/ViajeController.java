package com.montero.app.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.montero.app.model.Viaje;
import com.montero.app.service.ViajeService;

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
    public String listarViajes(@RequestParam(value = "origen", required = false) String origen,
                               @RequestParam(value = "destino", required = false) String destino,
                               @RequestParam(value = "fecha", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                               Model model) {
        model.addAttribute("viajes", viajeService.obtenerTodos());
        model.addAttribute("origenBuscado", origen);
        model.addAttribute("destinoBuscado", destino);
        model.addAttribute("fechaBuscada", fecha);
        return "reserva/busqueda_rutas"; // Vista Thymeleaf existente
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
            @RequestParam(value = "modo", required = false) String modo,
            Model model) {

        model.addAttribute("origenBuscado", origen);
        model.addAttribute("destinoBuscado", destino);
        model.addAttribute("fechaBuscada", fecha);
        model.addAttribute("fechaRetornoBuscada", fechaRetorno);
        model.addAttribute("modo", modo != null ? modo : "ida");

        if ("retorno".equals(modo)) {
            // Los parámetros ya vienen invertidos (origen=destinoIda, destino=origenIda)
            List<Viaje> resultados = viajeService.buscarViajes(origen, destino, fecha);
            model.addAttribute("viajes", resultados);
        } else {
            List<Viaje> resultados = viajeService.buscarViajes(origen, destino, fecha);
            model.addAttribute("viajes", resultados);
        }

        return "reserva/seleccion_pasaje";
    }
}
