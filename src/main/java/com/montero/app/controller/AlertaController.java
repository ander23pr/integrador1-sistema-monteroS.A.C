package com.montero.app.controller;

import com.montero.app.model.Alerta;
import com.montero.app.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/alertas")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    public String listarAlertas(Model model) {
        // Obtenemos la lista real de la base de datos
        List<Alerta> listaAlertas = alertaService.obtenerTodas();
        
        model.addAttribute("tituloPagina", "Panel Oficial de Alertas");
        model.addAttribute("alertas", listaAlertas); // Enviamos la lista al HTML
        
        return "alertas/lista";
    }
}