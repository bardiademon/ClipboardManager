package com.bardiademon.manager.clipboard.data.repository;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import io.vertx.core.Future;

import java.util.List;

public interface ClipboardRepository {

    Future<Void> saveClipboard(String name, String data, ClipboardType clipboardType);

    Future<List<ClipboardEntity>> fetchClipboards(int start, int end);

    Future<List<ClipboardEntity>> fetchClipboards();

    Future<Integer> fetchTotalClipboards();

    Future<Void> deleteClipboard(int limit);

    Future<Void> deleteClipboardById(int id);

    Future<Void> deleteAllClipboard();

}
