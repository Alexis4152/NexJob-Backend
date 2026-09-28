package com.nexjob.platform.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Una linea del desglose que el prestador envia al cotizar un servicio "a cotizar" (ver
 * mockup "Cotizacion Detallada"): material/concepto, cantidad, unidad y costo unitario. No es
 * una entidad JPA propia: la lista completa se guarda como JSON en
 * {@code bookings.quote_items_json} (ver BookingQuoteItemsConverter).
 */
@Data
@NoArgsConstructor
public class BookingQuoteItem {
    private String concept;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitCost;
}
