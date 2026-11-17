package com.vitalpet.msappointments.service;

import com.vitalpet.msappointments.client.PetClient;
import com.vitalpet.msappointments.dto.AppointmentRequestDTO;
import com.vitalpet.msappointments.dto.AppointmentResponseDTO;
import com.vitalpet.msappointments.dto.PetInfoDTO;
import com.vitalpet.msappointments.exception.ConflictException;
import com.vitalpet.msappointments.exception.ResourceNotFoundException;
import com.vitalpet.msappointments.model.Appointment;
import com.vitalpet.msappointments.repository.AppointmentRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PetClient petClient;

    @InjectMocks private AppointmentService appointmentService;

    private Faker faker;
    private Appointment mockAppointment;
    private AppointmentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setPetId(5L);
        mockAppointment.setPetName(faker.dog().name());
        mockAppointment.setOwnerId(2L);
        mockAppointment.setOwnerName(faker.name().fullName());
        mockAppointment.setOwnerEmail("camila.rojas@gmail.com");
        mockAppointment.setDate(LocalDate.now().plusDays(1));
        mockAppointment.setTime(LocalTime.of(10, 0));
        mockAppointment.setReason("Control");
        mockAppointment.setVet("Dra. Paula Nunez");
        mockAppointment.setStatus("Pendiente");

        requestDTO = new AppointmentRequestDTO(
                mockAppointment.getPetId(),
                mockAppointment.getPetName(),
                mockAppointment.getOwnerId(),
                mockAppointment.getOwnerName(),
                mockAppointment.getOwnerEmail(),
                mockAppointment.getDate(),
                mockAppointment.getTime(),
                mockAppointment.getReason(),
                mockAppointment.getVet()
        );
    }

    // primero los get
    @Test
    void getAll_ShouldReturnAppointmentsOrdered() {
        when(appointmentRepository.findAllByOrderByDateAscTimeAsc()).thenReturn(List.of(mockAppointment));
        List<AppointmentResponseDTO> result = appointmentService.getAll();
        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findAllByOrderByDateAscTimeAsc();
    }

    @Test
    void getById_ShouldReturnAppointment_WhenExists() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        AppointmentResponseDTO result = appointmentService.getById(1L);
        assertEquals(1L, result.getId());
        assertEquals("Pendiente", result.getStatus());
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.getById(99L));
    }

    @Test
    void getByOwnerId_ShouldReturnOwnerAppointments() {
        when(appointmentRepository.findByOwnerIdOrderByDateAscTimeAsc(2L)).thenReturn(List.of(mockAppointment));
        List<AppointmentResponseDTO> result = appointmentService.getByOwnerId(2L);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getOwnerId());
    }

    @Test
    void appointmentExists_ShouldReturnTrue() {
        when(appointmentRepository.existsAppointmentById(1L)).thenReturn(true);
        assertTrue(appointmentService.appointmentExists(1L));
    }

    // crear
    @Test
    void create_ShouldSaveAsPendiente() {
        // aunque el dto traiga otra cosa, el servidor deja la cita en pendiente
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        AppointmentResponseDTO result = appointmentService.create(requestDTO);

        assertEquals(10L, result.getId());
        assertEquals("Pendiente", result.getStatus());
        assertEquals(5L, result.getPetId());
        assertEquals(2L, result.getOwnerId());
        verify(petClient, never()).getById(any());
    }

    @Test
    void create_ShouldThrow_WhenTimeBeforeOpening() {
        requestDTO.setTime(LocalTime.of(8, 0));
        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(requestDTO));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrow_WhenTimeAfterClosing() {
        requestDTO.setTime(LocalTime.of(20, 0));
        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(requestDTO));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_ShouldAccept_OpeningAndClosingHour() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        requestDTO.setTime(LocalTime.of(9, 0));
        assertDoesNotThrow(() -> appointmentService.create(requestDTO));

        requestDTO.setTime(LocalTime.of(19, 0));
        assertDoesNotThrow(() -> appointmentService.create(requestDTO));
    }

    // validacion contra ms-pets (activada)
    @Test
    void create_ShouldValidatePet_WhenValidationEnabled() {
        ReflectionTestUtils.setField(appointmentService, "petsValidationEnabled", true);
        when(petClient.getById(5L)).thenReturn(new PetInfoDTO(5L, "Rocky", true, 2L));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> appointmentService.create(requestDTO));
        verify(petClient, times(1)).getById(5L);
    }

    @Test
    void create_ShouldThrow_WhenPetBelongsToAnotherOwner() {
        ReflectionTestUtils.setField(appointmentService, "petsValidationEnabled", true);
        when(petClient.getById(5L)).thenReturn(new PetInfoDTO(5L, "Rocky", true, 99L));

        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(requestDTO));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrow_WhenPetIsInactive() {
        ReflectionTestUtils.setField(appointmentService, "petsValidationEnabled", true);
        when(petClient.getById(5L)).thenReturn(new PetInfoDTO(5L, "Rocky", false, 2L));

        assertThrows(IllegalArgumentException.class, () -> appointmentService.create(requestDTO));
    }

    // cambiar estado
    @Test
    void changeStatus_PendienteToConfirmada() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponseDTO result = appointmentService.changeStatus(1L, "Confirmada");
        assertEquals("Confirmada", result.getStatus());
    }

    @Test
    void changeStatus_PendienteToCancelada() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("Cancelada", appointmentService.changeStatus(1L, "Cancelada").getStatus());
    }

    @Test
    void changeStatus_ConfirmadaToRealizada() {
        mockAppointment.setStatus("Confirmada");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("Realizada", appointmentService.changeStatus(1L, "Realizada").getStatus());
    }

    @Test
    void changeStatus_ShouldThrowConflict_WhenPendienteToRealizada() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        assertThrows(ConflictException.class, () -> appointmentService.changeStatus(1L, "Realizada"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void changeStatus_ShouldThrowConflict_WhenAlreadyFinal() {
        mockAppointment.setStatus("Cancelada");
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        assertThrows(ConflictException.class, () -> appointmentService.changeStatus(1L, "Confirmada"));
    }

    @Test
    void changeStatus_ShouldThrowNotFound_WhenMissing() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.changeStatus(99L, "Confirmada"));
    }

    // eliminar
    @Test
    void delete_ShouldRemoveAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        appointmentService.delete(1L);
        verify(appointmentRepository, times(1)).delete(mockAppointment);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> appointmentService.delete(99L));
        verify(appointmentRepository, never()).delete(any());
    }
}
