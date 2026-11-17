package com.vitalpet.msappointments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// microservicio de citas de vitalpet connect, version standalone
// se expone directo por ip y puerto de la ec2, sin eureka
@SpringBootApplication
@EnableFeignClients
public class MsAppointmentsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAppointmentsApplication.class, args);
    }
}
