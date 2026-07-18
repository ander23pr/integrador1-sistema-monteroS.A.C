package com.montero.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.montero.app.dto.PagoYapeDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.MetodoPago;
import com.montero.app.model.Pago;
import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.repository.PagoRepository;
import com.montero.app.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private PagoService pagoService;

    private Reserva reservaPendiente;
    private Viaje viaje;

    @BeforeEach
    void setUp() {
        viaje = new Viaje(1L, "Piura", "Paita", LocalDate.now().plusDays(1), LocalTime.of(8, 0), BigDecimal.valueOf(50), 30);
        reservaPendiente = new Reserva();
        reservaPendiente.setId(1L);
        reservaPendiente.setViaje(viaje);
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);
        reservaPendiente.setPrecioTotal(BigDecimal.valueOf(100));
        reservaPendiente.setNumerosAsientos("1,2");
        reservaPendiente.setNombresPasajero("Juan");
        reservaPendiente.setApellidosPasajero("Pérez");
    }

    @Test
    @DisplayName("Procesar pago con Yape exitosamente")
    void deberiaProcesarPagoYape() {
        PagoYapeDTO dto = new PagoYapeDTO();
        dto.setNumeroTelefono("987654321");
        dto.setCodigoAprobacion("123456");

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pago resultado = pagoService.procesarPagoYape(1L, dto);

        assertNotNull(resultado);
        assertEquals(MetodoPago.YAPE, resultado.getMetodoPago());
        assertEquals("987654321", resultado.getNumeroTelefono());
        assertEquals("123456", resultado.getCodigoAprobacion());
        assertEquals(EstadoReserva.PAGADO, reservaPendiente.getEstado());
        verify(pagoRepository).save(any(Pago.class));
        verify(reservaRepository).save(reservaPendiente);
        verify(notificacionService).crearNotificacionConfirmacionReserva(reservaPendiente);
        verify(notificacionService).crearNotificacionNuevaVenta(eq(reservaPendiente), eq(MetodoPago.YAPE));
    }

    @Test
    @DisplayName("Procesar pago con Tarjeta exitosamente")
    void deberiaProcesarPagoTarjeta() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pago resultado = pagoService.procesarPagoTarjeta(1L, "4111111111111111", "12/28", "123", "Juan Pérez");

        assertNotNull(resultado);
        assertEquals(MetodoPago.TARJETA, resultado.getMetodoPago());
        assertEquals("Juan Pérez", resultado.getNombreTitular());
        assertNotNull(resultado.getCodigoAprobacion());
        assertEquals(EstadoReserva.PAGADO, reservaPendiente.getEstado());
        verify(notificacionService).crearNotificacionNuevaVenta(eq(reservaPendiente), eq(MetodoPago.TARJETA));
    }

    @Test
    @DisplayName("Procesar pago en Efectivo exitosamente")
    void deberiaProcesarPagoEfectivo() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pago resultado = pagoService.procesarPagoEfectivo(1L, "cliente@example.com");

        assertNotNull(resultado);
        assertEquals(MetodoPago.PAGO_EFECTIVO, resultado.getMetodoPago());
        assertEquals("cliente@example.com", resultado.getCorreo());
        assertNotNull(resultado.getCodigoAprobacion());
        assertEquals(EstadoReserva.PAGADO, reservaPendiente.getEstado());
        verify(notificacionService).crearNotificacionNuevaVenta(eq(reservaPendiente), eq(MetodoPago.PAGO_EFECTIVO));
    }

    @Test
    @DisplayName("Procesar pago con QR exitosamente")
    void deberiaProcesarPagoQR() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pago resultado = pagoService.procesarPagoQR(1L);

        assertNotNull(resultado);
        assertEquals(MetodoPago.PAGO_QR, resultado.getMetodoPago());
        assertNotNull(resultado.getCodigoAprobacion());
        assertEquals(EstadoReserva.PAGADO, reservaPendiente.getEstado());
        verify(notificacionService).crearNotificacionNuevaVenta(eq(reservaPendiente), eq(MetodoPago.PAGO_QR));
    }

    @Test
    @DisplayName("Rechazar pago cuando la reserva ya está pagada")
    void deberiaRechazarPagoSiReservaYaEstaPagada() {
        reservaPendiente.setEstado(EstadoReserva.PAGADO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));

        PagoYapeDTO dto = new PagoYapeDTO();
        assertThrows(IllegalStateException.class, () -> pagoService.procesarPagoYape(1L, dto));
    }

    @Test
    @DisplayName("Rechazar pago con tarjeta cuando el número es inválido")
    void deberiaRechazarTarjetaInvalida() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.procesarPagoTarjeta(1L, "123", "12/28", "123", "Juan"));
    }

    @Test
    @DisplayName("Rechazar pago en efectivo con correo inválido")
    void deberiaRechazarCorreoInvalidoEnEfectivo() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaPendiente));

        assertThrows(IllegalArgumentException.class,
                () -> pagoService.procesarPagoEfectivo(1L, "correo-invalido"));
    }
}
