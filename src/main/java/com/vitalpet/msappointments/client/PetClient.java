package com.vitalpet.msappointments.client;

import com.vitalpet.msappointments.config.InternalFeignConfig;
import com.vitalpet.msappointments.dto.PetInfoDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "ms-pets",
        url = "${pets.service.url}",
        configuration = InternalFeignConfig.class
)
public interface PetClient {

    @GetMapping("/api/pets/{id}")
    PetInfoDTO getById(@PathVariable("id") Long id);
}
