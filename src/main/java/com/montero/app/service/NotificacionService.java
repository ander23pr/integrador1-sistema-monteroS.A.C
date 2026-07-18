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
import com.montero.app.model.MetodoPago;
import com.montero.app.model.Notificacion;
import com.montero.app.model.Notificacion.TipoNotificacion;
import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.repository.NotificacionRepository;
import com.montero.app.repository.ReservaRepository;
import com.montero.app.repository.ViajeRepository;

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
    private final ViajeRepository viajeRepository;

    public NotificacionService(NotificacionRepository notificacionRepository,
                                ReservaRepository reservaRepository,
                                ViajeRepository viajeRepository) {
        this.notificacionRepository = notificacionRepository;
        this.reservaRepository = reservaRepository;
        this.viajeRepository = viajeRepository;
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

    // ===================== NOTIFICACIONES PARA ADMIN =====================

    private Notificacion crearNotificacionAdminBase(String titulo, String mensaje, String icono, String accion) {
        Notificacion n = new Notificacion();
        n.setDestinatarioRol("ADMIN");
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setFechaCreacion(LocalDateTime.now());
        n.setLeida(false);
        n.setIcono(icono);
        n.setAccion(accion);
        return n;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificacionesAdmin() {
        return notificacionRepository.findByDestinatarioRolOrderByFechaCreacionDesc("ADMIN");
    }

    @Transactional(readOnly = true)
    public long contarNoLeidasAdmin() {
        return notificacionRepository.countByDestinatarioRolAndLeidaFalse("ADMIN");
    }

    @Transactional
    public void marcarTodasComoLeidasAdmin() {
        List<Notificacion> notificaciones = obtenerNotificacionesAdmin();
        notificaciones.stream()
                .filter(n -> !n.isLeida())
                .forEach(n -> n.setLeida(true));
        if (!notificaciones.isEmpty()) {
            notificacionRepository.saveAll(notificaciones);
        }
    }

    @Transactional
    public void eliminarNotificacionAdmin(Long notificacionId) {
        Notificacion n = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada con ID: " + notificacionId));
        if (!"ADMIN".equals(n.getDestinatarioRol())) {
            throw new SecurityException("La notificación no pertenece al administrador.");
        }
        notificacionRepository.delete(n);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Notificacion>> obtenerNotificacionesAdminAgrupadas() {
        List<Notificacion> notificaciones = obtenerNotificacionesAdmin();
        Map<String, List<Notificacion>> agrupadas = new LinkedHashMap<>();
        for (Notificacion n : notificaciones) {
            String etiqueta = calcularEtiquetaGrupo(n.getFechaCreacion());
            agrupadas.computeIfAbsent(etiqueta, k -> new ArrayList<>()).add(n);
        }
        return agrupadas;
    }

    // --- Creación de notificaciones específicas ---

    @Transactional
    public void crearNotificacionNuevaVenta(Reserva reserva, MetodoPago metodo) {
        String destino = reserva.getViaje().getDestino();
        Notificacion n = crearNotificacionAdminBase(
                "Nueva venta",
                "Reserva #" + reserva.getId() + " pagada con " + metodo.name() + " — Viaje a " + destino + " por S/ " + reserva.getPrecioTotal(),
                "payments",
                "ver_venta"
        );
        n.setTipo(TipoNotificacion.NUEVA_VENTA);
        n.setReserva(reserva);
        notificacionRepository.save(n);
        logger.info("Notificación admin de nueva venta creada para reserva {}", reserva.getId());
    }

    @Transactional
    public void crearNotificacionCancelacion(Reserva reserva) {
        String destino = reserva.getViaje().getDestino();
        Notificacion n = crearNotificacionAdminBase(
                "Reserva cancelada",
                "Reserva #" + reserva.getId() + " fue cancelada — Viaje a " + destino,
                "cancel",
                "ver_cancelacion"
        );
        n.setTipo(TipoNotificacion.CANCELACION);
        n.setReserva(reserva);
        notificacionRepository.save(n);
        logger.info("Notificación admin de cancelación creada para reserva {}", reserva.getId());
    }

    @Transactional
    public void crearNotificacionNuevoUsuario(String email) {
        Notificacion n = crearNotificacionAdminBase(
                "Nuevo usuario registrado",
                "Se registró un nuevo usuario: " + email,
                "person_add",
                "ver_usuario"
        );
        n.setTipo(TipoNotificacion.NUEVO_USUARIO);
        notificacionRepository.save(n);
        logger.info("Notificación admin de nuevo usuario creada: {}", email);
    }

    @Transactional
    public void crearNotificacionPocaDisponibilidad(Viaje viaje, int disponibles) {
        Notificacion n = crearNotificacionAdminBase(
                "Disponibilidad crítica",
                "Viaje " + viaje.getOrigen() + " → " + viaje.getDestino() + " (" + viaje.getFechaSalida() + ") — Solo quedan " + disponibles + " asientos.",
                "airline_seat_recline_normal",
                "ver_viaje"
        );
        n.setTipo(TipoNotificacion.ALERTA);
        n.setReserva(null);
        notificacionRepository.save(n);
        logger.info("Notificación admin de poca disponibilidad creada para viaje {}", viaje.getId());
    }

    /**
     * Job programado: revisa los viajes de los próximos 7 días y alerta si
     * tienen 3 o menos asientos disponibles. Corre cada 2 horas.
     */
    @Transactional
    @Scheduled(cron = "0 0 */2 * * *") // cada 2 horas
    public void generarAlertasPocaDisponibilidad() {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(7);
        List<Viaje> viajes = viajeRepository.findByFechaSalidaBetweenOrderByFechaSalida(hoy, fin);

        for (Viaje viaje : viajes) {
            List<Reserva> reservas = reservaRepository.findByViajeId(viaje.getId());
            int vendidos = 0;
            for (Reserva r : reservas) {
                if (r.getEstado() != EstadoReserva.CANCELADO) {
                    vendidos += r.getListaNumerosAsientos().size();
                }
            }
            int disponibles = viaje.getAsientosTotales() - vendidos;
            if (disponibles <= 3 && disponibles > 0) {
                // Evitar duplicados: solo crear si no existe ya una alerta activa para este viaje
                boolean yaExiste = notificacionRepository.findAll().stream()
                        .anyMatch(n -> n.getTipo() == TipoNotificacion.ALERTA
                                && n.getReserva() != null
                                && n.getReserva().getViaje().getId().equals(viaje.getId())
                                && !n.isLeida());
                if (!yaExiste) {
                    crearNotificacionPocaDisponibilidad(viaje, disponibles);
                }
            }
        }
    }
}