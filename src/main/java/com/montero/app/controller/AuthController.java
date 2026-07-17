package com.montero.app.controller;

import com.montero.app.dto.LoginRequestDTO;
import com.montero.app.dto.RegistroRequestDTO;
import com.montero.app.model.Usuario;
import com.montero.app.service.SesionService;
import com.montero.app.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SesionService sesionService;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginRequestDTO());
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginDTO") LoginRequestDTO loginDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                jakarta.servlet.http.HttpSession session,
                                HttpServletRequest request) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        Usuario usuario = usuarioService.autenticar(loginDTO);
        if (usuario == null) {
            sesionService.registrarSesion(null, session.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());
            result.rejectValue("email", "error.loginDTO", "Credenciales incorrectas");
            return "auth/login";
        }

        session.setAttribute("usuarioLogueado", usuario);
        sesionService.registrarSesion(usuario, session.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());
        redirectAttributes.addFlashAttribute("mensajeExito", "¡Bienvenido de vuelta!");
        
        // Redirigir según el rol del usuario
        String rol = usuario.getRol() != null ? usuario.getRol() : "USER";
        if ("ADMIN".equals(rol)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/viajes";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", new RegistroRequestDTO());
        }
        return "auth/registro_usuario";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroRequestDTO registroDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro_usuario";
        }

        boolean registrado = usuarioService.registrarUsuario(registroDTO);
        if (!registrado) {
            result.rejectValue("email", "error.registroDTO", "El correo ya está registrado");
            return "auth/registro_usuario";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Registro exitoso. Inicia sesión.");
        return "redirect:/login";
    }

    @PostMapping("/auth/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario != null) {
            sesionService.cerrarSesionPorToken(session.getId(), usuario.getId());
        }
        session.invalidate();
        return "redirect:/login";
    }
}
