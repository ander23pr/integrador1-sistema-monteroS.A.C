package com.montero.app.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.montero.app.model.Usuario;
import com.montero.app.service.NotificacionService;

import jakarta.servlet.http.HttpSession;

/**
 * Agrega a todas las vistas renderizadas por un @Controller el atributo
 * "notificacionesNoLeidas", que fragments/navegacion.html usa para pintar el
 * badge de la barra de navegación en el primer render (antes de que el
 * polling de notificaciones.js lo actualice). Centralizarlo aquí evita
 * repetir la misma consulta en cada controller de la aplicación.
 */
@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
public class NavegacionModelAdvice {

    private final NotificacionService notificacionService;

    public NavegacionModelAdvice(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute("notificacionesNoLeidas")
    public long notificacionesNoLeidas(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) return 0L;
        if ("ADMIN".equals(usuarioSesion.getRol())) {
            return notificacionService.contarNoLeidasAdmin();
        }
        return notificacionService.contarNoLeidas(usuarioSesion.getId());
    }
}