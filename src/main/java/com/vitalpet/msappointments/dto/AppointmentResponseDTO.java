package com.vitalpet.msappointments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponseDTO {

    private Long id;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;

    private String reason;
    private String vet;
    private String status;
    private LocalDateTime createdAt;
}
