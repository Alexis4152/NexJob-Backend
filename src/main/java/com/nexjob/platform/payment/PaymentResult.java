package com.nexjob.platform.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResult {
    private boolean approved;
    private String transactionId;
    private String authorizationCode;
    private String message;
}
