package com.montero.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador raíz del sistema.
 * Gestiona la redirección inicial y la pantalla de inicio (dashboard).
 */
@Controller
public class HomeController {

    /**
     * Ruta raíz: redirige al login como puerta de entrada.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    /**
     * Dashboard principal post-login.
     */
    @GetMapping("/inicio")
    public String inicio() {
        return "modulo_inicio";
    }
}
