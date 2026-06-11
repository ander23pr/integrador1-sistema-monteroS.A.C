package com.montero.app.controller;

import com.montero.app.model.Usuario;
import com.montero.app.service.NotificacionService;
import com.montero.app.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * GET: Muestra la página de notificaciones con todas las notificaciones del usuario
     */
    @GetMapping
    public String listarNotificaciones(Model model, HttpSession session) {
        // Obtener usuario de la sesión
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Pasar notificaciones al modelo
        model.addAttribute("notificaciones", 
            notificacionService.obtenerNotificacionesPorUsuario(usuario));
        model.addAttribute("usuario", usuario);
        model.addAttribute("noLeidas", 
            notificacionService.contarNotificacionesNoLeidas(usuario));

        return "notificaciones";
    }

    /**
     * POST: Marca una notificación como leída (AJAX)
     */
    @PostMapping("/{id}/marcar-leida")
    @ResponseBody
    public String marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return "{\"success\": true}";
    }

    /**
     * POST: Marca todas las notificaciones como leídas (AJAX)
     */
    @PostMapping("/marcar-todas-leidas")
    @ResponseBody
    public String marcarTodasComoLeidas(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            return "{\"success\": false, \"error\": \"No autenticado\"}";
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        if (usuario != null) {
            notificacionService.marcarTodasComoLeidas(usuario);
            return "{\"success\": true}";
        }

        return "{\"success\": false}";
    }

    /**
     * GET: Obtiene el contador de notificaciones no leídas (AJAX)
     */
    @GetMapping("/contar-no-leidas")
    @ResponseBody
    public String contarNoLeidas(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            return "{\"count\": 0}";
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
        if (usuario != null) {
            Long count = notificacionService.contarNotificacionesNoLeidas(usuario);
            return "{\"count\": " + count + "}";
        }

        return "{\"count\": 0}";
    }
}
