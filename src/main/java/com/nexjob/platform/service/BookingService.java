package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.BookingRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.BookingSummaryResponse;
import com.nexjob.platform.dto.response.ProviderDashboardResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    // ── Cliente ──────────────────────────────────────────────────
    BookingDetailResponse create(BookingRequest request);
    Page<BookingSummaryResponse> getMine(Pageable pageable);
    BookingDetailResponse getMineDetail(Long id);
    BookingDetailResponse cancel(Long id, String reason);
    BookingDetailResponse approveAndReleasePayment(Long id, String cardNumber, MultipartFile proofFile);
    ReviewResponse addReview(Long id, Integer rating, String comment);

    // ── Prestador ────────────────────────────────────────────────
    List<BookingSummaryResponse> getProviderBoard();
    List<BookingSummaryResponse> getProviderCalendar(LocalDateTime from, LocalDateTime to);
    BookingDetailResponse getProviderDetail(Long id);
    BookingDetailResponse updateStatus(Long id, BookingStatus newStatus, String note);
    BookingDetailResponse addEvidence(Long id, MultipartFile file, String description);
    ProviderDashboardResponse getProviderDashboard();

    // ── Admin ────────────────────────────────────────────────────
    Page<BookingSummaryResponse> adminList(BookingStatus status, String q, LocalDateTime dateFrom,
                                            LocalDateTime dateTo, Pageable pageable);
    BookingDetailResponse adminGetDetail(Long id);
}
