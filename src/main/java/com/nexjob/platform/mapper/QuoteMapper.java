package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.ProviderQuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestSummaryResponse;
import com.nexjob.platform.dto.response.QuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.QuoteRequestRecipientResponse;
import com.nexjob.platform.dto.response.QuoteRequestSummaryResponse;
import com.nexjob.platform.dto.response.QuoteResponse;
import com.nexjob.platform.entity.Quote;
import com.nexjob.platform.entity.QuoteRequest;
import com.nexjob.platform.entity.QuoteRequestRecipient;
import com.nexjob.platform.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuoteMapper {

    private final ProviderService providerService;

    public QuoteResponse toQuoteResponse(Quote q) {
        var provider = q.getProvider();
        return QuoteResponse.builder()
                .id(q.getId())
                .providerId(provider.getId())
                .providerBusinessName(provider.getBusinessName())
                .providerProfileImageUrl(provider.getProfileImageUrl())
                .providerAverageRating(provider.getAverageRating())
                .providerTotalReviews(provider.getTotalReviews())
                .providerTrustTier(providerService.computeTrustTier(provider.getId()))
                .providerAverageResponseMinutes(providerService.computeAverageResponseMinutes(provider.getId()))
                .serviceOfferingId(q.getServiceOffering().getId())
                .serviceTitle(q.getServiceOffering().getTitle())
                .price(q.getPrice())
                .availableAt(q.getAvailableAt())
                .note(q.getNote())
                .status(q.getStatus().name())
                .createdAt(q.getCreatedAt())
                .build();
    }

    public QuoteRequestRecipientResponse toRecipientResponse(QuoteRequestRecipient r, boolean quoted) {
        String status = Boolean.TRUE.equals(r.getDeclined()) ? "DESCARTADO" : (quoted ? "COTIZO" : "PENDIENTE");
        return QuoteRequestRecipientResponse.builder()
                .providerId(r.getProvider().getId())
                .providerBusinessName(r.getProvider().getBusinessName())
                .status(status)
                .build();
    }

    public QuoteRequestSummaryResponse toSummary(QuoteRequest qr, int recipientsCount, int quotesCount) {
        return QuoteRequestSummaryResponse.builder()
                .id(qr.getId())
                .categoryName(qr.getCategory().getName())
                .description(qr.getDescription())
                .city(qr.getCity())
                .status(qr.getStatus().name())
                .recipientsCount(recipientsCount)
                .quotesCount(quotesCount)
                .createdAt(qr.getCreatedAt())
                .build();
    }

    public QuoteRequestDetailResponse toDetail(QuoteRequest qr, List<QuoteRequestRecipientResponse> recipients, List<QuoteResponse> quotes) {
        return QuoteRequestDetailResponse.builder()
                .id(qr.getId())
                .categoryId(qr.getCategory().getId())
                .categoryName(qr.getCategory().getName())
                .description(qr.getDescription())
                .addressLine(qr.getAddressLine())
                .city(qr.getCity())
                .scheduledAt(qr.getScheduledAt())
                .paymentMethod(qr.getPaymentMethod().name())
                .urgency(qr.getUrgency().name())
                .status(qr.getStatus().name())
                .createdAt(qr.getCreatedAt())
                .resultingBookingId(qr.getResultingBooking() == null ? null : qr.getResultingBooking().getId())
                .recipients(recipients)
                .quotes(quotes)
                .build();
    }

    public ProviderQuoteRequestSummaryResponse toProviderSummary(QuoteRequest qr, String myStatus) {
        return ProviderQuoteRequestSummaryResponse.builder()
                .id(qr.getId())
                .categoryName(qr.getCategory().getName())
                .description(qr.getDescription())
                .city(qr.getCity())
                .requestStatus(qr.getStatus().name())
                .myStatus(myStatus)
                .createdAt(qr.getCreatedAt())
                .build();
    }

    public ProviderQuoteRequestDetailResponse toProviderDetail(QuoteRequest qr, Boolean myDeclined, QuoteResponse myQuote) {
        return ProviderQuoteRequestDetailResponse.builder()
                .id(qr.getId())
                .categoryId(qr.getCategory().getId())
                .categoryName(qr.getCategory().getName())
                .description(qr.getDescription())
                .city(qr.getCity())
                .scheduledAt(qr.getScheduledAt())
                .urgency(qr.getUrgency().name())
                .requestStatus(qr.getStatus().name())
                .createdAt(qr.getCreatedAt())
                .myDeclined(myDeclined)
                .myQuote(myQuote)
                .build();
    }
}
