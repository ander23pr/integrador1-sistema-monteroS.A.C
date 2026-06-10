package com.montero.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuración de BCryptPasswordEncoder para el cifrado de contraseñas.
 * Proporciona un bean para hashear y validar contraseñas de usuarios.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Crea un bean de BCryptPasswordEncoder.
     * * BCrypt es un algoritmo seguro que:
     * - Incorpora salt automáticamente.
     * - Es resistente a ataques de fuerza bruta.
     * - Genera hashes únicos incluso para contraseñas idénticas.
     * * @return BCryptPasswordEncoder configurado con una fortaleza de 12.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // Fortaleza 12: equilibrio óptimo entre seguridad y tiempo de procesamiento.
        return new BCryptPasswordEncoder(12);
    }
}