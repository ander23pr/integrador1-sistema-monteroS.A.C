package com.montero.app.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montero.app.dto.PerfilUsuarioDTO;
import com.montero.app.model.Reserva;
import com.montero.app.model.Usuario;
import com.montero.app.repository.ReservaRepository;
import com.montero.app.repository.UsuarioRepository;

@Service
public class PerfilUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public PerfilUsuarioDTO obtenerPerfilPorUsuarioId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;

        PerfilUsuarioDTO dto = new PerfilUsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setDni(usuario.getDni());
        dto.setTelefono(usuario.getTelefono());
        dto.setPreferenciaAsiento(usuario.getPreferenciaAsiento());
        dto.setPreferenciaServicio(usuario.getPreferenciaServicio());
        dto.setFotoPerfil(usuario.getFotoPerfil());

        // Calcular estadísticas
        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && usuarioId.equals(r.getUsuario().getId()))
                .collect(Collectors.toList());

        dto.setViajesTotales(reservas.size());

        if (!reservas.isEmpty()) {
            String destinoFav = reservas.stream()
                    .collect(Collectors.groupingBy(r -> r.getViaje().getDestino(), Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            dto.setDestinoFavorito(destinoFav);
        } else {
            dto.setDestinoFavorito("N/A");
        }

        return dto;
    }

    public boolean actualizarPerfil(PerfilUsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getId()).orElse(null);
        if (usuario == null) return false;

        usuario.setNombre(dto.getNombre());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setPreferenciaAsiento(dto.getPreferenciaAsiento());
        usuario.setPreferenciaServicio(dto.getPreferenciaServicio());
        if (dto.getFotoPerfil() != null) {
            usuario.setFotoPerfil(dto.getFotoPerfil());
        }

        usuarioRepository.save(usuario);
        return true;
    }

    public Usuario obtenerUsuarioActualizado(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public boolean actualizarPreferencias(Long usuarioId, String preferenciaAsiento, String preferenciaServicio) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return false;
        usuario.setPreferenciaAsiento(preferenciaAsiento);
        usuario.setPreferenciaServicio(preferenciaServicio);
        usuarioRepository.save(usuario);
        return true;
    }

    public void actualizarFotoPerfil(Long id, String fotoUrl) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setFotoPerfil(fotoUrl);
            usuarioRepository.save(usuario);
        }
    }
}
