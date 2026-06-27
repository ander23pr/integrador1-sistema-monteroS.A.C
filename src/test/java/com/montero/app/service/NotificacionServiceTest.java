package com.montero.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Notificacion;
import com.montero.app.model.Notificacion.TipoNotificacion;
import com.montero.app.model.Reserva;
import com.montero.app.model.Usuario;
import com.montero.app.model.Viaje;
import com.montero.app.repository.NotificacionRepository;
import com.montero.app.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuario;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Ana");

        Viaje viaje = new Viaje(10L, "Piura", "Máncora", LocalDate.now().plusDays(2), LocalTime.of(8, 0), BigDecimal.valueOf(50), 30);
        reserva = new Reserva();
        reserva.setId(99L);
        reserva.setUsuario(usuario);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.PAGADO);
        reserva.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("Crear notificación de confirmación cuando el pago se confirma por primera vez")
    void deberiaCrearNotificacionDeConfirmacionAlConfirmarPago() {
        when(notificacionRepository.existsByReservaIdAndTipo(99L, TipoNotificacion.CONFIRMACION_RESERVA)).thenReturn(false);

        notificacionService.crearNotificacionConfirmacionReserva(reserva);

        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("No duplicar la notificación de confirmación si ya existe una para la misma reserva")
    void noDeberiaDuplicarNotificacionDeConfirmacion() {
        when(notificacionRepository.existsByReservaIdAndTipo(99L, TipoNotificacion.CONFIRMACION_RESERVA)).thenReturn(true);

        notificacionService.crearNotificacionConfirmacionReserva(reserva);

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("No crear notificaciones para reservas sin usuario (checkout de invitado)")
    void noDeberiaCrearNotificacionParaReservaDeInvitado() {
        reserva.setUsuario(null);

        notificacionService.crearNotificacionConfirmacionReserva(reserva);

        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Contar notificaciones no leídas delega en el repositorio")
    void deberiaContarNoLeidasCorrectamente() {
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(3L);

        long resultado = notificacionService.contarNoLeidas(1L);

        assertTrue(resultado == 3L);
        verify(notificacionRepository).countByUsuarioIdAndLeidaFalse(eq(1L));
    }

    @Test
    @DisplayName("Marcar todas las notificaciones como leídas")
    void deberiaMarcarTodasLasNotificacionesComoLeidas() {
        Notificacion primera = new Notificacion();
        primera.setId(1L);
        primera.setUsuario(usuario);
        primera.setLeida(false);

        Notificacion segunda = new Notificacion();
        segunda.setId(2L);
        segunda.setUsuario(usuario);
        segunda.setLeida(false);

        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of(primera, segunda));

        notificacionService.marcarTodasComoLeidas(1L);

        assertTrue(primera.isLeida());
        assertTrue(segunda.isLeida());
        verify(notificacionRepository).saveAll(List.of(primera, segunda));
    }
}
