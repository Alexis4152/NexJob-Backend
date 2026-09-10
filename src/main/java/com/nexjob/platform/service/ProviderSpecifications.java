package com.nexjob.platform.service;

import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.ServiceOffering;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Construye el filtro dinamico de busqueda de prestadores en el catalogo publico. */
public final class ProviderSpecifications {

    private ProviderSpecifications() {
    }

    public static Specification<ProviderProfile> search(Long categoryId, String keyword, String city, BigDecimal minRating) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
