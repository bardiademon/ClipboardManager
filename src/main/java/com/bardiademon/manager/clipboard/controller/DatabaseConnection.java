package com.bardiademon.manager.clipboard.controller;

import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.bardiademon.manager.clipboard.util.Paths.*;

public final class DatabaseConnection {

    private final static Logger logger = LogManager.getLogger(DatabaseConnection.class);

    public static final DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final String CONNECTION_URL = "jdbc:sqlite:" + DB_PATH;
    private static final int POOL_SIZE = 20;

    private static SQLClient jdbcClient;

    private DatabaseConnection() {
    }

    public static Future<SQLConnection> connect(Vertx vertx) {
        Promise<SQLConnection> promise = Promise.promise();

        File dbFile = new File(DB_PATH);

        if (!dbFile.exists()) {
            if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
                logger.error("Cannot create directories database, DbFile: {}", dbFile);
                promise.fail(new RuntimeException("Cannot create directories database, DbFile: " + dbFile.getAbsolutePath()));
                return promise.future();
            }
            try (InputStream dbSqlite3 = ClipboardManagerApplication.class.getResourceAsStream(RESOURCE_DB_SQLITE3)) {
                Objects.requireNonNull(dbSqlite3);
                long copy = Files.copy(dbSqlite3, dbFile.toPath());
                if (copy <= 0) {
                    throw new IOException("Failed copy sqlite3");
                }
            } catch (IOException | NullPointerException e) {
                logger.error("Failed to copy sqlite, DbFile: {}", dbFile, e);
                promise.fail(new RuntimeException(e));
                return promise.future();
            }
        }

        JsonObject config = new JsonObject()
                .put("url", CONNECTION_URL)
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", POOL_SIZE);

        logger.trace("Starting get connection, Config: {}", config);

        jdbcClient = JDBCClient.createShared(vertx, config);

        jdbcClient.getConnection(resultHandler -> {
            if (resultHandler.succeeded()) {
                logger.trace("Successfully connection database, Config: {}", config);
                resultHandler.result().close();
                promise.complete();
            } else {
                logger.error("Failed to connection database, Config: {}", config, resultHandler.cause());
                promise.fail(resultHandler.cause());
            }
        });

        return promise.future();
    }

    public static void close() {
        jdbcClient.close();
    }

    public static SQLClient getConnection() {
        return jdbcClient;
    }
}
