package com.montero.app.config;

import com.montero.app.model.Usuario;
import com.montero.app.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (usuarioRepository.findByEmail("admin@montero.com").isEmpty()) {

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail("admin@montero.com");
            admin.setPassword(passwordEncoder.encode("Admin123*"));
            admin.setRol("ADMIN");
            admin.setActivo(true);

            usuarioRepository.save(admin);

            System.out.println("Administrador creado.");
        }

    }
}