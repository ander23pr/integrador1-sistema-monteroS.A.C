package com.montero.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alertas")
public class AlertaController {

    @GetMapping
    public String listarAlertas(Model model) {
        // Por ahora pasamos un título de prueba a la vista
        model.addAttribute("tituloPagina", "Mis Alertas del Sistema");
        return "alertas/lista"; // Esto buscará un HTML en templates/alertas/lista.html
    }
}