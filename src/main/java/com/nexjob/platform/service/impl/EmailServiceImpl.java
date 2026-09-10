package com.nexjob.platform.service.impl;

import com.nexjob.platform.entity.EmailConfig;
import com.nexjob.platform.repository.EmailConfigRepository;
import com.nexjob.platform.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Arma un {@link JavaMailSenderImpl} en cada envio a partir de la configuracion SMTP guardada
 * en la tabla {@code email_config} (administrada desde {@code /api/admin/email-config}), en
 * vez de una configuracion estatica en application.properties. Si el correo esta deshabilitado
 * o falla el envio, solo se registra en el log: nunca debe tronar el flujo de negocio que lo
 * dispara (crear un ticket, cambiar el estado de una contratacion, etc.).
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailConfigRepository emailConfigRepository;

    @Override
    public void sendSupportTicketConfirmation(String toEmail, String subject, String folio) {
        send(toEmail, "Recibimos tu reporte - NexJob",
                "Gracias por escribirnos. Tu ticket \"" + subject + "\" (folio " + folio + ") ya esta en revision. "
                        + "Te responderemos a este correo en cuanto tengamos novedades.");
    }

    @Override
    public void sendBookingStatusNotification(String toEmail, String folio, String newStatus) {
        send(toEmail, "Actualizacion de tu contratacion " + folio + " - NexJob",
                "El estado de tu contratacion " + folio + " cambio a: " + newStatus + ".");
    }

    private void send(String toEmail, String subject, String body) {
        EmailConfig config = emailConfigRepository.findAll().stream().findFirst().orElse(null);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            log.info("Correo deshabilitado, se omite envio a {}: {}", toEmail, subject);
            return;
        }
        try {
            JavaMailSenderImpl sender = buildSender(config);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setFrom(config.getFromAddress() != null ? config.getFromAddress() : config.getSmtpUsername());
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}", toEmail, e);
        }
    }

    private JavaMailSenderImpl buildSender(EmailConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort());
        sender.setUsername(config.getSmtpUsername());
        sender.setPassword(config.getSmtpPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
