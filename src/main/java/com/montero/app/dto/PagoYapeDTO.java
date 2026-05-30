package com.montero.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para capturar y validar los datos de la simulación de pago con Yape.
 */
public class PagoYapeDTO {

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^9\\d{8}$", message = "El número debe empezar con 9 y tener 9 dígitos")
    private String numeroTelefono;

    @NotBlank(message = "El código de aprobación es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El código de aprobación debe tener exactamente 6 dígitos")
    private String codigoAprobacion;

    public PagoYapeDTO() {
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
}
