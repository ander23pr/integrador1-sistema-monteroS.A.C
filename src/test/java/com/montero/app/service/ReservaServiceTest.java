package com.montero.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.montero.app.dto.ReservaRequestDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Reserva;
import com.montero.app.model.Viaje;
import com.montero.app.repository.ReservaRepository;

/**
 * Pruebas unitarias para ReservaService
 * Cubre casos críticos de creación de reservas y validaciones
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ViajeService viajeService;

    @InjectMocks
    private ReservaService reservaService;

    private ReservaRequestDTO reservaDTO;
    private Viaje viajeValido;
    private Reserva reservaMock;

    @BeforeEach
    void setUp() {
        // Viaje válido con fecha futura y 30 asientos disponibles
        viajeValido = new Viaje(
                1L,
                "Piura",
                "Paita",
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                BigDecimal.valueOf(50.00),
                30
        );

        // DTO básico para pruebas
        reservaDTO = new ReservaRequestDTO();
        reservaDTO.setViajeId(1L);
        reservaDTO.setNumerosAsientos(Arrays.asList(1, 2));
        reservaDTO.setOrigenSeleccionado("Piura");
        reservaDTO.setDestinoSeleccionado("Paita");
        reservaDTO.setFechaSeleccionada(LocalDate.now().plusDays(1));
        reservaDTO.setNombresPasajero("Juan");
        reservaDTO.setApellidosPasajero("Pérez");
        reservaDTO.setDniPasajero("12345678");
        reservaDTO.setEmailPasajero("juan@example.com");

        // Mock de reserva retornada por el repositorio
        reservaMock = new Reserva();
        reservaMock.setId(1L);
        reservaMock.setViaje(viajeValido);
        reservaMock.setEstado(EstadoReserva.PENDIENTE);
        reservaMock.setNumerosAsientos("1,2");
    }

    @Test
    @DisplayName("Crear reserva exitosa con datos válidos")
    void testCrearReservaExitosa() {
        // Arrange: Preparar datos válidos
        when(viajeService.obtenerViajePorId(1L)).thenReturn(viajeValido);
        when(viajeService.obtenerAsientosOcupados(1L)).thenReturn(Collections.emptyList());
        when(reservaRepository.findByViajeId(1L)).thenReturn(Collections.emptyList());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        // Act: Ejecutar la creación de reserva
        Reserva resultado = reservaService.iniciarReserva(reservaDTO);

        // Assert: Verificar que la reserva se creó correctamente
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        assertEquals("1,2", resultado.getNumerosAsientos());
    }

    @Test
    @DisplayName("Validar error cuando no hay asientos seleccionados")
    void testValidarAsientosVacios() {
        // Arrange: DTO sin asientos seleccionados
        reservaDTO.setNumerosAsientos(null);
        when(viajeService.obtenerViajePorId(1L)).thenReturn(viajeValido);

        // Act & Assert: Debe lanzar IllegalArgumentException
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.iniciarReserva(reservaDTO),
                "Debe lanzar excepción cuando no hay asientos seleccionados"
        );
        assertTrue(excepcion.getMessage().contains("seleccionar al menos un asiento"));
    }

    @Test
    @DisplayName("Validar error cuando DNI está vacío")
    void testValidarDniVacio() {
        // Arrange: DTO con DNI en blanco
        reservaDTO.setDniPasajero("");

        // Act & Assert: Debe lanzar IllegalArgumentException
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.iniciarReserva(reservaDTO),
                "Debe lanzar excepción cuando DNI está vacío"
        );
        assertTrue(excepcion.getMessage().contains("DNI del pasajero es obligatorio"));
    }

    @Test
    @DisplayName("Validar error cuando se intenta reservar más de 5 asientos")
    void testValidarLimiteMaximoAsientos() {
        // Arrange: Seleccionar 6 asientos (máximo permitido: 5)
        reservaDTO.setNumerosAsientos(Arrays.asList(1, 2, 3, 4, 5, 6));
        when(viajeService.obtenerViajePorId(1L)).thenReturn(viajeValido);

        // Act & Assert: Debe lanzar IllegalArgumentException
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.iniciarReserva(reservaDTO),
                "Debe lanzar excepción cuando se superan 5 asientos"
        );
        assertTrue(excepcion.getMessage().contains("No se pueden reservar más de 5 asientos"));
    }

    @Test
    @DisplayName("Validar error cuando se intenta reservar asiento ocupado")
    void testValidarAsientosOcupados() {
        // Arrange: El asiento 1 está ocupado
        when(viajeService.obtenerViajePorId(1L)).thenReturn(viajeValido);
        when(viajeService.obtenerAsientosOcupados(1L)).thenReturn(Arrays.asList(1));

        // Act & Assert: Debe lanzar IllegalStateException
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> reservaService.iniciarReserva(reservaDTO),
                "Debe lanzar excepción cuando se intenta reservar asiento ocupado"
        );
        assertTrue(excepcion.getMessage().contains("ya se encuentran ocupados"));
    }

    @Test
    @DisplayName("Validar error cuando el viaje no existe")
    void testValidarViajeInexistente() {
        // Arrange: Viaje no existe, ViajeService lanza excepción
        when(viajeService.obtenerViajePorId(999L)).thenThrow(
                new IllegalArgumentException("Viaje no encontrado con ID: 999")
        );
        reservaDTO.setViajeId(999L);

        // Act & Assert: Debe propagar la excepción del ViajeService
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.iniciarReserva(reservaDTO),
                "Debe lanzar excepción cuando el viaje no existe"
        );
        assertTrue(excepcion.getMessage().contains("Viaje no encontrado"));
    }

    @Test
    @DisplayName("Obtener historial de reservas del usuario autenticado")
    void testObtenerHistorialPorUsuario() {

        // Arrange
        Viaje viaje = new Viaje(
                1L,
                "Piura",
                "Paita",
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                BigDecimal.valueOf(50.00),
                30
        );

        Reserva reservaUsuario = new Reserva();
        reservaUsuario.setId(10L);
        reservaUsuario.setEstado(EstadoReserva.PAGADO);
        reservaUsuario.setViaje(viaje);
        reservaUsuario.setPrecioTotal(BigDecimal.valueOf(50.00));

        when(reservaRepository.findByUsuarioId(7L))
                .thenReturn(Collections.singletonList(reservaUsuario));

        // Act
        var resultado = reservaService.obtenerHistorialPorUsuario(
                7L,
                null,           // búsqueda
                "todos",        // filtro
                "fecha_desc"    // orden
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        assertEquals("Piura", resultado.get(0).getViaje().getOrigen());
        assertEquals("Paita", resultado.get(0).getViaje().getDestino());
        assertEquals(EstadoReserva.PAGADO, resultado.get(0).getEstado());
    }

    @Test
    @DisplayName("Buscar reservas ignorando acentos y mayúsculas")
    void testBuscarReservasIgnorandoAcentosYMayusculas() {
        Viaje viaje = new Viaje(
                2L,
                "Máncora",
                "Lima",
                LocalDate.of(2024, 7, 15),
                LocalTime.of(10, 30),
                BigDecimal.valueOf(80.00),
                25
        );

        Reserva reservaUsuario = new Reserva();
        reservaUsuario.setId(11L);
        reservaUsuario.setEstado(EstadoReserva.PAGADO);
        reservaUsuario.setViaje(viaje);
        reservaUsuario.setPrecioTotal(BigDecimal.valueOf(80.00));

        when(reservaRepository.findByUsuarioId(7L))
                .thenReturn(Collections.singletonList(reservaUsuario));

        var resultado = reservaService.obtenerHistorialPorUsuario(7L, "mancora", "todos", "fecha_desc");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(11L, resultado.get(0).getId());
    }

    @Test
    @DisplayName("Buscar reservas por nombres de mes y año en español")
    void testBuscarReservasPorMesYAnioEnEspanol() {
        Viaje viaje = new Viaje(
                3L,
                "Arequipa",
                "Cusco",
                LocalDate.of(2024, 7, 18),
                LocalTime.of(14, 0),
                BigDecimal.valueOf(120.00),
                20
        );

        Reserva reservaUsuario = new Reserva();
        reservaUsuario.setId(12L);
        reservaUsuario.setEstado(EstadoReserva.PAGADO);
        reservaUsuario.setViaje(viaje);
        reservaUsuario.setPrecioTotal(BigDecimal.valueOf(120.00));

        when(reservaRepository.findByUsuarioId(7L))
                .thenReturn(Collections.singletonList(reservaUsuario));

        var resultado = reservaService.obtenerHistorialPorUsuario(7L, "julio", "todos", "fecha_desc");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(12L, resultado.get(0).getId());
    }
}
