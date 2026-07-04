package com.montero.app.service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.montero.app.dto.ReservaRequestDTO;
import com.montero.app.model.EstadoReserva;
import com.montero.app.model.Reserva;
import com.montero.app.model.Usuario;
import com.montero.app.model.Viaje;
import com.montero.app.repository.ReservaRepository;

/**
 * Servicio encargado de la lógica de negocio de las Reservas.
 */
// Su función principal es validar, procesar y registrar una reserva antes de guardarla en la base de datos.

@Service
public class ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);
    private static final Map<String, String> MESES_ES = Map.ofEntries(
            Map.entry("enero", "01"),
            Map.entry("ene", "01"),
            Map.entry("febrero", "02"),
            Map.entry("feb", "02"),
            Map.entry("marzo", "03"),
            Map.entry("mar", "03"),
            Map.entry("abril", "04"),
            Map.entry("abr", "04"),
            Map.entry("mayo", "05"),
            Map.entry("may", "05"),
            Map.entry("junio", "06"),
            Map.entry("jun", "06"),
            Map.entry("julio", "07"),
            Map.entry("jul", "07"),
            Map.entry("agosto", "08"),
            Map.entry("ago", "08"),
            Map.entry("septiembre", "09"),
            Map.entry("sep", "09"),
            Map.entry("setiembre", "09"),
            Map.entry("set", "09"),
            Map.entry("octubre", "10"),
            Map.entry("oct", "10"),
            Map.entry("noviembre", "11"),
            Map.entry("nov", "11"),
            Map.entry("diciembre", "12"),
            Map.entry("dic", "12")
    );

    private final ReservaRepository reservaRepository;
    private final ViajeService viajeService;

    public ReservaService(ReservaRepository reservaRepository, ViajeService viajeService) {
        this.reservaRepository = reservaRepository;
        this.viajeService = viajeService;
    }

    public Reserva iniciarReserva(ReservaRequestDTO dto) {
        return iniciarReserva(dto, null);
    }

    /**
     * Inicia una nueva reserva en estado PENDIENTE, validando la disponibilidad del asiento.
     */
    @Transactional
    public Reserva iniciarReserva(ReservaRequestDTO dto, Usuario usuario) {

        if (dto.getViajeId() == null) {
            throw new IllegalArgumentException("No se encontró el viaje seleccionado. Por favor recargue la página e intente nuevamente.");
        }

        if (dto.getDniPasajero() == null || dto.getDniPasajero().isBlank()) {
            throw new IllegalArgumentException("El DNI del pasajero es obligatorio.");
        }

        // 1. Obtener el viaje seleccionado y los asientos enviados desde el formulario
        Viaje viaje = viajeService.obtenerViajePorId(dto.getViajeId());
        List<Integer> asientosSeleccionados = dto.getNumerosAsientos();

        // 2. Validar que la fecha del viaje no sea en el pasado
        if (viaje.getFechaSalida().isBefore(LocalDate.now())) {
            logger.warn("Intento de reserva para viaje con fecha pasada. Viaje ID: {}, Fecha: {}", viaje.getId(), viaje.getFechaSalida());
            throw new IllegalArgumentException("No se puede reservar un viaje cuya fecha de salida ya ha pasado.");
        }

        // 3. Validar que el DNI sea únicamente numérico (aunque el DTO también lo valida)
        if (!StringUtils.isNumeric(dto.getDniPasajero())) {
            logger.warn("Intento de reserva con DNI inválido (contiene caracteres no numéricos): {}", dto.getDniPasajero());
            throw new IllegalArgumentException("El DNI debe contener solo números.");
        }

        // 4. Validar que el usuario haya seleccionado al menos un asiento
        if (asientosSeleccionados == null || asientosSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un asiento.");
        }

        // 5. Validar que no se seleccionen más de 5 asientos
        long asientosUnicos = asientosSeleccionados.stream().distinct().count();
        if (asientosUnicos > 5) {
            throw new IllegalArgumentException("No se pueden reservar más de 5 asientos por operación.");
        }

        // 6. Validar que los asientos existan dentro del rango permitido del bus
        boolean asientoFueraDeRango = asientosSeleccionados.stream()
                .anyMatch(numero -> numero == null || numero < 1 || numero > viaje.getAsientosTotales());
        if (asientoFueraDeRango) {
            throw new IllegalArgumentException("Uno o más asientos son inválidos o están fuera de rango.");
        }

        // 7. Consultar qué asientos ya están ocupados para ese viaje
        List<Integer> asientosOcupados = viajeService.obtenerAsientosOcupados(viaje.getId());

        // 8. Validar que los asientos seleccionados no estén reservados
        boolean hayAsientoOcupado = asientosSeleccionados.stream().anyMatch(asientosOcupados::contains);
        if (hayAsientoOcupado) {
            logger.warn("Intento de reserva con asientos ocupados. Viaje ID: {}, Asientos: {}", viaje.getId(), asientosSeleccionados);
            throw new IllegalStateException("Uno o más asientos seleccionados ya se encuentran ocupados. Por favor, seleccione otros.");
        }

        // 9. Validar límite máximo de reservas activas por pasajero
        String dniPasajero = dto.getDniPasajero();
        long reservasActivasDni = reservaRepository.findByViajeId(viaje.getId()).stream()
                .filter(r -> !r.getEstado().name().equals("CANCELADO"))
                .filter(r -> dniPasajero.equals(r.getDniPasajero()))
                .count();
        if (reservasActivasDni >= 5) {
            logger.warn("Límite de reservas alcanzado. Pasajero DNI: {}, Viaje ID: {}", dniPasajero, viaje.getId());
            throw new IllegalStateException("Se ha alcanzado el máximo de 5 reservas por pasajero para este viaje.");
        }

        // 10. Crear la nueva reserva con estado PENDIENTE y calcular el precio total
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);

         // 11. Asociar la reserva con el viaje seleccionado
        reserva.setViaje(viaje);

         // 12. Ordenar y almacenar los asientos seleccionados
        List<Integer> asientosOrdenados = asientosSeleccionados.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        reserva.setNumeroAsiento(asientosOrdenados.get(0));
        reserva.setNumerosAsientos(
                asientosOrdenados.stream().map(String::valueOf).collect(Collectors.joining(","))
        );

        // 13. Registrar el estado y fecha de creación de la reserva
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaCreacion(LocalDateTime.now());

        // 14. Calcular el precio total según la cantidad de asientos
        BigDecimal precioIda = viaje.getPrecio().multiply(BigDecimal.valueOf(asientosOrdenados.size()));
        reserva.setPrecioTotal(precioIda);

        // 15. Si hay viaje de retorno, validar y procesar
        if (dto.getViajeRetornoId() != null) {
            Viaje viajeRetorno = viajeService.obtenerViajePorId(dto.getViajeRetornoId());
            List<Integer> asientosRetorno = dto.getNumerosAsientosRetorno();

            if (asientosRetorno == null || asientosRetorno.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un asiento para el viaje de retorno.");
            }

            long asientosRetornoUnicos = asientosRetorno.stream().distinct().count();
            if (asientosRetornoUnicos > 5) {
                throw new IllegalArgumentException("No se pueden reservar más de 5 asientos por operación.");
            }

            boolean asientoRetornoFueraDeRango = asientosRetorno.stream()
                    .anyMatch(numero -> numero == null || numero < 1 || numero > viajeRetorno.getAsientosTotales());
            if (asientoRetornoFueraDeRango) {
                throw new IllegalArgumentException("Uno o más asientos de retorno son inválidos o están fuera de rango.");
            }

            List<Integer> asientosOcupadosRetorno = viajeService.obtenerAsientosOcupados(viajeRetorno.getId());
            boolean hayAsientoOcupadoRetorno = asientosRetorno.stream().anyMatch(asientosOcupadosRetorno::contains);
            if (hayAsientoOcupadoRetorno) {
                throw new IllegalStateException("Uno o más asientos de retorno ya se encuentran ocupados.");
            }

            reserva.setViajeRetorno(viajeRetorno);

            List<Integer> asientosRetornoOrdenados = asientosRetorno.stream()
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
            reserva.setNumerosAsientosRetorno(
                    asientosRetornoOrdenados.stream().map(String::valueOf).collect(Collectors.joining(","))
            );

            BigDecimal precioRetorno = viajeRetorno.getPrecio().multiply(BigDecimal.valueOf(asientosRetornoOrdenados.size()));
            reserva.setPrecioRetorno(precioRetorno);
            reserva.setPrecioTotal(precioIda.add(precioRetorno));
        }

        // 17. Guardar los datos del pasajero en la reserva
        reserva.setNombresPasajero(dto.getNombresPasajero());
        reserva.setApellidosPasajero(dto.getApellidosPasajero());
        reserva.setDniPasajero(dto.getDniPasajero());
        reserva.setEmailPasajero(dto.getEmailPasajero());

        // 18. Guardar finalmente la reserva en la base de datos
        Reserva reservaGuardada = reservaRepository.save(reserva);
        logger.info("Reserva creada exitosamente. Reserva ID: {}, DNI Pasajero: {}, Asientos: {}", 
                reservaGuardada.getId(), dniPasajero, reserva.getNumerosAsientos());
        return reservaGuardada;
    }

    /**
     * Obtiene el historial de reservas de un usuario autenticado, aplicando filtros y ordenamiento.
     */
    @Transactional(readOnly = true)
    public List<Reserva> obtenerHistorialPorUsuario(Long usuarioId, String busqueda, String filtro, String orden) {
        if (usuarioId == null) {
            return List.of();
        }

        List<Reserva> reservas = reservaRepository.findByUsuarioId(usuarioId);
        return reservas.stream()
                .filter(reserva -> coincideBusqueda(reserva, busqueda))
                .filter(reserva -> coincideFiltro(reserva, filtro))
                .sorted(obtenerComparador(orden))
                .collect(Collectors.toList());
    }

    private boolean coincideBusqueda(Reserva reserva, String busqueda) {
        if (StringUtils.isBlank(busqueda)) {
            return true;
        }

        String textoNormalizado = normalizarTexto(busqueda);
        Viaje viaje = reserva.getViaje();
        if (viaje == null) {
            return false;
        }

        String origen = normalizarTexto(viaje.getOrigen());
        String destino = normalizarTexto(viaje.getDestino());
        String fecha = normalizarTexto(viaje.getFechaSalida() != null ? viaje.getFechaSalida().toString() : "");
        String hora = normalizarTexto(viaje.getHoraSalida() != null ? viaje.getHoraSalida().toString() : "");
        String precio = normalizarTexto(String.valueOf(reserva.getPrecioTotal()));
        String estado = normalizarTexto(reserva.getEstado().name());

        boolean coincideTexto = origen.contains(textoNormalizado)
                || destino.contains(textoNormalizado)
                || fecha.contains(textoNormalizado)
                || hora.contains(textoNormalizado)
                || precio.contains(textoNormalizado)
                || estado.contains(textoNormalizado);

        if (coincideTexto) {
            return true;
        }

        return coincideConFecha(viaje.getFechaSalida(), textoNormalizado);
    }

    private boolean coincideConFecha(LocalDate fechaSalida, String textoNormalizado) {
        if (fechaSalida == null || StringUtils.isBlank(textoNormalizado)) {
            return false;
        }

        String texto = textoNormalizado;
        if (texto.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return fechaSalida.toString().contains(texto);
        }

        String mes = MESES_ES.get(texto);
        if (mes != null) {
            return fechaSalida.getMonthValue() == Integer.parseInt(mes);
        }

        if (texto.matches("\\d{4}")) {
            return String.valueOf(fechaSalida.getYear()).contains(texto);
        }

        if (texto.matches("\\d{1,2}")) {
            return String.valueOf(fechaSalida.getDayOfMonth()).contains(texto);
        }

        return false;
    }

    private String normalizarTexto(String texto) {
        if (StringUtils.isBlank(texto)) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    private boolean coincideFiltro(Reserva reserva, String filtro) {
        if (StringUtils.isBlank(filtro) || "todos".equalsIgnoreCase(filtro)) {
            return true;
        }

        if ("completados".equalsIgnoreCase(filtro)) {
            return reserva.getEstado() == EstadoReserva.PAGADO;
        }

        if ("cancelados".equalsIgnoreCase(filtro)) {
            return reserva.getEstado() == EstadoReserva.CANCELADO;
        }

        return true;
    }

    private Comparator<Reserva> obtenerComparador(String orden) {
        boolean ascendente = "fecha_asc".equalsIgnoreCase(orden);
        Comparator<Reserva> comparador = Comparator.comparing(
                reserva -> reserva.getViaje() != null ? reserva.getViaje().getFechaSalida() : LocalDate.MIN,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        comparador = comparador.thenComparing(
                reserva -> reserva.getViaje() != null ? reserva.getViaje().getHoraSalida() : LocalTime.MIDNIGHT,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        return ascendente ? comparador : comparador.reversed();
    }

    /**
     * Obtiene una reserva por su ID para mostrar el resumen.
     */
    public Reserva obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Intento de acceso a reserva no existente. Reserva ID: {}", id);
                    return new IllegalArgumentException("Reserva no encontrada con ID: " + id);
                });
    }

    @Transactional
    public void cancelarReserva(Long id) {
        Reserva reserva = obtenerReservaPorId(id);
        if (reserva.getEstado() == EstadoReserva.PAGADO) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya fue pagada.");
        }
        reserva.setEstado(EstadoReserva.CANCELADO);
        reservaRepository.save(reserva);
        logger.info("Reserva cancelada exitosamente. Reserva ID: {}", id);
    }
}
