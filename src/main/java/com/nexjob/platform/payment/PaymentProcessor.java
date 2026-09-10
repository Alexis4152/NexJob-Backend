package com.nexjob.platform.payment;

import java.math.BigDecimal;

/** Abstrae el procesador de pago con tarjeta; hoy solo existe la implementacion dummy de pruebas. */
public interface PaymentProcessor {
    PaymentResult charge(String cardNumber, BigDecimal amount);
}
