package com.montero.app.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para capturar los datos del pasajero y los asientos seleccionados
 * desde el formulario de reservas (Etapa 3).
 */

// Aquí se almacenan temporalmente datos como el ID del viaje, los asientos seleccionados, 
// la ruta, la fecha y los datos personales del pasajero.
public class ReservaRequestDTO {

    @NotNull(message = "El viaje es obligatorio")
    private Long viajeId;

    @NotEmpty(message = "Debe seleccionar al menos un asiento")
    private List<Integer> numerosAsientos;

    private String origenSeleccionado;
    private String destinoSeleccionado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaSeleccionada;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombresPasajero;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidosPasajero;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dniPasajero;

    @Email(message = "Debe ingresar un email válido")
    private String emailPasajero;

    public ReservaRequestDTO() {
    }

    public Long getViajeId() {
        return viajeId;
    }

    public void setViajeId(Long viajeId) {
        this.viajeId = viajeId;
    }

    public List<Integer> getNumerosAsientos() {
        return numerosAsientos;
    }

    public void setNumerosAsientos(List<Integer> numerosAsientos) {
        this.numerosAsientos = numerosAsientos;
    }

    public String getOrigenSeleccionado() {
        return origenSeleccionado;
    }

    public void setOrigenSeleccionado(String origenSeleccionado) {
        this.origenSeleccionado = origenSeleccionado;
    }

    public String getDestinoSeleccionado() {
        return destinoSeleccionado;
    }

    public void setDestinoSeleccionado(String destinoSeleccionado) {
        this.destinoSeleccionado = destinoSeleccionado;
    }

    public LocalDate getFechaSeleccionada() {
        return fechaSeleccionada;
    }

    public void setFechaSeleccionada(LocalDate fechaSeleccionada) {
        this.fechaSeleccionada = fechaSeleccionada;
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
