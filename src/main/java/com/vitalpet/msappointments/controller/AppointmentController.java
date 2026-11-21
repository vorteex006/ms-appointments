package com.vitalpet.msappointments.controller;

import com.vitalpet.msappointments.dto.AppointmentRequestDTO;
import com.vitalpet.msappointments.dto.AppointmentResponseDTO;
import com.vitalpet.msappointments.dto.AppointmentStatusRequestDTO;
import com.vitalpet.msappointments.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    // ---------------------------------------
    // Funciones auxiliares de autorizacion
    // ---------------------------------------

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isOwner(
            Authentication authentication,
            Long ownerId
    ) {
        Object principal = authentication.getPrincipal();

        return principal instanceof Long userId
                && userId.equals(ownerId);
    }

    // ---------------------------------------
    // Listar todas las citas - ADMIN
    // ---------------------------------------

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    // ---------------------------------------
    // Consultar una cita
    // Propietario o ADMIN
    // ---------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AppointmentResponseDTO appointment =
                appointmentService.getById(id);

        if (!isAdmin(authentication)
                && !isOwner(authentication, appointment.getOwnerId())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(appointment);
    }

    // ---------------------------------------
    // Crear cita
    // CLIENT: solo para si mismo
    // ADMIN: cualquier propietario valido
    // ---------------------------------------

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(
            @Valid @RequestBody AppointmentRequestDTO dto,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)) {

            if (!isOwner(authentication, dto.getOwnerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Evita que un CLIENT use el correo de otro usuario.
            Object details = authentication.getDetails();

            if (!(details instanceof String authenticatedEmail)
                    || !authenticatedEmail.equalsIgnoreCase(
                        dto.getOwnerEmail().trim()
                    )) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // AppointmentService mantiene las reglas:
        // horario, fecha, mascota activa y propietario real.
        AppointmentResponseDTO created =
                appointmentService.create(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // ---------------------------------------
    // Cambiar estado
    // ADMIN: reglas de negocio normales
    // CLIENT: solo cancelar su propia cita
    // ---------------------------------------

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequestDTO dto,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)) {

            AppointmentResponseDTO appointment =
                    appointmentService.getById(id);

            if (!isOwner(authentication, appointment.getOwnerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!"Cancelada".equals(dto.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(
                appointmentService.changeStatus(
                    id,
                    dto.getStatus()
                )
        );
    }

    // ---------------------------------------
    // Eliminar cita - solo ADMIN
    // ---------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------
    // Consultar citas de un propietario
    // Propietario o ADMIN
    // ---------------------------------------

    @GetMapping("/{id}/owner")
    public ResponseEntity<List<AppointmentResponseDTO>> getByOwnerId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (!isAdmin(authentication)
                && !isOwner(authentication, id)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                appointmentService.getByOwnerId(id)
        );
    }

    // ---------------------------------------
    // Verificar existencia - ADMIN
    // ---------------------------------------

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> appointmentExists(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appointmentService.appointmentExists(id)
        );
    }
}
