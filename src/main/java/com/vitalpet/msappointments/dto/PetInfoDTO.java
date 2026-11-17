package com.vitalpet.msappointments.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// solo los campos de la mascota que necesita citas (respuesta de ms-pets)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetInfoDTO {
    private Long id;
    private String name;
    private boolean active;
    private Long ownerId;
}
