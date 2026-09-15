package com.nexjob.platform.service;

import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.ServiceImage;
import com.nexjob.platform.entity.ServiceOffering;
import com.nexjob.platform.enums.BookingStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Construye el filtro dinamico de busqueda de prestadores en el catalogo publico. */
public final class ProviderSpecifications {

    private ProviderSpecifications() {
    }

    public static Specification<ProviderProfile> search(Long categoryId, String keyword, String city, BigDecimal minRating,
                                                          Boolean verified, Integer minExperience, BigDecimal minPrice, BigDecimal maxPrice,
                                                          Boolean hasPhotos, String availability, LocalDate date, String serviceType) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));

            if (categoryId != null) {
                predicates.add(cb.equal(root.join("categories", JoinType.LEFT).get("id"), categoryId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                Predicate byBusiness = cb.like(cb.lower(root.get("businessName")), like);
                Predicate byBio = cb.like(cb.lower(root.get("bio")), like);
                Join<Object, Object> catJoin = root.join("categories", JoinType.LEFT);
                Predicate byCategory = cb.like(cb.lower(catJoin.get("name")), like);

                Subquery<Long> serviceSub = query.subquery(Long.class);
                var serviceRoot = serviceSub.from(ServiceOffering.class);
                serviceSub.select(serviceRoot.get("id"));
                serviceSub.where(cb.and(
                        cb.equal(serviceRoot.get("provider"), root),
                        cb.isTrue(serviceRoot.get("isActive")),
                        cb.or(
                                cb.like(cb.lower(serviceRoot.get("title")), like),
                                cb.like(cb.lower(serviceRoot.get("description")), like)
                        )
                ));
                Predicate byService = cb.exists(serviceSub);

                predicates.add(cb.or(byBusiness, byBio, byCategory, byService));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            if (Boolean.TRUE.equals(verified)) {
                predicates.add(cb.isTrue(root.get("isVerified")));
            }
            if (minExperience != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearsExperience"), minExperience));
            }
            if (minPrice != null || maxPrice != null) {
                // Un mismo servicio activo debe cumplir ambos limites a la vez (rango real,
                // no dos filtros independientes): "tiene un servicio entre $X y $Y".
                Subquery<Long> priceSub = query.subquery(Long.class);
                var priceServiceRoot = priceSub.from(ServiceOffering.class);
                List<Predicate> pricePredicates = new ArrayList<>();
                pricePredicates.add(cb.equal(priceServiceRoot.get("provider"), root));
                pricePredicates.add(cb.isTrue(priceServiceRoot.get("isActive")));
                if (minPrice != null) {
                    pricePredicates.add(cb.greaterThanOrEqualTo(priceServiceRoot.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    pricePredicates.add(cb.lessThanOrEqualTo(priceServiceRoot.get("price"), maxPrice));
                }
                priceSub.select(priceServiceRoot.get("id"));
                priceSub.where(cb.and(pricePredicates.toArray(new Predicate[0])));
                predicates.add(cb.exists(priceSub));
            }
            if (Boolean.TRUE.equals(hasPhotos)) {
                Subquery<Long> photoSub = query.subquery(Long.class);
                var photoRoot = photoSub.from(ServiceImage.class);
                Join<Object, Object> photoService = photoRoot.join("service", JoinType.INNER);
                photoSub.select(photoRoot.get("id"));
                photoSub.where(cb.and(
                        cb.equal(photoService.get("provider"), root),
                        cb.isTrue(photoService.get("isActive"))
                ));
                predicates.add(cb.exists(photoSub));
            }
            if (serviceType != null && !serviceType.isBlank()) {
                Subquery<Long> typeSub = query.subquery(Long.class);
                var typeServiceRoot = typeSub.from(ServiceOffering.class);
                typeSub.select(typeServiceRoot.get("id"));
                typeSub.where(cb.and(
                        cb.equal(typeServiceRoot.get("provider"), root),
                        cb.isTrue(typeServiceRoot.get("isActive")),
                        cb.equal(typeServiceRoot.get("title"), serviceType)
                ));
                predicates.add(cb.exists(typeSub));
            }
            if (availability != null && !availability.isBlank()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime rangeStart = null;
                LocalDateTime rangeEnd = null;
                boolean weekHeuristic = "semana".equalsIgnoreCase(availability);
                if ("manana".equalsIgnoreCase(availability)) {
                    rangeStart = now.toLocalDate().plusDays(1).atStartOfDay();
                    rangeEnd = rangeStart.plusDays(1);
                } else if (weekHeuristic) {
                    rangeStart = now.toLocalDate().atStartOfDay();
                    rangeEnd = rangeStart.plusDays(7);
                } else if ("fecha".equalsIgnoreCase(availability)) {
                    // Fecha especifica elegida por el cliente; sin fecha no hay nada que filtrar.
                    if (date != null) {
                        rangeStart = date.atStartOfDay();
                        rangeEnd = rangeStart.plusDays(1);
                    }
                } else {
                    // "hoy" y tambien el alias "urgente" del frontend.
                    rangeStart = now.toLocalDate().atStartOfDay();
                    rangeEnd = rangeStart.plusDays(1);
                }

                if (rangeStart != null) {
                    Subquery<Long> bookingSub = query.subquery(Long.class);
                    var bookingRoot = bookingSub.from(Booking.class);
                    Predicate blockingBooking = cb.and(
                            cb.equal(bookingRoot.get("provider"), root),
                            cb.not(bookingRoot.get("status").in(BookingStatus.SLOT_RELEASED)),
                            cb.between(bookingRoot.get("scheduledAt"), rangeStart, rangeEnd)
                    );
                    if (weekHeuristic) {
                        // No hay tabla de horarios: se aproxima "disponible esta semana" a
                        // "no tiene la semana saturada" (menos de una reserva por dia en promedio).
                        bookingSub.select(cb.count(bookingRoot));
                        bookingSub.where(blockingBooking);
                        predicates.add(cb.lessThan(bookingSub, 7L));
                    } else {
                        bookingSub.select(bookingRoot.get("id"));
                        bookingSub.where(blockingBooking);
                        predicates.add(cb.not(cb.exists(bookingSub)));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
