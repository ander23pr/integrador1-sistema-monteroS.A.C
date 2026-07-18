package com.montero.app.repository;

import com.montero.app.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Método paginado con orden descendente por ID
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol ORDER BY u.id DESC")
    Page<Usuario> findByRolOrdered(@Param("rol") String rol, Pageable pageable);
}
