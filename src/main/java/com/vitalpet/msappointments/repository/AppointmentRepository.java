package com.vitalpet.msappointments.repository;

import com.vitalpet.msappointments.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // listado ordenado por fecha y hora
    List<Appointment> findAllByOrderByDateAscTimeAsc();

    List<Appointment> findByOwnerIdOrderByDateAscTimeAsc(Long ownerId);

    boolean existsAppointmentById(Long id);
}
