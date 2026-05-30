package com.montero.app.repository;

import com.montero.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Útil para buscar un usuario por su email durante el login
    Optional<Usuario> findByEmail(String email);
}
