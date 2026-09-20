package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.QuoteRequestCreateRequest;
import com.nexjob.platform.dto.request.QuoteSubmitRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestSummaryResponse;
import com.nexjob.platform.dto.response.QuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.QuoteRequestRecipientResponse;
import com.nexjob.platform.dto.response.QuoteRequestSummaryResponse;
import com.nexjob.platform.dto.response.QuoteResponse;
import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.BookingStatusHistory;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.Quote;
import com.nexjob.platform.entity.QuoteRequest;
import com.nexjob.platform.entity.QuoteRequestRecipient;
import com.nexjob.platform.entity.ServiceOffering;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.QuoteRequestStatus;
import com.nexjob.platform.enums.QuoteStatus;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.BookingMapper;
import com.nexjob.platform.mapper.QuoteMapper;
import com.nexjob.platform.repository.BookingRepository;
import com.nexjob.platform.repository.BookingStatusHistoryRepository;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.repository.ProviderProfileRepository;
import com.nexjob.platform.repository.QuoteRepository;
import com.nexjob.platform.repository.QuoteRequestRecipientRepository;
import com.nexjob.platform.repository.QuoteRequestRepository;
import com.nexjob.platform.repository.ServiceOfferingRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.NotificationService;
import com.nexjob.platform.service.PostalCodeLookupService;
import com.nexjob.platform.service.ProviderSpecifications;
import com.nexjob.platform.service.QuoteRequestService;
import com.nexjob.platform.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteRequestServiceImpl implements QuoteRequestService {

    private static final int MAX_RECIPIENTS = 5;
    // Un prestador fuera de este radio no puede trasladarse de forma razonable, asi que
    // nunca se le notifica aunque eso signifique juntar menos de MAX_RECIPIENTS (o ninguno).
    private static final double RADIUS_KM = 50;

    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteRequestRecipientRepository recipientRepository;
    private final QuoteRepository quoteRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final NotificationService notificationService;
    private final PostalCodeLookupService postalCodeLookupService;
    private final QuoteMapper quoteMapper;
    private final BookingMapper bookingMapper;

    // ── Cliente ──────────────────────────────────────────────────

    @Override
    @Transactional
    public QuoteRequestDetailResponse create(QuoteRequestCreateRequest request) {
        User client = currentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + request.getCategoryId()));

        QuoteRequest quoteRequest = QuoteRequest.builder()
                .client(client)
                .category(category)
                .description(request.getDescription())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .scheduledAt(request.getScheduledAt())
                .paymentMethod(request.getPaymentMethod())
                .urgency(request.getUrgency())
                .status(QuoteRequestStatus.ABIERTA)
                .build();
        var coords = postalCodeLookupService.lookup(request.getPostalCode());
        if (coords.isPresent()) {
            quoteRequest.setLatitude(coords.get().latitude());
            quoteRequest.setLongitude(coords.get().longitude());
        }
        quoteRequest.setCreatedBy(client);
        quoteRequest = quoteRequestRepository.save(quoteRequest);

        List<ProviderProfile> chosen = selectRecipients(request.getCategoryId(), request.getCity(),
                quoteRequest.getLatitude(), quoteRequest.getLongitude());

        if (chosen.isEmpty()) {
            throw new BusinessException("No encontramos prestadores de esta categoria cerca de tu ubicacion. "
                    + "Puedes buscar y contactar directamente a alguno fuera de tu zona si asi lo decides.");
        }

        for (ProviderProfile provider : chosen) {
            recipientRepository.save(QuoteRequestRecipient.builder()
                    .quoteRequest(quoteRequest).provider(provider).declined(false)
                    .build());
            notificationService.notify(provider.getUser(), null, "QUOTE_REQUEST_CREATED",
                    "Nueva solicitud de cotizacion",
                    "Un cliente solicita \"" + category.getName() + "\" en " + quoteRequest.getCity() + ": " + truncate(request.getDescription()),
                    "/prestador/cotizaciones/" + quoteRequest.getId());
        }

        return buildDetail(quoteRequest);
    }

    /**
     * Elige a los prestadores de la categoria que recibiran la solicitud, sin importar
     * de otros estados: si un prestador no puede trasladarse hasta el cliente, no tiene
     * sentido notificarlo. Si el cliente dio un codigo postal valido (y por lo tanto tenemos
     * su lat/lng), se filtra estrictamente a los que estan dentro de RADIUS_KM (distancia
     * real, Haversine) y se ordena por cercania; puede devolver menos de MAX_RECIPIENTS, o
     * ninguno, si no hay suficientes prestadores cerca. Sin coordenadas (no dio codigo postal
     * o no se encontro en el catalogo), se cae a coincidencia de texto en la ciudad.
     */
    private List<ProviderProfile> selectRecipients(Long categoryId, String city, BigDecimal lat, BigDecimal lng) {
        Sort recomendados = Sort.by(Sort.Direction.DESC, "isVerified")
                .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                .and(Sort.by(Sort.Direction.DESC, "totalReviews"))
                .and(Sort.by(Sort.Direction.ASC, "id"));
        Specification<ProviderProfile> categorySpec = ProviderSpecifications.search(
                categoryId, null, null, null, null, null, null, null, null, null, null, null);

        if (lat != null && lng != null) {
            double clientLat = lat.doubleValue();
            double clientLng = lng.doubleValue();

            record Ranked(ProviderProfile provider, double distanceKm) {
            }

            return providerProfileRepository.findAll(categorySpec).stream()
                    .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                    .map(p -> new Ranked(p, GeoUtils.distanceKm(clientLat, clientLng,
                            p.getLatitude().doubleValue(), p.getLongitude().doubleValue())))
                    .filter(r -> r.distanceKm() <= RADIUS_KM)
                    .sorted(Comparator.comparingDouble(Ranked::distanceKm))
                    .map(Ranked::provider)
                    .limit(MAX_RECIPIENTS)
                    .toList();
        }

        Specification<ProviderProfile> citySpec = ProviderSpecifications.search(
                categoryId, null, city, null, null, null, null, null, null, null, null, null);
        return providerProfileRepository.findAll(citySpec, PageRequest.of(0, MAX_RECIPIENTS, recomendados))
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteRequestSummaryResponse> getMine(Pageable pageable) {
        return quoteRequestRepository.findByClient_IdOrderByCreatedAtDesc(currentUser().getId(), pageable)
                .map(qr -> quoteMapper.toSummary(qr,
                        recipientRepository.findByQuoteRequest_IdOrderByCreatedAtAsc(qr.getId()).size(),
                        quoteRepository.findByQuoteRequest_IdOrderByPriceAsc(qr.getId()).size()));
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteRequestDetailResponse getMineDetail(Long id) {
        return buildDetail(findOwnedByClient(id));
    }

    @Override
    @Transactional
    public QuoteRequestDetailResponse cancel(Long id) {
        QuoteRequest quoteRequest = findOwnedByClient(id);
        if (quoteRequest.getStatus() != QuoteRequestStatus.ABIERTA) {
            throw new BusinessException("Esta solicitud ya no se puede cancelar");
        }
        quoteRequest.setStatus(QuoteRequestStatus.EXPIRADA);
        quoteRequest.setUpdatedBy(currentUser());
        quoteRequestRepository.save(quoteRequest);
        return buildDetail(quoteRequest);
    }

    @Override
    @Transactional
    public BookingDetailResponse choose(Long id, Long quoteId) {
        QuoteRequest quoteRequest = findOwnedByClient(id);
        if (quoteRequest.getStatus() != QuoteRequestStatus.ABIERTA) {
            throw new BusinessException("Esta solicitud ya no esta abierta");
        }
        Quote chosen = quoteRepository.findByIdAndQuoteRequest_Id(quoteId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion no encontrada: " + quoteId));
        if (chosen.getStatus() != QuoteStatus.ENVIADA) {
            throw new BusinessException("Esta cotizacion ya no esta disponible");
        }

        ProviderProfile provider = chosen.getProvider();
        if (bookingRepository.existsByProvider_IdAndScheduledAtAndStatusNotIn(
                provider.getId(), chosen.getAvailableAt(), BookingStatus.SLOT_RELEASED)) {
            throw new BusinessException("El prestador ya tiene una visita agendada en ese horario. Pidele que actualice su disponibilidad.");
        }

        Booking booking = Booking.builder()
                .folio(generateFolio())
                .client(quoteRequest.getClient())
                .provider(provider)
                .service(chosen.getServiceOffering())
                .agreedPrice(chosen.getPrice())
                .description(quoteRequest.getDescription())
                .addressLine(quoteRequest.getAddressLine())
                .city(quoteRequest.getCity())
                .scheduledAt(chosen.getAvailableAt())
                .status(BookingStatus.SOLICITADO)
                .paymentMethod(quoteRequest.getPaymentMethod())
                .urgency(quoteRequest.getUrgency())
                .build();
        booking.setCreatedBy(quoteRequest.getClient());
        booking = bookingRepository.save(booking);

        BookingStatusHistory history = bookingStatusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking).previousStatus(null).newStatus(BookingStatus.SOLICITADO)
                .changedBy(quoteRequest.getClient()).note("Contratacion creada al elegir una cotizacion (folio solicitud #" + quoteRequest.getId() + ")")
                .build());

        chosen.setStatus(QuoteStatus.ELEGIDA);
        quoteRepository.save(chosen);

        for (Quote other : quoteRepository.findByQuoteRequest_IdAndStatus(id, QuoteStatus.ENVIADA)) {
            other.setStatus(QuoteStatus.DESCARTADA);
            quoteRepository.save(other);
            notificationService.notify(other.getProvider().getUser(), booking, "QUOTE_DISCARDED",
                    "El cliente eligio a otro prestador",
                    "Tu cotizacion para \"" + quoteRequest.getCategory().getName() + "\" no fue seleccionada esta vez",
                    "/prestador/cotizaciones/" + id);
        }

        quoteRequest.setStatus(QuoteRequestStatus.CERRADA);
        quoteRequest.setResultingBooking(booking);
        quoteRequest.setUpdatedBy(currentUser());
        quoteRequestRepository.save(quoteRequest);

        notificationService.notify(provider.getUser(), booking, "QUOTE_CHOSEN",
                "¡Tu cotizacion fue elegida!",
                "El cliente eligio tu cotizacion (folio " + booking.getFolio() + "). Revisa la nueva contratacion.",
                "/prestador/contrataciones/" + booking.getId());

        return bookingMapper.toDetail(booking, List.of(), List.of(history), null, null);
    }

    // ── Prestador ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderQuoteRequestSummaryResponse> getProviderInbox(Pageable pageable) {
        Long providerId = myProvider().getId();
        return recipientRepository.findByProvider_IdOrderByCreatedAtDesc(providerId, pageable)
                .map(r -> {
                    Quote quote = quoteRepository.findByQuoteRequest_IdAndProvider_Id(r.getQuoteRequest().getId(), providerId).orElse(null);
                    String myStatus = myStatusFor(r, quote);
                    return quoteMapper.toProviderSummary(r.getQuoteRequest(), myStatus);
                });
    }

    /**
     * Estado del prestador sobre una solicitud, para su bandeja: distingue "cotizaste, esperando"
     * de "te eligieron" (debe dar seguimiento en el tablero, ya no en esta cotizacion) y de
     * "no fue elegida" (el cliente eligio a otro prestador).
     */
    private String myStatusFor(QuoteRequestRecipient recipient, Quote quote) {
        if (Boolean.TRUE.equals(recipient.getDeclined())) {
            return "DESCARTADO";
        }
        if (quote == null) {
            return "PENDIENTE";
        }
        return switch (quote.getStatus()) {
            case ELEGIDA -> "ELEGIDA";
            case DESCARTADA -> "NO_ELEGIDA";
            case ENVIADA -> "COTIZO";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderQuoteRequestDetailResponse getProviderDetail(Long id) {
        Long providerId = myProvider().getId();
        QuoteRequestRecipient recipient = findRecipient(id, providerId);
        Quote myQuote = quoteRepository.findByQuoteRequest_IdAndProvider_Id(id, providerId).orElse(null);
        return quoteMapper.toProviderDetail(recipient.getQuoteRequest(), recipient.getDeclined(),
                myQuote == null ? null : quoteMapper.toQuoteResponse(myQuote));
    }

    @Override
    @Transactional
    public ProviderQuoteRequestDetailResponse submitQuote(Long id, QuoteSubmitRequest request) {
        ProviderProfile provider = myProvider();
        QuoteRequestRecipient recipient = findRecipient(id, provider.getId());
        QuoteRequest quoteRequest = recipient.getQuoteRequest();

        if (quoteRequest.getStatus() != QuoteRequestStatus.ABIERTA) {
            throw new BusinessException("Esta solicitud ya no esta abierta");
        }
        if (Boolean.TRUE.equals(recipient.getDeclined())) {
            throw new BusinessException("Ya descartaste esta solicitud");
        }
        if (quoteRepository.findByQuoteRequest_IdAndProvider_Id(id, provider.getId()).isPresent()) {
            throw new BusinessException("Ya enviaste una cotizacion para esta solicitud");
        }

        ServiceOffering service = serviceOfferingRepository.findByIdAndProvider_Id(request.getServiceOfferingId(), provider.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + request.getServiceOfferingId()));
        if (!service.getCategory().getId().equals(quoteRequest.getCategory().getId())) {
            throw new BusinessException("El servicio elegido no pertenece a la categoria de la solicitud");
        }

        Quote quote = quoteRepository.save(Quote.builder()
                .quoteRequest(quoteRequest).provider(provider).serviceOffering(service)
                .price(request.getPrice()).availableAt(request.getAvailableAt()).note(request.getNote())
                .status(QuoteStatus.ENVIADA)
                .build());

        notificationService.notify(quoteRequest.getClient(), null, "QUOTE_RECEIVED",
                "Nueva cotizacion recibida",
                provider.getBusinessName() + " te envio una cotizacion de " + request.getPrice(),
                "/cotizaciones/" + id);

        return quoteMapper.toProviderDetail(quoteRequest, false, quoteMapper.toQuoteResponse(quote));
    }

    @Override
    @Transactional
    public ProviderQuoteRequestDetailResponse decline(Long id) {
        ProviderProfile provider = myProvider();
        QuoteRequestRecipient recipient = findRecipient(id, provider.getId());
        if (quoteRepository.findByQuoteRequest_IdAndProvider_Id(id, provider.getId()).isPresent()) {
            throw new BusinessException("Ya enviaste una cotizacion, no puedes descartar esta solicitud");
        }
        recipient.setDeclined(true);
        recipientRepository.save(recipient);
        return quoteMapper.toProviderDetail(recipient.getQuoteRequest(), true, null);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private QuoteRequestDetailResponse buildDetail(QuoteRequest qr) {
        List<QuoteRequestRecipient> recipients = recipientRepository.findByQuoteRequest_IdOrderByCreatedAtAsc(qr.getId());
        List<Quote> quotes = quoteRepository.findByQuoteRequest_IdOrderByPriceAsc(qr.getId());

        List<QuoteRequestRecipientResponse> recipientResponses = recipients.stream()
                .map(r -> {
                    boolean quoted = quotes.stream().anyMatch(q -> q.getProvider().getId().equals(r.getProvider().getId()));
                    return quoteMapper.toRecipientResponse(r, quoted);
                })
                .toList();
        List<QuoteResponse> quoteResponses = quotes.stream().map(quoteMapper::toQuoteResponse).toList();

        return quoteMapper.toDetail(qr, recipientResponses, quoteResponses);
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }

    private String generateFolio() {
        return "NJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private QuoteRequest findOwnedByClient(Long id) {
        return quoteRequestRepository.findByIdAndClient_Id(id, currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cotizacion no encontrada: " + id));
    }

    private QuoteRequestRecipient findRecipient(Long quoteRequestId, Long providerId) {
        return recipientRepository.findByQuoteRequest_IdAndProvider_Id(quoteRequestId, providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cotizacion no encontrada: " + quoteRequestId));
    }

    private ProviderProfile myProvider() {
        return providerProfileRepository.findByUser_Id(currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de prestador"));
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return user;
    }
}
