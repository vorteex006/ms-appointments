package com.vitalpet.msappointments.service;

import com.vitalpet.msappointments.client.PetClient;
import com.vitalpet.msappointments.dto.AppointmentRequestDTO;
import com.vitalpet.msappointments.dto.AppointmentResponseDTO;
import com.vitalpet.msappointments.dto.PetInfoDTO;
import com.vitalpet.msappointments.exception.ConflictException;
import com.vitalpet.msappointments.exception.ResourceNotFoundException;
import com.vitalpet.msappointments.model.Appointment;
import com.vitalpet.msappointments.repository.AppointmentRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

// aca viven las reglas de negocio de las citas
@Service
public class AppointmentService {

    // el horario de atencion es de 09:00 a 19:00
    private static final LocalTime OPENS = LocalTime.of(9, 0);
    private static final LocalTime CLOSES = LocalTime.of(19, 0);

    @Autowired private AppointmentRepository appointmentRepository;

    @Autowired private PetClient petClient;

    // si es true, al crear la cita se consulta a ms-pets (existe, esta activa y es del dueno)
    // si es false (por defecto en modo standalone), el petId se acepta tal cual
    @Value("${pets.validation.enabled:false}")
    private boolean petsValidationEnabled;

    public List<AppointmentResponseDTO> getAll() {
        return appointmentRepository.findAllByOrderByDateAscTimeAsc().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public AppointmentResponseDTO getById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada."));
        return toDTO(appointment);
    }

    // traer las citas de cada dueno
    public List<AppointmentResponseDTO> getByOwnerId(Long ownerId) {
        return appointmentRepository.findByOwnerIdOrderByDateAscTimeAsc(ownerId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // validar si la cita existe
    public boolean appointmentExists(Long id) {
        return appointmentRepository.existsAppointmentById(id);
    }

    // crear una cita: valido la hora, dejo el estado en pendiente y guardo
    @Transactional
    public AppointmentResponseDTO create(AppointmentRequestDTO dto) {
        // la fecha (no anterior a hoy) ya la valida el dto con @FutureOrPresent
        // aca reviso la hora dentro del horario de atencion
        if (dto.getTime().isBefore(OPENS) || dto.getTime().isAfter(CLOSES)) {
            throw new IllegalArgumentException("El horario de atencion es de 09:00 a 19:00.");
        }

        if (petsValidationEnabled) {
            validatePet(dto.getPetId(), dto.getOwnerId());
        }

        Appointment appointment = new Appointment();
        appointment.setPetId(dto.getPetId());
        appointment.setPetName(dto.getPetName().trim());
        appointment.setOwnerId(dto.getOwnerId());
        appointment.setOwnerName(dto.getOwnerName().trim());
        appointment.setOwnerEmail(dto.getOwnerEmail().trim());
        appointment.setDate(dto.getDate());
        appointment.setTime(dto.getTime());
        appointment.setReason(dto.getReason().trim());
        appointment.setVet(dto.getVet().trim());
        // el estado lo pone el servidor, toda cita nueva parte en pendiente
        appointment.setStatus("Pendiente");

        return toDTO(appointmentRepository.save(appointment));
    }

    // cambiar el estado por id, con las mismas transiciones que los botones del panel
    // pendiente -> confirmada o cancelada ; confirmada -> realizada
    @Transactional
    public AppointmentResponseDTO changeStatus(Long id, String newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada."));

        String current = appointment.getStatus();
        boolean allowed =
                (current.equals("Pendiente") && (newStatus.equals("Confirmada") || newStatus.equals("Cancelada")))
                        || (current.equals("Confirmada") && newStatus.equals("Realizada"));

        if (!allowed) {
            throw new ConflictException("No se puede pasar de " + current + " a " + newStatus + ".");
        }

        appointment.setStatus(newStatus);
        return toDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    public void delete(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada."));
        appointmentRepository.delete(appointment);
    }

    // revisa en ms-pets que la mascota exista, este activa y sea del dueno
    private void validatePet(Long petId, Long ownerId) {
        PetInfoDTO pet;
        try {
            pet = petClient.getById(petId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Error: La mascota con ID " + petId + " no existe.");
        }

        if (pet == null || !pet.isActive()) {
            throw new IllegalArgumentException("Error: La mascota esta inactiva.");
        }

        if (pet.getOwnerId() == null || !pet.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Error: La mascota no pertenece al dueno indicado.");
        }
    }

    private AppointmentResponseDTO toDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setPetId(appointment.getPetId());
        dto.setPetName(appointment.getPetName());
        dto.setOwnerId(appointment.getOwnerId());
        dto.setOwnerName(appointment.getOwnerName());
        dto.setOwnerEmail(appointment.getOwnerEmail());
        dto.setDate(appointment.getDate());
        dto.setTime(appointment.getTime());
        dto.setReason(appointment.getReason());
        dto.setVet(appointment.getVet());
        dto.setStatus(appointment.getStatus());
        dto.setCreatedAt(appointment.getCreatedAt());
        return dto;
    }
}
