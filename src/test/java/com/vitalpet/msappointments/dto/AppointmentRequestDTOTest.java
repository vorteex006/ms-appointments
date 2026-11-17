package com.vitalpet.msappointments.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

// revisa las validaciones de entrada del dto (las que corren antes del servicio)
class AppointmentRequestDTOTest {

    private Validator validator;
    private AppointmentRequestDTO dto;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        dto = new AppointmentRequestDTO(5L, "Rocky", 2L, "Camila Rojas", "camila.rojas@gmail.com",
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Control", "Dra. Paula Nunez");
    }

    @Test
    void validDto_ShouldHaveNoViolations() {
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void pastDate_ShouldFail() {
        dto.setDate(LocalDate.now().minusDays(1));
        assertEquals(1, validator.validate(dto).size());
    }

    @Test
    void blankPetName_ShouldFail() {
        dto.setPetName("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void missingPetId_ShouldFail() {
        dto.setPetId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void invalidEmail_ShouldFail() {
        dto.setOwnerEmail("no-es-un-correo");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void statusRequest_ShouldOnlyAcceptKnownStates() {
        assertTrue(validator.validate(new AppointmentStatusRequestDTO("Confirmada")).isEmpty());
        assertFalse(validator.validate(new AppointmentStatusRequestDTO("Pendiente")).isEmpty());
        assertFalse(validator.validate(new AppointmentStatusRequestDTO("")).isEmpty());
    }
}
