package com.montero.app.controller;

import java.time.LocalTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.montero.app.model.Usuario;

import jakarta.servlet.http.HttpSession;

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
    public String inicio(Model model, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion != null) {
            String nombreCompleto = usuarioSesion.getNombre() != null && !usuarioSesion.getNombre().isBlank()
                    ? usuarioSesion.getNombre()
                    : (usuarioSesion.getEmail() != null ? usuarioSesion.getEmail() : "Usuario");
            String primerNombre = obtenerPrimerNombre(nombreCompleto);
            model.addAttribute("saludoBienvenida", generarSaludo(primerNombre));
        } else {
            model.addAttribute("saludoBienvenida", "Bienvenido");
        }
        return "modulo_inicio";
    }

    private String obtenerPrimerNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "Usuario";
        }
        String[] partes = nombreCompleto.trim().split("\\s+");
        return partes.length > 0 ? partes[0] : nombreCompleto;
    }

    private String generarSaludo(String nombre) {
        int hora = LocalTime.now().getHour();
        String saludo;
        if (hora >= 5 && hora < 12) {
            saludo = "Buenos días";
        } else if (hora >= 12 && hora < 19) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }
        return String.format("%s, %s", saludo, nombre);
    }

    /**
     * Vista de servicios y novedades de la empresa.
     */
    @GetMapping("/servicios")
    public String servicios() {
        return "servicios";
    }
}
