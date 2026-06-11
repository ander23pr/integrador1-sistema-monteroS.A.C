package com.montero.app.service;

import com.montero.app.model.Notificacion;
import com.montero.app.model.Reserva;
import com.montero.app.model.TipoNotificacion;
import com.montero.app.model.Usuario;
import com.montero.app.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio que gestiona la lógica de notificaciones
 */
@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    /**
     * Crea una nueva notificación
     */
    @Transactional
    public Notificacion crearNotificacion(Usuario usuario, String mensaje, String descripcion,
                                         TipoNotificacion tipo, Reserva boleto) {
        Notificacion notificacion = new Notificacion(usuario, mensaje, descripcion, tipo, boleto);
        return notificacionRepository.save(notificacion);
    }

    /**
     * Crea una notificación sin boleto asociado
     */
    @Transactional
    public Notificacion crearNotificacion(Usuario usuario, String mensaje, String descripcion,
                                         TipoNotificacion tipo) {
        return crearNotificacion(usuario, mensaje, descripcion, tipo, null);
    }

    /**
     * Obtiene todas las notificaciones de un usuario ordenadas por fecha
     */
    public List<Notificacion> obtenerNotificacionesPorUsuario(Usuario usuario) {
        return notificacionRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    /**
     * Obtiene solo las notificaciones no leídas de un usuario
     */
    public List<Notificacion> obtenerNotificacionesNoLeidas(Usuario usuario) {
        return notificacionRepository.findByUsuarioAndLeidaFalseOrderByFechaDesc(usuario);
    }

    /**
     * Cuenta el total de notificaciones no leídas de un usuario
     */
    public Long contarNotificacionesNoLeidas(Usuario usuario) {
        return notificacionRepository.countByUsuarioAndLeidaFalse(usuario);
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public void marcarComoLeida(Long id) {
        notificacionRepository.marcarComoLeida(id);
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Transactional
    public void marcarTodasComoLeidas(Usuario usuario) {
        notificacionRepository.marcarTodasComoLeidas(usuario);
    }

    /**
     * Obtiene una notificación por ID
     */
    public Notificacion obtenerNotificacionPorId(Long id) {
        return notificacionRepository.findById(id).orElse(null);
    }

    /**
     * Elimina una notificación
     */
    @Transactional
    public void eliminarNotificacion(Long id) {
        notificacionRepository.deleteById(id);
    }
}
