package com.vitalpet.msappointments.controller;

import com.vitalpet.msappointments.dto.AppointmentRequestDTO;
import com.vitalpet.msappointments.dto.AppointmentResponseDTO;
import com.vitalpet.msappointments.dto.AppointmentStatusRequestDTO;
import com.vitalpet.msappointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAll() {
        return ResponseEntity.ok(appointmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    // @Valid revisa las restricciones del dto antes de entrar al servicio
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(dto));
    }

    // confirmar, cancelar o marcar realizada, igual que los botones del panel
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDTO> changeStatus(@PathVariable Long id,
                                                               @Valid @RequestBody AppointmentStatusRequestDTO dto) {
        return ResponseEntity.ok(appointmentService.changeStatus(id, dto.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // listar citas por dueno
    @GetMapping("/{id}/owner")
    public ResponseEntity<List<AppointmentResponseDTO>> getByOwnerId(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getByOwnerId(id));
    }

    // verificar si la cita existe
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> appointmentExists(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.appointmentExists(id));
    }
}
