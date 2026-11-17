package com.vitalpet.msappointments.client;

import com.vitalpet.msappointments.dto.PetInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// cliente hacia ms-pets
// sin eureka, la url viene por configuracion (pets.service.url / PETS_SERVICE_URL)
// solo se usa si pets.validation.enabled=true (ver AppointmentService)
@FeignClient(name = "ms-pets", url = "${pets.service.url}")
public interface PetClient {

    @GetMapping("/api/pets/{id}")
    PetInfoDTO getById(@PathVariable("id") Long id);
}
