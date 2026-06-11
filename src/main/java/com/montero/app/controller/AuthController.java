package com.montero.app.controller;

import com.montero.app.dto.LoginRequestDTO;
import com.montero.app.dto.RegistroRequestDTO;
import com.montero.app.model.Usuario;
import com.montero.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginRequestDTO());
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginDTO") LoginRequestDTO loginDTO,
                                BindingResult result,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "login";
        }

        Usuario usuario = usuarioService.autenticar(loginDTO);
        if (usuario == null) {
            result.rejectValue("email", "error.loginDTO", "Credenciales incorrectas");
            return "login";
        }

        // Guardar usuarioId en sesión para notificaciones
        session.setAttribute("usuarioId", usuario.getId());

        redirectAttributes.addFlashAttribute("mensajeExito", "¡Bienvenido de vuelta!");
        return "redirect:/viajes";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registroDTO")) {
            model.addAttribute("registroDTO", new RegistroRequestDTO());
        }
        return "registro_usuario";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroRequestDTO registroDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "registro_usuario";
        }

        boolean registrado = usuarioService.registrarUsuario(registroDTO);
        if (!registrado) {
            result.rejectValue("email", "error.registroDTO", "El correo ya está registrado");
            return "registro_usuario";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Registro exitoso. Inicia sesión.");
        return "redirect:/login";
    }
}
