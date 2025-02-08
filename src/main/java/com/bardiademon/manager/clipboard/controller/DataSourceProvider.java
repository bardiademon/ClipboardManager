package com.bardiademon.manager.clipboard.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.time.format.DateTimeFormatter;

public final class DataSourceProvider {

    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final String DB_NAME = "clipboard_manager.sqlite3";
    private static final String DB_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + DB_NAME;

    private static final String
            CONNECTION_URL = "jdbc:sqlite://" + DB_PATH;
    private static final int POOL_SIZE = 500;

    private static HikariDataSource dataSource;

    private DataSourceProvider() {
    }

    public static void setDataSource() {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(CONNECTION_URL);
        hikariConfig.setMaximumPoolSize(POOL_SIZE);
        dataSource = new HikariDataSource(hikariConfig);

        Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
