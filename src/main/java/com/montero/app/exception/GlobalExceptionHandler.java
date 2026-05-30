package com.montero.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Manejador global de excepciones para el sistema Montero.
 * Captura errores de negocio y de sistema para mostrar páginas de error amigables.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de lógica de negocio (IllegalStateException, IllegalArgumentException).
     * Ej: asiento ocupado, reserva no encontrada, límite de asientos.
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(Exception ex, Model model) {
        model.addAttribute("titulo", "Error de validación");
        model.addAttribute("mensaje", ex.getMessage());
        model.addAttribute("linkVolver", "/viajes");
        return "error_generico";
    }

    /**
     * Captura cualquier otro error no esperado del servidor.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("titulo", "Error inesperado");
        model.addAttribute("mensaje", "Ocurrió un problema en el sistema. Por favor intente nuevamente.");
        model.addAttribute("linkVolver", "/inicio");
        return "error_generico";
    }
}
