package com.bardiademon.manager.clipboard.data.repository;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

import java.util.List;

public interface ClipboardRepository {

    void saveClipboard(String name, String data, ClipboardType clipboardType);

    List<ClipboardEntity> fetchClipboards(int start, int end);

    List<ClipboardEntity> fetchClipboards();

    int fetchTotalClipboards();

    void deleteClipboard(int limit);

    void deleteClipboardById(int id);

    void deleteAllClipboard();

}
