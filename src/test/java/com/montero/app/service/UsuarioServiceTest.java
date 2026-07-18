package com.montero.app.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.montero.app.dto.LoginRequestDTO;
import com.montero.app.dto.RegistroRequestDTO;
import com.montero.app.model.Usuario;
import com.montero.app.repository.UsuarioRepository;

/**
 * Pruebas unitarias para UsuarioService
 * Cubre casos críticos de registro y autenticación
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegistroRequestDTO registroDTO;
    private LoginRequestDTO loginDTO;
    private Usuario usuarioExistente;
    private String passwordHasheado;

    @BeforeEach
    void setUp() {
        // DTO para registro
        registroDTO = new RegistroRequestDTO();
        registroDTO.setEmail("nuevo@example.com");
        registroDTO.setPassword("password123");

        // DTO para login
        loginDTO = new LoginRequestDTO();
        loginDTO.setEmail("existente@example.com");
        loginDTO.setPassword("password123");

        // Usuario existente en la base de datos
        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setEmail("existente@example.com");
        usuarioExistente.setPassword("$2a$10$hashedPassword123");
        usuarioExistente.setRol("USER");

        // Hash simulado de BCrypt
        passwordHasheado = "$2a$10$hashedPassword123";
    }

    @Test
    @DisplayName("Registro de usuario exitoso con email único")
    void testRegistroUsuarioExitoso() {
        // Arrange: Email no existe en la BD
        when(usuarioRepository.findByEmail("nuevo@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn(passwordHasheado);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario(
                1L,
                "nuevo@example.com",
                passwordHasheado,
                "USER"
        ));

        // Act: Ejecutar registro
        boolean resultado = usuarioService.registrarUsuario(registroDTO);

        // Assert: Verificar que el registro fue exitoso
        assertTrue(resultado, "El registro debe retornar true cuando el email es único");
    }

    @Test
    @DisplayName("Validar error cuando se intenta registrar con email duplicado")
    void testRegistroConEmailDuplicado() {
        // Arrange: El email ya existe en la BD
        when(usuarioRepository.findByEmail("existente@example.com")).thenReturn(Optional.of(usuarioExistente));
        registroDTO.setEmail("existente@example.com");

        // Act: Intentar registrar con email duplicado
        boolean resultado = usuarioService.registrarUsuario(registroDTO);

        // Assert: Debe retornar false cuando el email ya existe
        assertFalse(resultado, "El registro debe retornar false cuando el email ya existe");
    }

    @Test
    @DisplayName("Autenticación exitosa con credenciales correctas (BCrypt)")
    void testAutenticacionExitosa() {
        // Arrange: Usuario existe y contraseña es correcta
        when(usuarioRepository.findByEmail("existente@example.com")).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches("password123", usuarioExistente.getPassword())).thenReturn(true);

        // Act: Ejecutar autenticación
        Usuario resultado = usuarioService.autenticar(loginDTO);

        // Assert: Verificar que se retorna el usuario autenticado
        assertNotNull(resultado, "Debe retornar un usuario cuando las credenciales son correctas");
        assertEquals("existente@example.com", resultado.getEmail());
        assertEquals("USER", resultado.getRol());
    }

    @Test
    @DisplayName("Autenticación fallida con contraseña incorrecta")
    void testAutenticacionConContraseñaIncorrecta() {
        // Arrange: Usuario existe pero la contraseña es incorrecta
        when(usuarioRepository.findByEmail("existente@example.com")).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches("passwordIncorrecta", usuarioExistente.getPassword())).thenReturn(false);
        loginDTO.setPassword("passwordIncorrecta");

        // Act: Intentar autenticar con contraseña incorrecta
        Usuario resultado = usuarioService.autenticar(loginDTO);

        // Assert: Debe retornar null cuando la contraseña es incorrecta
        assertNull(resultado, "Debe retornar null cuando la contraseña es incorrecta");
    }
}
