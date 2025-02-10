package com.bardiademon.manager.clipboard.service;

import io.vertx.core.AsyncResult;
import io.vertx.ext.sql.ResultSet;

public sealed class Service permits ClipboardService {

    public boolean isEmptyDaoSelect(AsyncResult<ResultSet> handler) {
        return handler == null || handler.result() == null || handler.result().getRows() == null || handler.result().getRows().isEmpty();
    }

}
