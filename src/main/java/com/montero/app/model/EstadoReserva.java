package com.montero.app.model;

/**
 * Enum que representa los estados posibles de una reserva en el sistema.
 */
public enum EstadoReserva {
    PENDIENTE, // Asiento seleccionado, esperando pago
    PAGADO,    // Pago completado exitosamente
    CANCELADO  // Flujo abortado o tiempo de pago expirado
}
