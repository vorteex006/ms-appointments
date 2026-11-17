package com.vitalpet.msappointments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

// datos que entran al crear una cita, con las validaciones de entrada
// la regla del horario (09:00 a 19:00) la revisa el servicio
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequestDTO {

    @NotNull(message = "Debe especificar el ID de la mascota.")
    private Long petId;

    @NotBlank(message = "El nombre de la mascota no puede estar vacio.")
    @Size(max = 100)
    private String petName;

    @NotNull(message = "Debe especificar el ID del dueno.")
    private Long ownerId;

    @NotBlank(message = "El nombre del dueno no puede estar vacio.")
    @Size(max = 100)
    private String ownerName;

    @NotBlank(message = "El correo del dueno no puede estar vacio.")
    @Email(message = "El correo del dueno no tiene un formato valido.")
    @Size(max = 150)
    private String ownerEmail;

    @NotNull(message = "La fecha de la cita no puede estar vacia.")
    @FutureOrPresent(message = "La fecha de la cita no puede ser anterior a hoy.")
    private LocalDate date;

    // el front manda "09:30"
    @NotNull(message = "La hora de la cita no puede estar vacia.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    @NotBlank(message = "El motivo de la cita no puede estar vacio.")
    @Size(max = 200)
    private String reason;

    @NotBlank(message = "Debe especificar el veterinario.")
    @Size(max = 100)
    private String vet;
}
