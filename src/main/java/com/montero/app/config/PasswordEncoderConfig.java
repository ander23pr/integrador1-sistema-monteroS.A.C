package com.montero.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuración de BCryptPasswordEncoder para el cifrado de contraseñas.
 * Esta configuración proporciona un bean de BCrypt sin activar Spring Security completo.
 * Se utiliza únicamente para hashear y validar contraseñas de usuarios.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Crea un bean de BCryptPasswordEncoder.
     * BCrypt es un algoritmo seguro para cifrado de contraseñas que:
     * - Incorpora salt automáticamente
     * - Es resistente a ataques de fuerza bruta
     * - Cada hash es único aunque la contraseña sea idéntica
     * 
     * @return BCryptPasswordEncoder configurado con fortaleza 12
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // Fortaleza 12: balanza entre seguridad y tiempo de procesamiento
        return new BCryptPasswordEncoder(12);
    }
}
