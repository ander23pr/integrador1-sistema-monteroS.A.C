package com.montero.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación del sistema
 */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String mensaje;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean leida = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boleto_id", nullable = true)
    private Reserva boleto;

    public Notificacion() {
    }

    public Notificacion(Usuario usuario, String mensaje, String descripcion, 
                       TipoNotificacion tipo, Reserva boleto) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.boleto = boleto;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    // Getters y Setters
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public Reserva getBoleto() {
        return boleto;
    }

    public void setBoleto(Reserva boleto) {
        this.boleto = boleto;
    }
}
