package com.montero.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad Pago para almacenar la información de los pagos simulados (ej. Yape).
 */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MetodoPago metodoPago;

    @Column(length = 20)
    private String numeroTelefono;

    @Column(length = 20)
    private String codigoAprobacion;

    @Column(length = 100)
    private String nombreTitular;

    @Column(length = 50)
    private String correo;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    public Pago() {
    }

    public Pago(Long id, Reserva reserva, MetodoPago metodoPago, String numeroTelefono,
                String codigoAprobacion, LocalDateTime fechaPago) {
        this.id = id;
        this.reserva = reserva;
        this.metodoPago = metodoPago;
        this.numeroTelefono = numeroTelefono;
        this.codigoAprobacion = codigoAprobacion;
        this.fechaPago = fechaPago;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getCodigoAprobacion() {
        return codigoAprobacion;
    }

    public void setCodigoAprobacion(String codigoAprobacion) {
        this.codigoAprobacion = codigoAprobacion;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
