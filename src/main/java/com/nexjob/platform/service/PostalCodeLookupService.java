package com.nexjob.platform.service;

import com.nexjob.platform.entity.PostalCode;
import com.nexjob.platform.repository.PostalCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Traduce un codigo postal mexicano a una coordenada aproximada, usando el catalogo
 * cargado en db/06_postal_codes.sql. Un mismo CP suele cubrir varias colonias con
 * centroides ligeramente distintos; se promedian para no obligar al prestador a elegir
 * su colonia exacta. Sigue siendo una aproximacion (a nivel CP, no direccion exacta),
 * pero mucho mas fina que el centroide por ciudad que se usaba antes.
 */
@Service
@RequiredArgsConstructor
public class PostalCodeLookupService {

    private final PostalCodeRepository postalCodeRepository;

    public record Coordinates(BigDecimal latitude, BigDecimal longitude) {
    }

    public Optional<Coordinates> lookup(String postalCode) {
        if (postalCode == null || postalCode.isBlank()) {
            return Optional.empty();
        }
        List<PostalCode> matches = postalCodeRepository.findByPostalCode(postalCode.trim());
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal latSum = BigDecimal.ZERO;
        BigDecimal lngSum = BigDecimal.ZERO;
        for (PostalCode match : matches) {
            latSum = latSum.add(match.getLatitude());
            lngSum = lngSum.add(match.getLongitude());
        }
        BigDecimal count = BigDecimal.valueOf(matches.size());
        BigDecimal avgLat = latSum.divide(count, 6, RoundingMode.HALF_UP);
        BigDecimal avgLng = lngSum.divide(count, 6, RoundingMode.HALF_UP);
        return Optional.of(new Coordinates(avgLat, avgLng));
    }
}
