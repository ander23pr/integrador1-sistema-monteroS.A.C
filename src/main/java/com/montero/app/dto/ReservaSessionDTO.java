package com.montero.app.dto;

import java.time.LocalDate;
import java.util.List;

public class ReservaSessionDTO {

    private ProgresoReserva progreso;

    private String origenBuscado;
    private String destinoBuscado;
    private LocalDate fechaIdaBuscada;
    private LocalDate fechaVueltaBuscada;

    private Long viajeIdaId;
    private List<Integer> asientosIda;

    private Long viajeVueltaId;
    private List<Integer> asientosVuelta;

    private String nombresPasajero;
    private String apellidosPasajero;
    private String dniPasajero;
    private String emailPasajero;

    public ReservaSessionDTO() {
        this.progreso = ProgresoReserva.BUSQUEDA_INICIAL;
    }

    public ProgresoReserva getProgreso() {
        return progreso;
    }

    public void setProgreso(ProgresoReserva progreso) {
        this.progreso = progreso;
    }

    public String getOrigenBuscado() {
        return origenBuscado;
    }

    public void setOrigenBuscado(String origenBuscado) {
        this.origenBuscado = origenBuscado;
    }

    public String getDestinoBuscado() {
        return destinoBuscado;
    }

    public void setDestinoBuscado(String destinoBuscado) {
        this.destinoBuscado = destinoBuscado;
    }

    public LocalDate getFechaIdaBuscada() {
        return fechaIdaBuscada;
    }

    public void setFechaIdaBuscada(LocalDate fechaIdaBuscada) {
        this.fechaIdaBuscada = fechaIdaBuscada;
    }

    public LocalDate getFechaVueltaBuscada() {
        return fechaVueltaBuscada;
    }

    public void setFechaVueltaBuscada(LocalDate fechaVueltaBuscada) {
        this.fechaVueltaBuscada = fechaVueltaBuscada;
    }

    public Long getViajeIdaId() {
        return viajeIdaId;
    }

    public void setViajeIdaId(Long viajeIdaId) {
        this.viajeIdaId = viajeIdaId;
    }

    public List<Integer> getAsientosIda() {
        return asientosIda;
    }

    public void setAsientosIda(List<Integer> asientosIda) {
        this.asientosIda = asientosIda;
    }

    public Long getViajeVueltaId() {
        return viajeVueltaId;
    }

    public void setViajeVueltaId(Long viajeVueltaId) {
        this.viajeVueltaId = viajeVueltaId;
    }

    public List<Integer> getAsientosVuelta() {
        return asientosVuelta;
    }

    public void setAsientosVuelta(List<Integer> asientosVuelta) {
        this.asientosVuelta = asientosVuelta;
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
