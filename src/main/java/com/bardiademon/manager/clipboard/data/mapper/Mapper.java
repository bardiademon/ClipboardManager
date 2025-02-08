package com.bardiademon.manager.clipboard.data.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.bardiademon.manager.clipboard.controller.DataSourceProvider.*;

public final class Mapper {


    private Mapper() {
    }

    public static LocalDateTime toLocalDateTime(final String name, final ResultSet resultSet) throws SQLException {
        return toLocalDateTime(name, resultSet, SQL_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(final String name, final ResultSet resultSet, final DateTimeFormatter dateTimeFormatter) throws SQLException {
        if (resultSet.getObject(name) != null && resultSet.getObject(name) instanceof final String dateTime) {
            try {
                return LocalDateTime.parse(dateTime, dateTimeFormatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }


    public static boolean isNotFoundInfo(final ResultSet resultSet) throws SQLException {
        return (resultSet == null || !resultSet.last() || !resultSet.isLast() || resultSet.getRow() == 0 || !resultSet.first() || !resultSet.isFirst());
    }
}
