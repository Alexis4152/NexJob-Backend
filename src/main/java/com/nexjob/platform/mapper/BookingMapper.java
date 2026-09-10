package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.*;
import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.BookingEvidence;
import com.nexjob.platform.entity.BookingStatusHistory;
import com.nexjob.platform.entity.Payment;
import com.nexjob.platform.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ReviewMapper reviewMapper;

    public BookingSummaryResponse toSummary(Booking b) {
        return BookingSummaryResponse.builder()
                .id(b.getId())
                .folio(b.getFolio())
                .serviceTitle(b.getService().getTitle())
                .providerBusinessName(b.getProvider().getBusinessName())
                .clientFullName(b.getClient().getFirstName() + " " + b.getClient().getLastName())
                .agreedPrice(b.getAgreedPrice())
                .status(b.getStatus().name())
                .scheduledAt(b.getScheduledAt())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public BookingDetailResponse toDetail(Booking b, List<BookingEvidence> evidences,
                                           List<BookingStatusHistory> history, Payment payment, Review review) {
        return BookingDetailResponse.builder()
                .id(b.getId())
                .folio(b.getFolio())
                .serviceId(b.getService().getId())
                .serviceTitle(b.getService().getTitle())
                .providerId(b.getProvider().getId())
                .providerBusinessName(b.getProvider().getBusinessName())
                .providerPhone(b.getProvider().getUser().getPhone())
                .clientId(b.getClient().getId())
                .clientFirstName(b.getClient().getFirstName())
                .clientLastName(b.getClient().getLastName())
                .clientPhone(b.getClient().getPhone())
                .agreedPrice(b.getAgreedPrice())
                .description(b.getDescription())
                .addressLine(b.getAddressLine())
                .city(b.getCity())
                .scheduledAt(b.getScheduledAt())
                .status(b.getStatus().name())
                .paymentMethod(b.getPaymentMethod().name())
                .cancelledReason(b.getCancelledReason())
                .createdAt(b.getCreatedAt())
                .evidences(evidences.stream().map(this::toEvidence).toList())
                .history(history.stream().map(this::toHistory).toList())
                .payment(payment == null ? null : toPayment(payment))
                .review(review == null ? null : reviewMapper.toResponse(review))
                .build();
    }

    private BookingEvidenceResponse toEvidence(BookingEvidence e) {
        return BookingEvidenceResponse.builder()
                .id(e.getId()).url(e.getUrl()).description(e.getDescription()).createdAt(e.getCreatedAt())
                .build();
    }

    private BookingStatusHistoryResponse toHistory(BookingStatusHistory h) {
        return BookingStatusHistoryResponse.builder()
                .previousStatus(h.getPreviousStatus() == null ? null : h.getPreviousStatus().name())
                .newStatus(h.getNewStatus().name())
                .changedAt(h.getChangedAt())
                .note(h.getNote())
                .build();
    }

    private PaymentResponse toPayment(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).method(p.getMethod().name()).amount(p.getAmount())
                .status(p.getStatus().name()).cardLast4(p.getCardLast4()).proofUrl(p.getProofUrl()).releasedAt(p.getReleasedAt())
                .build();
    }
}
