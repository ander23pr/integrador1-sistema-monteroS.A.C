package com.montero.app.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.montero.app.dto.LoginRequestDTO;
import com.montero.app.dto.RegistroRequestDTO;
import com.montero.app.model.Usuario;
import com.montero.app.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private NotificacionService notificacionService;


    public boolean registrarUsuario(RegistroRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            logger.warn("Intento de registro con email ya existente: {}", dto.getEmail());
            return false;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(dto.getEmail());
        // Cifro la contraseña usando BCrypt antes de almacenarla en la base de datos
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setRol("USER");

        usuarioRepository.save(nuevoUsuario);
        notificacionService.crearNotificacionNuevoUsuario(dto.getEmail());
        logger.info("Nuevo usuario registrado exitosamente: {}", dto.getEmail());
        return true;
    }

    public Usuario autenticar(LoginRequestDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Valido la contraseña usando matches() para comparar con el hash almacenado
            if (passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
                logger.info("Autenticación exitosa para usuario: {}", dto.getEmail());
                return usuario;
            }
            logger.warn("Intento de login fallido (contraseña incorrecta) para: {}", dto.getEmail());
        } else {
            logger.warn("Intento de login con email no registrado: {}", dto.getEmail());
        }
        return null;
    }

    public boolean cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva, String passwordConfirmacion) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            logger.warn("Intento de cambio de contraseña para usuario inexistente ID: {}", usuarioId);
            throw new IllegalArgumentException("Usuario no encontrado.");
        }

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            logger.warn("Intento de cambio de contraseña con contraseña actual incorrecta para usuario: {}", usuario.getEmail());
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
        }

        if (!passwordNueva.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos una mayúscula.");
        }
        if (!passwordNueva.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos un número.");
        }
        if (!passwordNueva.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("La nueva contraseña debe contener al menos un carácter especial.");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        logger.info("Contraseña actualizada exitosamente para usuario: {}", usuario.getEmail());
        return true;
    }
}
