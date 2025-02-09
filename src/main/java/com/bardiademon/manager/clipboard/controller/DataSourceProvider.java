package com.bardiademon.manager.clipboard.controller;

import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.bardiademon.manager.clipboard.util.Paths.*;

public final class DataSourceProvider {

    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final String CONNECTION_URL = "jdbc:sqlite://" + DB_PATH;
    private static final int POOL_SIZE = 20;

    private static HikariDataSource dataSource;

    private DataSourceProvider() {
    }

    public static void setDataSource() {

        File dbFile = new File(DB_PATH);

        if (!dbFile.exists()) {
            if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Cannot create directories database, DbFile: " + dbFile.getAbsolutePath());
            }
            try (InputStream dbSqlite3 = ClipboardManagerApplication.class.getResourceAsStream(RESOURCE_DB_SQLITE3)) {
                Objects.requireNonNull(dbSqlite3);
                long copy = Files.copy(dbSqlite3, dbFile.toPath());
                if (copy <= 0) {
                    throw new IOException("Failed copy sqlite3");
                }
            } catch (IOException | NullPointerException e) {
                throw new RuntimeException(e);
            }
        }

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
