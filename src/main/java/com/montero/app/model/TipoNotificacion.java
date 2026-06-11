package com.montero.app.model;

/**
 * Enum para clasificar los tipos de notificaciones
 */
public enum TipoNotificacion {
    COMPRA("Compra de boleto"),
    RECORDATORIO("Recordatorio de viaje"),
    CANCELACION("Cancelación de reserva"),
    PROMOCION("Promoción especial"),
    ALERTA("Alerta del servicio"),
    CONFIRMACION("Confirmación de reserva"),
    INFO("Información general");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
