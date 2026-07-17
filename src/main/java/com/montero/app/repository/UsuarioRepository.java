package com.montero.app.repository;

import com.montero.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Útil para buscar un usuario por su email durante el login
    Optional<Usuario> findByEmail(String email);
    
    // Método para contar usuarios por rol
    long countByRol(String rol);
    
    // Método para obtener lista de usuarios por rol
    List<Usuario> findByRol(String rol);
}
