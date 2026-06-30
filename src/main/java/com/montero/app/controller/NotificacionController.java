package com.montero.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.montero.app.model.Notificacion;
import com.montero.app.model.Usuario;
import com.montero.app.service.NotificacionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public String verNotificaciones(Model model, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        Map<String, List<Notificacion>> notificacionesAgrupadas =
                notificacionService.obtenerNotificacionesAgrupadas(usuarioSesion.getId());

        model.addAttribute("notificacionesAgrupadas", notificacionesAgrupadas);
        model.addAttribute("usuario", usuarioSesion);
        return "notificaciones_dinamica";
    }

    @PostMapping("/marcar-todo-leido")
    public String marcarTodoLeido(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion != null) {
            notificacionService.marcarTodasComoLeidas(usuarioSesion.getId());
        }
        return "redirect:/notificaciones";
    }

    @GetMapping("/reservas/{id}")
    public String verReserva(@PathVariable Long id) {
        return "redirect:/reservas/" + id + "/confirmacion";
    }

    /**
     * Elimina una notificación puntual. Se llama vía fetch() desde el gesto de
     * "deslizar para eliminar" en la vista, sin recargar la página.
     */
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            notificacionService.eliminarNotificacion(id, usuarioSesion.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | SecurityException excepcionNotificacionInvalida) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Endpoint JSON consultado por static/js/notificaciones.js (polling cada 30s)
     * para refrescar el badge de la barra de navegación sin recargar la página.
     */
    @GetMapping("/api/no-leidas")
    @ResponseBody
    public Map<String, Long> contarNoLeidas(HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        long count = usuarioSesion != null ? notificacionService.contarNoLeidas(usuarioSesion.getId()) : 0L;
        return Map.of("count", count);
    }
}