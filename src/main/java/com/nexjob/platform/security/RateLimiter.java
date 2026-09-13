package com.nexjob.platform.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Limitador de tasa en memoria (ventana deslizante), usado para frenar abuso de los
 * endpoints publicos de recuperacion de contrasena (spam de solicitudes, fuerza bruta
 * de codigos). No es apto para un despliegue con multiples instancias del backend
 * (cada una llevaria su propio conteo independiente) ni sobrevive un reinicio; para eso
 * se necesitaria un almacen compartido (ej. Redis). Suficiente para una sola instancia.
 */
@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    /** Devuelve true si la solicitud se permite (y la registra); false si se excedio el limite. */
    public boolean tryAcquire(String key, int maxRequests, Duration window) {
        Deque<Instant> timestamps = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (timestamps) {
            Instant cutoff = Instant.now().minus(window);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(Instant.now());
            return true;
        }
    }
}
