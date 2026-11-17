package com.vitalpet.msappointments.exception;

// el cambio choca con el estado actual de la cita (409)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
