package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.BookingRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.BookingSummaryResponse;
import com.nexjob.platform.dto.response.ProviderDashboardResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.entity.*;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.enums.PaymentMethod;
import com.nexjob.platform.enums.PaymentStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            BookingStatus.ACEPTADO, Set.of(BookingStatus.EN_PROCESO, BookingStatus.CANCELADO),
            BookingStatus.EN_PROCESO, Set.of(BookingStatus.CONCLUIDO, BookingStatus.CANCELADO),
            BookingStatus.CONCLUIDO, Set.of(),
            BookingStatus.APROBADO, Set.of(),
            BookingStatus.RECHAZADO, Set.of(),
            BookingStatus.CANCELADO, Set.of()
    );

    private static final Set<BookingStatus> CLIENT_CANCELABLE = Set.of(BookingStatus.SOLICITADO, BookingStatus.ACEPTADO);

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

        if (bookingRepository.existsByProvider_IdAndScheduledAtAndStatusNotIn(
                service.getProvider().getId(), request.getScheduledAt(), BookingStatus.SLOT_RELEASED)) {
            throw new BusinessException("El prestador ya tiene una visita agendada en esa fecha y hora. Elige otro horario.");
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
                .build();
        booking.setCreatedBy(client);
        booking = bookingRepository.save(booking);

        recordHistory(booking, null, BookingStatus.SOLICITADO, client, "Solicitud creada por el cliente");

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
        if (newStatus == BookingStatus.CONCLUIDO && evidenceRepository.findByBooking_IdOrderByCreatedAtAsc(id).isEmpty()) {
            throw new BusinessException("Sube al menos una evidencia antes de marcar el servicio como concluido");
        }

        booking.setStatus(newStatus);
        booking.setUpdatedBy(currentUser());
        booking = bookingRepository.save(booking);
        recordHistory(booking, previous, newStatus, currentUser(), note);
        emailService.sendBookingStatusNotification(booking.getClient().getEmail(), booking.getFolio(), newStatus.name());

        return buildDetail(booking);
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
                .filter(b -> b.getScheduledAt().isAfter(LocalDateTime.now()))
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
