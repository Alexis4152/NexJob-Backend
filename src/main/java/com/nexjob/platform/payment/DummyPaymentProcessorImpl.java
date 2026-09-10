package com.nexjob.platform.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Procesador de tarjeta simulado (no hay pasarela real conectada). En modo AUTO aprueba si el
 * numero de tarjeta termina en digito par y rechaza si termina en impar, para poder probar
 * ambos flujos desde el frontend sin depender de un proveedor externo.
 */
@Component
public class DummyPaymentProcessorImpl implements PaymentProcessor {

    @Value("${app.payment.dummy-mode}")
    private String mode;

    @Override
    public PaymentResult charge(String cardNumber, BigDecimal amount) {
        boolean approved = switch (mode) {
            case "ALWAYS_APPROVE" -> true;
            case "ALWAYS_REJECT" -> false;
            default -> isLastDigitEven(cardNumber);
        };

        if (approved) {
            return PaymentResult.builder()
                    .approved(true)
                    .transactionId(UUID.randomUUID().toString())
                    .authorizationCode(String.valueOf((int) (Math.random() * 900000) + 100000))
                    .message("Pago aprobado")
                    .build();
        }
        return PaymentResult.builder()
                .approved(false)
                .transactionId(UUID.randomUUID().toString())
                .message("Pago rechazado por el banco emisor")
                .build();
    }

    private boolean isLastDigitEven(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) return false;
        char last = digits.charAt(digits.length() - 1);
        return (last - '0') % 2 == 0;
    }
}
