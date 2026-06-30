package com.montero.app.controller;

import com.montero.app.dto.CambioPasswordDTO;
import com.montero.app.dto.PerfilUsuarioDTO;
import com.montero.app.model.SesionUsuario;
import com.montero.app.model.Usuario;
import com.montero.app.service.PerfilUsuarioService;
import com.montero.app.service.SesionService;
import com.montero.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/ajustes")
public class AjustesController {

    private final SesionService sesionService;
    private final UsuarioService usuarioService;
    private final PerfilUsuarioService perfilUsuarioService;

    public AjustesController(SesionService sesionService, UsuarioService usuarioService,
                             PerfilUsuarioService perfilUsuarioService) {
        this.sesionService = sesionService;
        this.usuarioService = usuarioService;
        this.perfilUsuarioService = perfilUsuarioService;
    }

    @GetMapping
    public String verAjustes(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "ajustes_aplicacion/ajustes_global";
    }

    @GetMapping("/seguridad")
    public String verSeguridad(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        return "ajustes_aplicacion/seguridad_cuenta";
    }

    @GetMapping("/seguridad/contrasena")
    public String verCambiarContrasena(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("cambioPasswordDTO")) {
            model.addAttribute("cambioPasswordDTO", new CambioPasswordDTO());
        }
        return "ajustes_aplicacion/cambiar_contrasena";
    }

    @PostMapping("/seguridad/contrasena/actualizar")
    public String actualizarContrasena(@Valid @ModelAttribute("cambioPasswordDTO") CambioPasswordDTO dto,
                                       BindingResult result,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "ajustes_aplicacion/cambiar_contrasena";
        }

        try {
            usuarioService.cambiarPassword(usuario.getId(), dto.getPasswordActual(),
                    dto.getPasswordNueva(), dto.getPasswordConfirmacion());
            redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/ajustes/seguridad/contrasena";
        }

        return "redirect:/ajustes/seguridad";
    }

    @GetMapping("/seguridad/sesiones")
    public String verSesiones(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        SesionUsuario sesionActual = sesionService.obtenerSesionActual(session.getId());
        List<SesionUsuario> otrasSesiones = sesionService.obtenerSesionesActivas(usuario.getId()).stream()
                .filter(s -> !s.getSessionId().equals(session.getId()))
                .toList();
        List<SesionUsuario> actividadReciente = sesionService.obtenerActividadReciente(usuario.getId());

        model.addAttribute("sesionActual", sesionActual != null ? sesionActual : new SesionUsuario());
        model.addAttribute("otrasSesiones", otrasSesiones);
        model.addAttribute("actividadReciente", actividadReciente);

        return "ajustes_aplicacion/sesiones_dispositivos";
    }

    @PostMapping("/seguridad/sesiones/{id}/cerrar")
    public String cerrarSesion(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        sesionService.cerrarSesion(id, usuario.getId());
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesión cerrada.");
        return "redirect:/ajustes/seguridad/sesiones";
    }

    @PostMapping("/seguridad/sesiones/cerrar-todas")
    public String cerrarTodasSesiones(HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        sesionService.cerrarTodasExcepto(session.getId(), usuario.getId());
        redirectAttributes.addFlashAttribute("mensajeExito", "Sesiones cerradas en los demás dispositivos.");
        return "redirect:/ajustes/seguridad/sesiones";
    }

    @GetMapping("/preferencias")
    public String verPreferencias(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        PerfilUsuarioDTO dto = perfilUsuarioService.obtenerPerfilPorUsuarioId(usuario.getId());
        if (dto == null) {
            return "redirect:/login";
        }
        model.addAttribute("preferencias", dto);
        return "ajustes_aplicacion/preferencias_viaje";
    }

    @PostMapping("/preferencias/actualizar")
    public String actualizarPreferencias(@RequestParam("preferenciaAsiento") String preferenciaAsiento,
                                          @RequestParam("preferenciaServicio") String preferenciaServicio,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        boolean actualizado = perfilUsuarioService.actualizarPreferencias(usuario.getId(), preferenciaAsiento, preferenciaServicio);

        if (actualizado) {
            Usuario usuarioActualizado = perfilUsuarioService.obtenerUsuarioActualizado(usuario.getId());
            session.setAttribute("usuarioLogueado", usuarioActualizado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Preferencias actualizadas correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudieron actualizar las preferencias.");
        }

        return "redirect:/ajustes/preferencias";
    }
}