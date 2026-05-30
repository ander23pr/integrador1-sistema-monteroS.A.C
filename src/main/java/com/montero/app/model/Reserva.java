package com.montero.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * La funcion principal del modelo es almacenar toda la información relacionada a una reserva realizada por un pasajero
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @Column(nullable = false)
    private Integer numeroAsiento;

    @Column(name = "numeros_asientos", nullable = false, length = 255)
    private String numerosAsientos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(nullable = false, length = 100)
    private String nombresPasajero;

    @Column(nullable = false, length = 100)
    private String apellidosPasajero;

    @Column(nullable = false, length = 20)
    private String dniPasajero;

    @Column(length = 100)
    private String emailPasajero;

    public Reserva() {
    }

    public Reserva(Long id, Usuario usuario, Viaje viaje, Integer numeroAsiento, String numerosAsientos,
                   EstadoReserva estado, LocalDateTime fechaCreacion, BigDecimal precioTotal,
                   String nombresPasajero, String apellidosPasajero, String dniPasajero, String emailPasajero) {
        this.id = id;
        this.usuario = usuario;
        this.viaje = viaje;
        this.numeroAsiento = numeroAsiento;
        this.numerosAsientos = numerosAsientos;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.precioTotal = precioTotal;
        this.nombresPasajero = nombresPasajero;
        this.apellidosPasajero = apellidosPasajero;
        this.dniPasajero = dniPasajero;
        this.emailPasajero = emailPasajero;
    }

    public List<Integer> getListaNumerosAsientos() {
        if (numerosAsientos == null || numerosAsientos.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(numerosAsientos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Viaje getViaje() {
        return viaje;
    }

    public void setViaje(Viaje viaje) {
        this.viaje = viaje;
    }

    public Integer getNumeroAsiento() {
        return numeroAsiento;
    }

    public void setNumeroAsiento(Integer numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public String getNumerosAsientos() {
        return numerosAsientos;
    }

    public void setNumerosAsientos(String numerosAsientos) {
        this.numerosAsientos = numerosAsientos;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public String getNombresPasajero() {
        return nombresPasajero;
    }

    public void setNombresPasajero(String nombresPasajero) {
        this.nombresPasajero = nombresPasajero;
    }

    public String getApellidosPasajero() {
        return apellidosPasajero;
    }

    public void setApellidosPasajero(String apellidosPasajero) {
        this.apellidosPasajero = apellidosPasajero;
    }

    public String getDniPasajero() {
        return dniPasajero;
    }

    public void setDniPasajero(String dniPasajero) {
        this.dniPasajero = dniPasajero;
    }

    public String getEmailPasajero() {
        return emailPasajero;
    }

    public void setEmailPasajero(String emailPasajero) {
        this.emailPasajero = emailPasajero;
    }
}
