package com.nexjob.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class NexJobPlatformApplication {

    // Fija la zona horaria del JVM antes de que arranque el contexto de Spring, para que
    // LocalDateTime.now() (fechas de agenda, historial de estados, etc.) refleje la hora de
    // Mexico sin importar en que zona horaria corra el contenedor Docker (usualmente UTC).
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City"));
    }

    public static void main(String[] args) {
        SpringApplication.run(NexJobPlatformApplication.class, args);
    }
}
