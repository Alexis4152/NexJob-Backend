package com.nexjob.platform.scheduler;

import com.nexjob.platform.entity.QuoteRequest;
import com.nexjob.platform.entity.QuoteRequestRecipient;
import com.nexjob.platform.enums.QuoteRequestStatus;
import com.nexjob.platform.repository.QuoteRequestRecipientRepository;
import com.nexjob.platform.repository.QuoteRequestRepository;
import com.nexjob.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cierra automaticamente las solicitudes de cotizacion (punto 18) que llevan mas de
 * {@link #EXPIRATION_HOURS} horas abiertas sin que el cliente eligiera ninguna, con o sin
 * cotizaciones recibidas ("se cierra en 24 hrs o cuando elijas una", como se propuso en el
 * mockup). Revisa cada {@link #CHECK_INTERVAL_MS}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteRequestExpirationScheduler {

    private static final long EXPIRATION_HOURS = 24;
    private static final long CHECK_INTERVAL_MS = 15 * 60 * 1000; // 15 minutos

    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteRequestRecipientRepository recipientRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = CHECK_INTERVAL_MS)
    @Transactional
    public void expireStaleRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(EXPIRATION_HOURS);
        List<QuoteRequest> stale = quoteRequestRepository.findByStatusAndCreatedAtBefore(QuoteRequestStatus.ABIERTA, cutoff);
        if (stale.isEmpty()) {
            return;
        }
        stale.forEach(this::expireOne);
        log.info("Expiracion de solicitudes de cotizacion: {} solicitud(es) cerradas", stale.size());
    }

    private void expireOne(QuoteRequest quoteRequest) {
        quoteRequest.setStatus(QuoteRequestStatus.EXPIRADA);
        quoteRequestRepository.save(quoteRequest);

        List<QuoteRequestRecipient> recipients = recipientRepository.findByQuoteRequest_IdOrderByCreatedAtAsc(quoteRequest.getId());

        notificationService.notify(quoteRequest.getClient(), null, "QUOTE_REQUEST_EXPIRED",
                "Tu solicitud de cotizacion se cerro",
                "Pasaron " + EXPIRATION_HOURS + " horas sin que eligieras una cotizacion para \""
                        + quoteRequest.getCategory().getName() + "\". Puedes solicitar de nuevo si sigues buscando.",
                "/cotizaciones/" + quoteRequest.getId());

        recipients.forEach(recipient -> notificationService.notify(recipient.getProvider().getUser(), null, "QUOTE_REQUEST_EXPIRED",
                "Solicitud de cotizacion cerrada",
                "La solicitud de \"" + quoteRequest.getCategory().getName() + "\" en " + quoteRequest.getCity() + " se cerro sin eleccion",
                "/prestador/cotizaciones/" + quoteRequest.getId()));
    }
}
