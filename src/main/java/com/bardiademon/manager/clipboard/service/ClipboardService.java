package com.bardiademon.manager.clipboard.service;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.mapper.ClipboardMapper;
import com.bardiademon.manager.clipboard.data.repository.ClipboardRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.bardiademon.manager.clipboard.controller.DatabaseConnection.getConnection;

public non-sealed class ClipboardService extends Service implements ClipboardRepository {

    private final static Logger logger = LogManager.getLogger(ClipboardService.class);

    private static ClipboardRepository clipboardRepository;

    private ClipboardService() {
    }

    public static ClipboardRepository repository() {
        if (clipboardRepository == null) {
            clipboardRepository = new ClipboardService();
        }
        return clipboardRepository;
    }

    @Override
    public Future<Void> saveClipboard(String name, String data, ClipboardType clipboardType) {
        logger.trace("Starting saveClipboard, Name: {} , Data: {} , Type: {}", name, data, clipboardType);

        Promise<Void> promise = Promise.promise();

        String query = """
                insert into "clipboard" ("name", "data", "type") values (?, ?, ?)
                """;

        JsonArray params = new JsonArray()
                .add(name)
                .add(data)
                .add(clipboardType.name());

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to save clipboard, Name: {} , Data: {} , Type: {}", name, data, clipboardType, resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            logger.trace("Successfully save clipboard, Name: {} , Data: {} , Type: {}", name, data, clipboardType, resultHandler.cause());

            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<List<ClipboardEntity>> fetchClipboards(int start, int end) {
        logger.trace("Starting saveClipboard, Start: {} , End: {}", start, end);

        Promise<List<ClipboardEntity>> promise = Promise.promise();

        String query = """
                select
                        "id",
                        "name",
                        "data",
                        "type",
                        strftime('%Y/%m/%d %H:%M:%S', "created_at") as "created_at"
                from "clipboard"
                where "deleted_at" is null
                    limit ? offset ?
                order by "id" desc
                """;

        JsonArray params = new JsonArray()
                .add(start)
                .add(end);

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to fetch clipboards, Start: {} , End: {}", start, end, resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            if (isEmptyDaoSelect(resultHandler)) {
                logger.error("Not found clipboards, Start: {} , End: {}", start, end);
                promise.complete(List.of());
                return;
            }

            List<ClipboardEntity> clipboardEntities = ClipboardMapper.toClipboardEntities(resultHandler.result().getRows());

            logger.trace("Successfully fetch clipboard, Clipboard: {}", clipboardEntities);

            promise.complete(clipboardEntities);

        });

        return promise.future();
    }

    @Override
    public Future<List<ClipboardEntity>> fetchClipboards() {
        logger.trace("Starting fetchClipboards");

        Promise<List<ClipboardEntity>> promise = Promise.promise();

        logger.trace("Successfully connection");
        String query = """
                select
                        "id",
                        "name",
                        "data",
                        "type",
                        strftime('%Y/%m/%d %H:%M:%S', "created_at") as "created_at"
                from "clipboard"
                where "deleted_at" is null
                order by "id" desc
                """;

        JsonArray params = new JsonArray();

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to fetch clipboards", resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            if (isEmptyDaoSelect(resultHandler)) {
                logger.error("Not found clipboards");
                promise.complete(List.of());
                return;
            }

            List<ClipboardEntity> clipboardEntities = ClipboardMapper.toClipboardEntities(resultHandler.result().getRows());

            logger.trace("Successfully fetch clipboard, Clipboard: {}", clipboardEntities);

            promise.complete(clipboardEntities);

        });

        return promise.future();
    }

    @Override
    public Future<Integer> fetchTotalClipboards() {
        logger.trace("Starting fetchTotalClipboards");

        Promise<Integer> promise = Promise.promise();

        String query = """
                select count("id") as count from "clipboard"
                """;

        JsonArray params = new JsonArray();

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to fetch clipboards", resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            if (isEmptyDaoSelect(resultHandler)) {
                logger.error("Not found clipboards");
                promise.complete(0);
                return;
            }

            int count = resultHandler.result().getRows().getFirst().getInteger("count", 0);

            logger.trace("Successfully fetch clipboard, Count: {}", count);

            promise.complete(count);

        });

        return promise.future();
    }

    @Override
    public Future<Void> deleteClipboard(int limit) {
        logger.trace("Starting deleteClipboard, Limit: {}", limit);

        Promise<Void> promise = Promise.promise();

        String query = """
                delete from "clipboard" where "id" in (select "id" from "clipboard" order by "id" limit ?)
                """;

        JsonArray params = new JsonArray()
                .add(limit);

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to delete clipboard, Limit: {}", limit, resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            logger.trace("Successfully delete clipboard, Limit: {}", limit, resultHandler.cause());

            promise.complete();
        });

        return promise.future();

    }

    @Override
    public Future<Void> deleteClipboardById(int id) {
        logger.trace("Starting deleteClipboardById, Id: {}", id);

        Promise<Void> promise = Promise.promise();

        String query = """
                delete from "clipboard" where "id" = ?
                """;

        JsonArray params = new JsonArray()
                .add(id);

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to delete clipboard, Id: {}", id, resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            logger.trace("Successfully delete clipboard, Id: {}", id, resultHandler.cause());

            promise.complete();
        });

        return promise.future();
    }

    @Override
    public Future<Void> deleteAllClipboard() {
        logger.trace("Starting deleteAllClipboard");

        Promise<Void> promise = Promise.promise();

        String query = """
                delete from "clipboard"
                """;

        JsonArray params = new JsonArray();

        logger.trace("Executing -> Query: {} , Params: {} , ParamsSize: {}", query, params, params.size());
        getConnection().queryWithParams(query, params, resultHandler -> {
            if (resultHandler.failed()) {
                logger.error("Failed to delete clipboard", resultHandler.cause());
                promise.fail(resultHandler.cause());
                return;
            }

            logger.trace("Successfully delete clipboard", resultHandler.cause());

            promise.complete();
        });

        return promise.future();
    }

}
