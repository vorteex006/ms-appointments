package com.vitalpet.msappointments.exception;

import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

// mismo formato de error que ms-pets (timestamp, status, error, message, details)
// el front lee error.response.data.message
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String error, String message, List<String> details) {
        ErrorResponseDTO body = new ErrorResponseDTO();
        body.setTimestamp(LocalDateTime.now());
        body.setStatus(status.value());
        body.setError(error);
        body.setMessage(message);
        body.setDetails(details);
        return new ResponseEntity<>(body, status);
    }

    // primera excepcion: 404 -> no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    // segunda excepcion: 400 -> faltan datos obligatorios (los de jakarta)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation Error", "El formulario contiene errores", errors);
    }

    // tercera excepcion: 400 -> json mal formado, o fecha u hora mal escrita
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponseDTO> handleMessageNotReadable(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON Request",
                "El formato de los datos enviados es incorrecto o hay tipos incompatibles", null);
    }

    // cuarta excepcion: 409 -> choque en la base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Database Conflict",
                "Conflicto con los datos. Es posible que este intentando guardar un registro duplicado.", null);
    }

    // quinta excepcion: 409 -> cambio de estado no permitido
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null);
    }

    // sexta excepcion: 400 -> reglas de negocio (ej: horario de atencion)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "Business Rule Violation", ex.getMessage(), null);
    }

    // septima excepcion: 503 -> ms-pets no responde (solo pasa si pets.validation.enabled=true)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(FeignException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "No se pudo validar la mascota: el servicio de mascotas no esta disponible.", null);
    }

    // error generico por si no atrapamos ningun otro
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ocurrio un error inesperado en el servidor.", null);
    }
}
