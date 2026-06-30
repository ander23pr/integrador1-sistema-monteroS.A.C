package com.montero.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_usuario")
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @Column(length = 100)
    private String nombreDispositivo;

    @Column(length = 45)
    private String ip;

    @Column(nullable = false)
    private boolean activa;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    private LocalDateTime fechaCierre;

    public SesionUsuario() {
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNombreDispositivo() {
        return nombreDispositivo;
    }

    public void setNombreDispositivo(String nombreDispositivo) {
        this.nombreDispositivo = nombreDispositivo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getFechaInicioFormateada() {
        if (fechaInicio == null) return "—";
        return fechaInicio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getIcono() {
        if (nombreDispositivo == null) return "devices";
        String lower = nombreDispositivo.toLowerCase();
        if (lower.contains("iphone") || lower.contains("ios") || lower.contains("ipad")) return "smartphone";
        if (lower.contains("android")) return "smartphone";
        if (lower.contains("mobile") || lower.contains("phone")) return "smartphone";
        if (lower.contains("tablet")) return "tablet";
        if (lower.contains("linux")) return "computer";
        if (lower.contains("mac") || lower.contains("macintosh")) return "computer";
        if (lower.contains("windows")) return "computer";
        return "devices";
    }
}