package com.montero.app.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.montero.app.dto.PerfilUsuarioDTO;
import com.montero.app.model.Usuario;
import com.montero.app.service.PerfilUsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil")
public class PerfilUsuarioController {

    @Autowired
    private PerfilUsuarioService perfilUsuarioService;

    @GetMapping
    public String verPerfil(Model model, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }
        
        PerfilUsuarioDTO perfilDTO = perfilUsuarioService.obtenerPerfilPorUsuarioId(usuarioSesion.getId());
        if (perfilDTO == null) {
            return "redirect:/login";
        }
        model.addAttribute("perfil", perfilDTO);
        return "perfil_usuario/perfil_usuario";
    }

    @GetMapping("/editar")
    public String mostrarFormularioEdicion(Model model, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        PerfilUsuarioDTO perfilDTO = perfilUsuarioService.obtenerPerfilPorUsuarioId(usuarioSesion.getId());
        if (perfilDTO == null) {
            return "redirect:/login";
        }
        model.addAttribute("perfil", perfilDTO);
        return "perfil_usuario/editar_perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute PerfilUsuarioDTO perfilDTO, 
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        perfilDTO.setId(usuarioSesion.getId());
        boolean actualizado = perfilUsuarioService.actualizarPerfil(perfilDTO);

        if (actualizado) {
            Usuario usuarioActualizado = perfilUsuarioService.obtenerUsuarioActualizado(usuarioSesion.getId());
            session.setAttribute("usuarioLogueado", usuarioActualizado);
            redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo actualizar el perfil.");
        }

        return "redirect:/perfil";
    }

    @PostMapping("/foto")
    public String actualizarFoto(@RequestParam("imagen") MultipartFile imagen, 
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }

        if (imagen != null && !imagen.isEmpty()) {
            try {
                String folder = "src/main/resources/static/uploads/";
                Path dirPath = Paths.get(folder);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                String fileName = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
                Path filePath = dirPath.resolve(fileName);
                Files.copy(imagen.getInputStream(), filePath);
                
                perfilUsuarioService.actualizarFotoPerfil(usuarioSesion.getId(), "/uploads/" + fileName);
                
                Usuario usuarioActualizado = perfilUsuarioService.obtenerUsuarioActualizado(usuarioSesion.getId());
                session.setAttribute("usuarioLogueado", usuarioActualizado);
                
                redirectAttributes.addFlashAttribute("mensajeExito", "Foto de perfil actualizada.");
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("mensajeError", "Error al subir la imagen.");
            }
        }

        return "redirect:/perfil";
    }
}
