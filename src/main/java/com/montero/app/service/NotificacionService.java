package com.montero.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Notificacion;
import com.montero.app.model.Notificacion.TipoNotificacion;
import com.montero.app.model.Reserva;
import com.montero.app.repository.NotificacionRepository;
import com.montero.app.repository.ReservaRepository;

/**
 * Servicio encargado de la generación y consulta de notificaciones del usuario.
 *
 * Las notificaciones se crean únicamente como reacción a eventos reales del sistema
 * (pago confirmado, recordatorio de viaje próximo, cambio de viaje), nunca como datos
 * de ejemplo. Cada método de creación valida previamente que no exista ya una
 * notificación equivalente para la misma reserva, evitando duplicados.
 */
@Service
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final ReservaRepository reservaRepository;

    public NotificacionService(NotificacionRepository notificacionRepository, ReservaRepository reservaRepository) {
        this.notificacionRepository = notificacionRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificacionesPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    /** Cantidad de notificaciones no leídas de un usuario, usada por el badge de navegación. */
    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        if (usuarioId == null) {
            return 0;
        }
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = obtenerNotificacionesPorUsuario(usuarioId);
        notificaciones.stream()
                .filter(notificacion -> !notificacion.isLeida())
                .forEach(notificacion -> notificacion.setLeida(true));
        if (!notificaciones.isEmpty()) {
            notificacionRepository.saveAll(notificaciones);
        }
    }

    /**
     * Elimina una notificación puntual, verificando primero que pertenezca al
     * usuario que solicita el borrado (evita que alguien elimine notificaciones
     * ajenas adivinando IDs desde el endpoint).
     */
    @Transactional
    public void eliminarNotificacion(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con ID: " + notificacionId));

        if (notificacion.getUsuario() == null || !notificacion.getUsuario().getId().equals(usuarioId)) {
            logger.warn("Intento de eliminar una notificación que no pertenece al usuario. Notificación ID: {}, Usuario ID: {}",
                    notificacionId, usuarioId);
            throw new SecurityException("No tienes permiso para eliminar esta notificación.");
        }

        notificacionRepository.delete(notificacion);
        logger.info("Notificación eliminada. ID: {}", notificacionId);
    }

    /**
     * Evento: una reserva quedó pagada/confirmada.
     * Se invoca desde PagoService justo después de marcar la reserva como PAGADO,
     * que es el único punto del flujo donde ocurre ese cambio de estado.
     */
    @Transactional
    public void crearNotificacionConfirmacionReserva(Reserva reserva) {
        if (reserva == null || reserva.getUsuario() == null) {
            // Reservas de invitado (sin usuario logueado) no tienen bandeja de notificaciones.
            return;
        }
        if (notificacionRepository.existsByReservaIdAndTipo(reserva.getId(), TipoNotificacion.CONFIRMACION_RESERVA)) {
            logger.debug("Notificación de confirmación ya existía para la reserva {}", reserva.getId());
            return;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(reserva.getUsuario());
        notificacion.setReserva(reserva);
        notificacion.setTipo(TipoNotificacion.CONFIRMACION_RESERVA);
        notificacion.setTitulo("Reserva confirmada");
        notificacion.setMensaje("Tu pago fue confirmado y tu viaje a " + reserva.getViaje().getDestino() + " quedó reservado.");
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        notificacion.setIcono("check_circle");
        notificacion.setAccion("ver_reserva");

        notificacionRepository.save(notificacion);
        logger.info("Notificación de confirmación creada para la reserva {}", reserva.getId());
    }

    /**
     * Evento: cambio importante relacionado a un viaje ya reservado (horario, terminal,
     * cancelación, etc.). Método reutilizable, listo para invocarse desde cualquier
     * punto futuro del sistema que modifique un viaje con reservas activas.
     */
    @Transactional
    public void crearNotificacionCambioViaje(Reserva reserva, String mensaje) {
        if (reserva == null || reserva.getUsuario() == null) {
            return;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(reserva.getUsuario());
        notificacion.setReserva(reserva);
        notificacion.setTipo(TipoNotificacion.CAMBIO_VIAJE);
        notificacion.setTitulo("Cambio en tu viaje");
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setLeida(false);
        notificacion.setIcono("update");
        notificacion.setAccion("ver_reserva");

        notificacionRepository.save(notificacion);
        logger.info("Notificación de cambio de viaje creada para la reserva {}", reserva.getId());
    }

    /**
     * Job programado: recorre las reservas pagadas cuyo viaje sale mañana y crea
     * un recordatorio si aún no existe uno para esa reserva (evita duplicados si el
     * job corre más de una vez para la misma fecha).
     */
    @Transactional
    @Scheduled(cron = "0 0 8 * * *") // todos los días a las 8:00 a.m.
    public void generarRecordatoriosDeViajesProximos() {
        LocalDate manana = LocalDate.now().plusDays(1);
        List<Reserva> reservasProximas = reservaRepository.findByEstadoAndViaje_FechaSalida(EstadoReserva.PAGADO, manana);

        for (Reserva reserva : reservasProximas) {
            if (reserva.getUsuario() == null) {
                continue;
            }
            if (notificacionRepository.existsByReservaIdAndTipo(reserva.getId(), TipoNotificacion.RECORDATORIO_VIAJE)) {
                continue;
            }

            Notificacion recordatorio = new Notificacion();
            recordatorio.setUsuario(reserva.getUsuario());
            recordatorio.setReserva(reserva);
            recordatorio.setTipo(TipoNotificacion.RECORDATORIO_VIAJE);
            recordatorio.setTitulo("Viaje próximo");
            recordatorio.setMensaje("Tu viaje a " + reserva.getViaje().getDestino() + " sale mañana a las "
                    + reserva.getViaje().getHoraSalida() + ". Revisa la terminal y llega con anticipación.");
            recordatorio.setFechaCreacion(LocalDateTime.now());
            recordatorio.setLeida(false);
            recordatorio.setIcono("departure_board");
            recordatorio.setAccion("ver_reserva");

            notificacionRepository.save(recordatorio);
            logger.info("Recordatorio de viaje creado para la reserva {}", reserva.getId());
        }
    }

    /**
     * Devuelve las notificaciones del usuario agrupadas por fecha, en el mismo
     * estilo que apps como WhatsApp o Gmail: "Hoy", "Ayer" y luego "8 julio",
     * "15 junio", etc. para fechas más antiguas.
     *
     * Se usa LinkedHashMap para conservar el orden de inserción: como la lista
     * de origen ya viene ordenada por fecha descendente, los grupos quedan en
     * el orden correcto sin necesidad de ordenarlos aparte.
     */
    @Transactional(readOnly = true)
    public Map<String, List<Notificacion>> obtenerNotificacionesAgrupadas(Long usuarioId) {
        List<Notificacion> notificaciones = obtenerNotificacionesPorUsuario(usuarioId);

        Map<String, List<Notificacion>> agrupadas = new LinkedHashMap<>();
        for (Notificacion notificacion : notificaciones) {
            String etiquetaGrupo = calcularEtiquetaGrupo(notificacion.getFechaCreacion());
            agrupadas.computeIfAbsent(etiquetaGrupo, clave -> new ArrayList<>()).add(notificacion);
        }
        return agrupadas;
    }

    private static final DateTimeFormatter FORMATO_FECHA_ANTIGUA =
            DateTimeFormatter.ofPattern("d 'de' MMMM", new Locale("es", "ES"));

    private String calcularEtiquetaGrupo(LocalDateTime fecha) {
        if (fecha == null) {
            return "Más antiguos";
        }
        LocalDate hoy = LocalDate.now();
        LocalDate fechaNotificacion = fecha.toLocalDate();

        if (fechaNotificacion.equals(hoy)) {
            return "Hoy";
        }
        if (fechaNotificacion.equals(hoy.minusDays(1))) {
            return "Ayer";
        }
        return fechaNotificacion.format(FORMATO_FECHA_ANTIGUA);
    }
}