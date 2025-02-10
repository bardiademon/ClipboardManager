package com.bardiademon.manager.clipboard.data.mapper;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ClipboardMapper {

    private ClipboardMapper() {
    }

    public static List<ClipboardEntity> toClipboardEntities(List<JsonObject> rows) {
        List<ClipboardEntity> clipboardEntities = new ArrayList<>();

        for (JsonObject row : rows) {
            clipboardEntities.add(toClipboardEntity(row));
        }

        return clipboardEntities;
    }

    public static ClipboardEntity toClipboardEntity(JsonObject row) {
        ClipboardEntity clipboardEntity = new ClipboardEntity();
        clipboardEntity.setId(row.getInteger("id"));
        clipboardEntity.setName(row.getString("name"));
        clipboardEntity.setData(row.getString("data"));
        clipboardEntity.setType(ClipboardType.valueOf(row.getString("type")));
        clipboardEntity.setCreatedAt(Mapper.toLocalDateTime("created_at", row));
        return clipboardEntity;
    }


}
