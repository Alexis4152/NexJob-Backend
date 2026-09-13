package com.nexjob.platform.service;

public interface EmailService {
    void sendSupportTicketConfirmation(String toEmail, String subject, String folio);
    void sendBookingStatusNotification(String toEmail, String folio, String newStatus);
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
