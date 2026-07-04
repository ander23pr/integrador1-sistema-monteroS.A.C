package com.montero.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasajeroRequestDTO {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 3, max = 100, message = "Los nombres deben tener entre 3 y 100 caracteres")
    private String nombresPasajero;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 3, max = 100, message = "Los apellidos deben tener entre 3 y 100 caracteres")
    private String apellidosPasajero;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener solo números")
    private String dniPasajero;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ingresar un email válido")
    private String emailPasajero;

    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoPasajero;

    public PasajeroRequestDTO() {
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

    public String getTelefonoPasajero() {
        return telefonoPasajero;
    }

    public void setTelefonoPasajero(String telefonoPasajero) {
        this.telefonoPasajero = telefonoPasajero;
    }
}
