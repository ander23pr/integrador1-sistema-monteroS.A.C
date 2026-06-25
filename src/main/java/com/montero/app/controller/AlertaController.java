package com.montero.app.controller;

import com.montero.app.model.Alerta;
import com.montero.app.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    // 1. LISTAR ALERTAS
    @GetMapping
    public String listarAlertas(Model model) {
        // Obtenemos la lista real de la base de datos
        List<Alerta> listaAlertas = alertaService.obtenerTodas();
        
        model.addAttribute("tituloPagina", "Panel Oficial de Alertas");
        model.addAttribute("alertas", listaAlertas); // Enviamos la lista al HTML
        
        return "alertas/lista";
    }

    // 2. MOSTRAR EL FORMULARIO DE NUEVA ALERTA
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("alerta", new Alerta());
        model.addAttribute("tituloPagina", "Registrar Nueva Alerta");
        return "alertas/formulario";
    }

    // 3. GUARDAR LA ALERTA EN LA BASE DE DATOS
    @PostMapping("/guardar")
    public String guardarAlerta(@ModelAttribute("alerta") Alerta alerta) {
        // Corregido: Llamamos exactamente a guardarAlerta como está en tu servicio
        alertaService.guardarAlerta(alerta); 
        return "redirect:/alertas";    // Te redirecciona al panel azul automáticamente
    }
}