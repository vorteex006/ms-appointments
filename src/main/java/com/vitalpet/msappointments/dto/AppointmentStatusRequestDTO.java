package com.vitalpet.msappointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// el nuevo estado al que se quiere mover la cita
// solo acepto estos tres, igual que los botones del panel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentStatusRequestDTO {

    @NotBlank(message = "Debe especificar el nuevo estado de la cita.")
    @Pattern(regexp = "Confirmada|Cancelada|Realizada",
            message = "El estado debe ser Confirmada, Cancelada o Realizada.")
    private String status;
}
