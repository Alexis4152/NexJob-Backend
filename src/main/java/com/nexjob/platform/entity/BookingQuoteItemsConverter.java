package com.nexjob.platform.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/** Serializa/deserializa {@code Booking.quoteItems} como JSON en una sola columna TEXT. */
@Converter
public class BookingQuoteItemsConverter implements AttributeConverter<List<BookingQuoteItem>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<BookingQuoteItem> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar quoteItems", e);
        }
    }

    @Override
    public List<BookingQuoteItem> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(dbData, new TypeReference<List<BookingQuoteItem>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer quoteItems", e);
        }
    }
}
