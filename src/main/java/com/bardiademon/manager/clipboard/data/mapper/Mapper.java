package com.bardiademon.manager.clipboard.data.mapper;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.bardiademon.manager.clipboard.controller.DatabaseConnection.*;

public final class Mapper {


    private Mapper() {
    }

    public static LocalDateTime toLocalDateTime(final String name, final JsonObject resultSet) {
        return toLocalDateTime(name, resultSet, SQL_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(final String name, final JsonObject row, final DateTimeFormatter dateTimeFormatter) {
        if (row.getValue(name) != null && row.getValue(name) instanceof final String dateTime) {
            try {
                return LocalDateTime.parse(dateTime, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

}
