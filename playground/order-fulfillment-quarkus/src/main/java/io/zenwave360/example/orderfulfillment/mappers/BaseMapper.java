package io.zenwave360.example.orderfulfillment.mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface BaseMapper {

    default Instant asInstant(OffsetDateTime date) {
        return date == null ? null : date.toInstant();
    }

    default OffsetDateTime asOffsetDateTime(Instant date) {
        return date == null ? null : OffsetDateTime.ofInstant(date, ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : OffsetDateTime.of(value, ZoneOffset.UTC);
    }
}
