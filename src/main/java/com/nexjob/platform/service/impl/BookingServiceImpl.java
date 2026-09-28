package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.AcceptQuoteRequest;
import com.nexjob.platform.dto.request.BookingQuoteRequest;
import com.nexjob.platform.dto.request.BookingRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.BookingSummaryResponse;
import com.nexjob.platform.dto.response.ProviderDashboardResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.entity.*;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.DeliveryMethod;
import com.nexjob.platform.enums.PaymentMethod;
import com.nexjob.platform.enums.PaymentStatus;
import com.nexjob.platform.enums.PriceType;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.PaymentException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.BookingMapper;
import com.nexjob.platform.mapper.ReviewMapper;
import com.nexjob.platform.payment.PaymentProcessor;
import com.nexjob.platform.payment.PaymentResult;
import com.nexjob.platform.repository.*;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.BookingService;
import com.nexjob.platform.service.BookingSpecifications;
import com.nexjob.platform.service.EmailService;
import com.nexjob.platform.service.FileStorageService;
import com.nexjob.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    // Transiciones que el PRESTADOR puede disparar moviendo tarjetas en el tablero Kanban.
    // CONCLUIDO -> APROBADO es exclusiva del cliente (ver approveAndReleasePayment) porque
    // implica liberar el pago, no es un simple cambio de estado administrativo.
    private static final Map<BookingStatus, Set<BookingStatus>> PROVIDER_TRANSITIONS = Map.of(
            BookingStatus.SOLICITADO, Set.of(BookingStatus.ACEPTADO, BookingStatus.RECHAZADO),
            BookingStatus.COTIZACION_ACEPTADA, Set.of(BookingStatus.ACEPTADO, BookingStatus.CANCELADO),
            BookingStatus.ACEPTADO, Set.of(BookingStatus.EN_PROCESO, BookingStatus.CANCELADO),
            BookingStatus.EN_PROCESO, Set.of(BookingStatus.CONCLUIDO, BookingStatus.CANCELADO),
            BookingStatus.CONCLUIDO, Set.of(),
            BookingStatus.APROBADO, Set.of(),
            BookingStatus.RECHAZADO, Set.of(),
            BookingStatus.CANCELADO, Set.of()
    );

    private static final Set<BookingStatus> CLIENT_CANCELABLE = Set.of(
            BookingStatus.SOLICITADO, BookingStatus.COTIZADO, BookingStatus.COTIZACION_ACEPTADA, BookingStatus.ACEPTADO);

    private final BookingRepository bookingRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final BookingEvidenceRepository evidenceRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final PaymentProcessor paymentProcessor;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;
    private final ReviewMapper reviewMapper;

    // ── Cliente ──────────────────────────────────────────────────

    @Override
    @Transactional
    public BookingDetailResponse create(BookingRequest request) {
        User client = currentUser();
        ServiceOffering service = serviceOfferingRepository.findByIdAndIsActiveTrue(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + request.getServiceId()));

        if (service.getProvider().getUser().getId().equals(client.getId())) {
            throw new BusinessException("No puedes contratar tu propio servicio");
        }

        // Direccion/fecha de visita solo aplican a servicios de precio fijo o por hora: en un
        // servicio "a cotizar" el prestador normalmente fabrica el mueble en su taller y solo
        // visita al cliente para entregar/instalar una vez aceptada la cotizacion, asi que no
        // existe todavia una visita que agendar.
        if (service.getPriceType() != PriceType.COTIZACION) {
            if (request.getAddressLine() == null || request.getAddressLine().isBlank()) {
                throw new BusinessException("La direccion de la visita es obligatoria");
            }
            if (request.getCity() == null || request.getCity().isBlank()) {
                throw new BusinessException("La ciudad es obligatoria");
            }
            if (request.getScheduledAt() == null) {
                throw new BusinessException("La fecha de la visita es obligatoria");
            }
            if (!request.getScheduledAt().isAfter(LocalDateTime.now())) {
                throw new BusinessException("La fecha de la visita debe ser posterior a la actual");
            }
            if (bookingRepository.existsByProvider_IdAndScheduledAtAndStatusNotIn(
                    service.getProvider().getId(), request.getScheduledAt(), BookingStatus.SLOT_RELEASED)) {
                throw new BusinessException("El prestador ya tiene una visita agendada en esa fecha y hora. Elige otro horario.");
            }
        }

        Booking booking = Booking.builder()
                .folio(generateFolio())
                .client(client)
                .provider(service.getProvider())
                .service(service)
                .agreedPrice(service.getPrice())
                .description(request.getDescription())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .scheduledAt(request.getScheduledAt())
                .status(BookingStatus.SOLICITADO)
                .paymentMethod(request.getPaymentMethod())
                .urgency(request.getUrgency())
                .build();
        booking.setCreatedBy(client);
        booking = bookingRepository.save(booking);

        recordHistory(booking, null, BookingStatus.SOLICITADO, client, "Solicitud creada por el cliente");

        notificationService.notify(service.getProvider().getUser(), booking, "BOOKING_CREATED",
                "Nueva solicitud recibida",
                client.getFirstName() + " " + client.getLastName() + " solicito \"" + service.getTitle() + "\" (folio " + booking.getFolio() + ")",
                "/prestador/contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingSummaryResponse> getMine(Pageable pageable) {
        return bookingRepository.findByClient_IdOrderByCreatedAtDesc(currentUser().getId(), pageable)
                .map(bookingMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getMineDetail(Long id) {
        return buildDetail(findOwnedByClient(id));
    }

    @Override
    @Transactional
    public BookingDetailResponse uploadReferenceImage(Long id, MultipartFile file) {
        Booking booking = findOwnedByClient(id);
        booking.setReferenceImageUrl(fileStorageService.store(file, "bookings"));
        return buildDetail(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse cancel(Long id, String reason) {
        Booking booking = findOwnedByClient(id);
        if (!CLIENT_CANCELABLE.contains(booking.getStatus())) {
            throw new BusinessException("Ya no puedes cancelar una contratacion en estado " + booking.getStatus());
        }
        BookingStatus previous = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELADO);
        booking.setCancelledReason(reason);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, BookingStatus.CANCELADO, currentUser(), reason);

        notificationService.notify(booking.getProvider().getUser(), booking, "BOOKING_CANCELLED",
                "Solicitud cancelada por el cliente",
                "El cliente cancelo la contratacion de \"" + booking.getService().getTitle() + "\" (folio " + booking.getFolio() + ")",
                "/prestador/contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse approveAndReleasePayment(Long id, String cardNumber, MultipartFile proofFile) {
        Booking booking = findOwnedByClient(id);
        if (booking.getStatus() != BookingStatus.CONCLUIDO) {
            throw new BusinessException("La contratacion debe estar concluida para poder validarla");
        }

        Payment.PaymentBuilder paymentBuilder = Payment.builder()
                .booking(booking)
                .method(booking.getPaymentMethod())
                .amount(booking.getAgreedPrice())
                .releasedBy(currentUser())
                .releasedAt(LocalDateTime.now());

        if (booking.getPaymentMethod() == PaymentMethod.TARJETA) {
            if (cardNumber == null || cardNumber.isBlank()) {
                throw new BusinessException("Captura el numero de tarjeta para liberar el pago");
            }
            PaymentResult result = paymentProcessor.charge(cardNumber, booking.getAgreedPrice());
            if (!result.isApproved()) {
                paymentRepository.save(paymentBuilder
                        .status(PaymentStatus.RECHAZADO)
                        .transactionId(result.getTransactionId())
                        .cardLast4(lastFour(cardNumber))
                        .build());
                throw new PaymentException(result.getMessage());
            }
            paymentRepository.save(paymentBuilder
                    .status(PaymentStatus.LIBERADO)
                    .transactionId(result.getTransactionId())
                    .cardLast4(lastFour(cardNumber))
                    .build());
        } else if (booking.getPaymentMethod() == PaymentMethod.TRANSFERENCIA) {
            if (proofFile == null || proofFile.isEmpty()) {
                throw new BusinessException("Adjunta una foto del comprobante de transferencia para liberar el pago");
            }
            String proofUrl = fileStorageService.store(proofFile, "payment-proofs");
            paymentRepository.save(paymentBuilder
                    .status(PaymentStatus.LIBERADO)
                    .transactionId(UUID.randomUUID().toString())
                    .proofUrl(proofUrl)
                    .build());
        } else {
            // EFECTIVO: el cobro ya ocurrio en persona, el cliente solo confirma y libera.
            paymentRepository.save(paymentBuilder
                    .status(PaymentStatus.LIBERADO)
                    .transactionId(UUID.randomUUID().toString())
                    .build());
        }

        BookingStatus previous = booking.getStatus();
        booking.setStatus(BookingStatus.APROBADO);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, BookingStatus.APROBADO, currentUser(), "Cliente valido el servicio y libero el pago");
        emailService.sendBookingStatusNotification(booking.getProvider().getUser().getEmail(), booking.getFolio(), "APROBADO");
        notificationService.notify(booking.getProvider().getUser(), booking, "BOOKING_APPROVED",
                "Pago liberado",
                "El cliente aprobo el servicio y se libero el pago del folio " + booking.getFolio(),
                "/prestador/contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional
    public ReviewResponse addReview(Long id, Integer rating, String comment) {
        Booking booking = findOwnedByClient(id);
        if (booking.getStatus() != BookingStatus.APROBADO) {
            throw new BusinessException("Solo puedes calificar una contratacion ya aprobada y pagada");
        }
        if (reviewRepository.existsByBooking_Id(id)) {
            throw new BusinessException("Esta contratacion ya tiene una resena");
        }

        Review review = reviewRepository.save(Review.builder()
                .booking(booking)
                .client(booking.getClient())
                .provider(booking.getProvider())
                .rating(rating)
                .comment(comment)
                .build());

        ProviderProfile provider = booking.getProvider();
        int totalReviews = provider.getTotalReviews() == null ? 0 : provider.getTotalReviews();
        BigDecimal currentAverage = provider.getAverageRating() == null ? BigDecimal.ZERO : provider.getAverageRating();
        BigDecimal newAverage = currentAverage.multiply(BigDecimal.valueOf(totalReviews))
                .add(BigDecimal.valueOf(rating))
                .divide(BigDecimal.valueOf(totalReviews + 1), 2, RoundingMode.HALF_UP);
        provider.setAverageRating(newAverage);
        provider.setTotalReviews(totalReviews + 1);
        providerProfileRepository.save(provider);

        notificationService.notify(provider.getUser(), booking, "BOOKING_REVIEWED",
                "Nueva resena recibida",
                booking.getClient().getFirstName() + " te dejo una resena de " + rating + " estrellas",
                "/prestador/contrataciones/" + booking.getId());

        return reviewMapper.toResponse(review);
    }

    // ── Prestador ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getProviderBoard() {
        return bookingRepository.findByProvider_IdOrderByScheduledAtAsc(myProvider().getId()).stream()
                .map(bookingMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getProviderCalendar(LocalDateTime from, LocalDateTime to) {
        return bookingRepository.findByProvider_IdAndScheduledAtBetweenAndStatusNotInOrderByScheduledAtAsc(
                        myProvider().getId(), from, to, BookingStatus.SLOT_RELEASED)
                .stream()
                .map(bookingMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getProviderDetail(Long id) {
        return buildDetail(findOwnedByProvider(id));
    }

    @Override
    @Transactional
    public BookingDetailResponse updateStatus(Long id, BookingStatus newStatus, String note) {
        Booking booking = findOwnedByProvider(id);
        BookingStatus previous = booking.getStatus();

        if (!PROVIDER_TRANSITIONS.getOrDefault(previous, Set.of()).contains(newStatus)) {
            throw new BusinessException("No se puede mover la contratacion de " + previous + " a " + newStatus);
        }
        if (previous == BookingStatus.SOLICITADO && newStatus == BookingStatus.ACEPTADO
                && booking.getService().getPriceType() == PriceType.COTIZACION) {
            throw new BusinessException("Este servicio es \"a cotizar\": envia tu cotizacion en vez de aceptar directamente");
        }
        if (newStatus == BookingStatus.CONCLUIDO && evidenceRepository.findByBooking_IdOrderByCreatedAtAsc(id).isEmpty()) {
            throw new BusinessException("Sube al menos una evidencia antes de marcar el servicio como concluido");
        }

        booking.setStatus(newStatus);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, newStatus, currentUser(), note);
        emailService.sendBookingStatusNotification(booking.getClient().getEmail(), booking.getFolio(), newStatus.name());
        notificationService.notify(booking.getClient(), booking, "BOOKING_" + newStatus.name(),
                titleForStatus(newStatus), bodyForStatus(newStatus, booking), "/mis-contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse submitQuote(Long id, BookingQuoteRequest request) {
        Booking booking = findOwnedByProvider(id);
        if (booking.getStatus() != BookingStatus.SOLICITADO) {
            throw new BusinessException("Solo puedes cotizar una solicitud que aun esta pendiente");
        }
        if (booking.getService().getPriceType() != PriceType.COTIZACION) {
            throw new BusinessException("Este servicio no es \"a cotizar\": usa Aceptar/Rechazar directamente");
        }
        if (request.getEstimatedDeliveryDate() != null && !request.getEstimatedDeliveryDate().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha estimada de entrega debe ser posterior a hoy");
        }

        List<BookingQuoteItem> items = request.getItems().stream().map(i -> {
            BookingQuoteItem item = new BookingQuoteItem();
            item.setConcept(i.getConcept());
            item.setQuantity(i.getQuantity());
            item.setUnit(i.getUnit());
            item.setUnitCost(i.getUnitCost());
            return item;
        }).toList();
        BigDecimal total = items.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BookingStatus previous = booking.getStatus();
        booking.setQuoteItems(items);
        booking.setQuoteNote(request.getNote());
        booking.setQuoteTotal(total);
        booking.setQuoteSentAt(LocalDateTime.now());
        booking.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        booking.setStatus(BookingStatus.COTIZADO);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, BookingStatus.COTIZADO, currentUser(), "Cotizacion enviada por " + total + " " + "MXN");

        emailService.sendBookingStatusNotification(booking.getClient().getEmail(), booking.getFolio(), "COTIZADO");
        notificationService.notify(booking.getClient(), booking, "BOOKING_COTIZADO",
                titleForStatus(BookingStatus.COTIZADO), bodyForStatus(BookingStatus.COTIZADO, booking),
                "/mis-contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse acceptQuote(Long id, AcceptQuoteRequest request) {
        Booking booking = findOwnedByClient(id);
        if (booking.getStatus() != BookingStatus.COTIZADO) {
            throw new BusinessException("No hay una cotizacion pendiente de respuesta");
        }

        // Este es el momento en que por fin se sabe la direccion real (no se pidio al
        // solicitar, ver create()): a domicilio del cliente, o recoge en el sitio del
        // prestador (del cual solo se conoce la ciudad registrada, no una calle exacta).
        if (request.getDeliveryMethod() == DeliveryMethod.DOMICILIO) {
            if (request.getAddressLine() == null || request.getAddressLine().isBlank()) {
                throw new BusinessException("Indica la direccion donde se entregaria el trabajo");
            }
            if (request.getCity() == null || request.getCity().isBlank()) {
                throw new BusinessException("Indica la ciudad donde se entregaria el trabajo");
            }
            booking.setAddressLine(request.getAddressLine());
            booking.setCity(request.getCity());
        } else {
            booking.setAddressLine("Recoge en el sitio del prestador");
            booking.setCity(booking.getProvider().getCity());
        }

        BookingStatus previous = booking.getStatus();
        booking.setAgreedPrice(booking.getQuoteTotal());
        // El cliente confirmo que la fecha estimada del prestador le funciona: se vuelve la
        // fecha de la visita, aunque el prestador todavia debe confirmar para pasar a ACEPTADO.
        if (booking.getEstimatedDeliveryDate() != null) {
            booking.setScheduledAt(booking.getEstimatedDeliveryDate().atStartOfDay());
        }
        booking.setStatus(BookingStatus.COTIZACION_ACEPTADA);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, BookingStatus.COTIZACION_ACEPTADA, currentUser(), "Cliente acepto la cotizacion");

        emailService.sendBookingStatusNotification(booking.getProvider().getUser().getEmail(), booking.getFolio(), "COTIZACION_ACEPTADA");
        notificationService.notify(booking.getProvider().getUser(), booking, "BOOKING_QUOTE_ACCEPTED",
                "El cliente acepto tu cotizacion",
                booking.getClient().getFirstName() + " " + booking.getClient().getLastName()
                        + " acepto tu cotizacion de " + booking.getQuoteTotal() + ", confirma para agendar la visita (folio " + booking.getFolio() + ")",
                "/prestador/contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse rejectQuote(Long id, String reason) {
        Booking booking = findOwnedByClient(id);
        if (booking.getStatus() != BookingStatus.COTIZADO) {
            throw new BusinessException("No hay una cotizacion pendiente de respuesta");
        }
        BookingStatus previous = booking.getStatus();
        booking.setStatus(BookingStatus.RECHAZADO);
        booking.setCancelledReason(reason);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, BookingStatus.RECHAZADO, currentUser(), "Cliente rechazo la cotizacion" + (reason != null ? ": " + reason : ""));

        emailService.sendBookingStatusNotification(booking.getProvider().getUser().getEmail(), booking.getFolio(), "RECHAZADO");
        notificationService.notify(booking.getProvider().getUser(), booking, "BOOKING_QUOTE_REJECTED",
                "El cliente rechazo tu cotizacion",
                booking.getClient().getFirstName() + " " + booking.getClient().getLastName()
                        + " rechazo tu cotizacion (folio " + booking.getFolio() + ")",
                "/prestador/contrataciones/" + booking.getId());

        return buildDetail(booking);
    }

    private String titleForStatus(BookingStatus status) {
        return switch (status) {
            case COTIZADO -> "Recibiste una cotizacion";
            case ACEPTADO -> "Tu solicitud fue aceptada";
            case RECHAZADO -> "Tu solicitud fue rechazada";
            case EN_PROCESO -> "Tu servicio esta en proceso";
            case CONCLUIDO -> "El prestador concluyo el servicio";
            case CANCELADO -> "Tu contratacion fue cancelada";
            default -> "Actualizacion de tu contratacion";
        };
    }

    private String bodyForStatus(BookingStatus status, Booking booking) {
        String provider = booking.getProvider().getBusinessName();
        return switch (status) {
            case COTIZADO -> provider + " envio una cotizacion de " + booking.getQuoteTotal() + " para tu solicitud (folio " + booking.getFolio() + ")";
            case ACEPTADO -> provider + " acepto tu solicitud (folio " + booking.getFolio() + ")";
            case RECHAZADO -> provider + " rechazo tu solicitud (folio " + booking.getFolio() + ")";
            case EN_PROCESO -> provider + " comenzo a trabajar en tu servicio (folio " + booking.getFolio() + ")";
            case CONCLUIDO -> provider + " marco como concluido tu servicio, valida el trabajo para liberar el pago (folio " + booking.getFolio() + ")";
            case CANCELADO -> provider + " cancelo tu contratacion (folio " + booking.getFolio() + ")";
            default -> "Folio " + booking.getFolio();
        };
    }

    @Override
    @Transactional
    public BookingDetailResponse addEvidence(Long id, MultipartFile file, String description) {
        Booking booking = findOwnedByProvider(id);
        if (booking.getStatus() != BookingStatus.EN_PROCESO) {
            throw new BusinessException("Solo puedes subir evidencias mientras el servicio esta en proceso");
        }
        String url = fileStorageService.store(file, "evidences");
        evidenceRepository.save(BookingEvidence.builder()
                .booking(booking).url(url).description(description).createdBy(currentUser())
                .build());
        return buildDetail(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDashboardResponse getProviderDashboard() {
        Long providerId = myProvider().getId();
        long solicitados = bookingRepository.findByProvider_IdAndStatus(providerId, BookingStatus.SOLICITADO).size();
        long porHacer = bookingRepository.findByProvider_IdAndStatus(providerId, BookingStatus.ACEPTADO).size();
        long enProceso = bookingRepository.findByProvider_IdAndStatus(providerId, BookingStatus.EN_PROCESO).size();
        long concluidos = bookingRepository.findByProvider_IdAndStatus(providerId, BookingStatus.CONCLUIDO).size()
                + bookingRepository.findByProvider_IdAndStatus(providerId, BookingStatus.APROBADO).size();

        List<BookingSummaryResponse> proximas = bookingRepository.findByProvider_IdOrderByScheduledAtAsc(providerId).stream()
                .filter(b -> b.getStatus() == BookingStatus.ACEPTADO || b.getStatus() == BookingStatus.EN_PROCESO)
                // Un servicio "a cotizar" puede llegar a estos estados sin tener scheduledAt
                // todavia (no se le pide fecha al solicitar, ver create()); sin fecha no hay
                // "proxima visita" que mostrar aqui.
                .filter(b -> b.getScheduledAt() != null && b.getScheduledAt().isAfter(LocalDateTime.now()))
                .limit(5)
                .map(bookingMapper::toSummary)
                .toList();

        return ProviderDashboardResponse.builder()
                .solicitados(solicitados).porHacer(porHacer).enProceso(enProceso).concluidos(concluidos)
                .proximasVisitas(proximas)
                .build();
    }

    // ── Admin ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<BookingSummaryResponse> adminList(BookingStatus status, String q, LocalDateTime dateFrom,
                                                   LocalDateTime dateTo, Pageable pageable) {
        String like = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        return bookingRepository.findAll(BookingSpecifications.search(status, like, dateFrom, dateTo), pageable)
                .map(bookingMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse adminGetDetail(Long id) {
        return buildDetail(bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contratacion no encontrada: " + id)));
    }

    // ── Helpers ──────────────────────────────────────────────────

    private BookingDetailResponse buildDetail(Booking booking) {
        List<BookingEvidence> evidences = evidenceRepository.findByBooking_IdOrderByCreatedAtAsc(booking.getId());
        var history = historyRepository.findByBooking_IdOrderByChangedAtAsc(booking.getId());
        Payment payment = paymentRepository.findByBooking_Id(booking.getId()).orElse(null);
        Review review = reviewRepository.findByBooking_Id(booking.getId()).orElse(null);
        return bookingMapper.toDetail(booking, evidences, history, payment, review);
    }

    private void recordHistory(Booking booking, BookingStatus previous, BookingStatus newStatus, User actor, String note) {
        historyRepository.save(BookingStatusHistory.builder()
                .booking(booking).previousStatus(previous).newStatus(newStatus).changedBy(actor).note(note)
                .build());
    }

    private String generateFolio() {
        return "NJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String lastFour(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");
        return digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits;
    }

    private Booking findOwnedByClient(Long id) {
        return bookingRepository.findByIdAndClient_Id(id, currentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contratacion no encontrada: " + id));
    }

    private Booking findOwnedByProvider(Long id) {
        return bookingRepository.findByIdAndProvider_Id(id, myProvider().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contratacion no encontrada: " + id));
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
