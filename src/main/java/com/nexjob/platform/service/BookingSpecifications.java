package com.nexjob.platform.service;

import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.enums.BookingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Construye el filtro dinamico de busqueda de contrataciones para el listado de administracion. */
public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> search(BookingStatus status, String keywordLower,
                                                  LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keywordLower != null) {
                Predicate byFolio = cb.like(cb.lower(root.get("folio")), keywordLower);
                Predicate byService = cb.like(cb.lower(root.get("service").get("title")), keywordLower);
                Predicate byProvider = cb.like(cb.lower(root.get("provider").get("businessName")), keywordLower);
                predicates.add(cb.or(byFolio, byService, byProvider));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
