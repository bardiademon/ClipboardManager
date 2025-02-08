package com.bardiademon.manager.clipboard.data.mapper;

import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClipboardMapper {

    private ClipboardMapper() {
    }

    public static List<ClipboardEntity> toClipboardEntities(ResultSet resultSet) throws SQLException {
        List<ClipboardEntity> clipboardEntities = new ArrayList<>();

        while (resultSet.next()) clipboardEntities.add(toClipboardEntity(resultSet));

        return clipboardEntities;
    }

    public static ClipboardEntity toClipboardEntity(ResultSet resultSet) throws SQLException {
        ClipboardEntity clipboardEntity = new ClipboardEntity();
        clipboardEntity.setId(resultSet.getInt("id"));
        clipboardEntity.setName(resultSet.getString("name"));
        clipboardEntity.setData(resultSet.getString("data"));
        clipboardEntity.setType(ClipboardType.valueOf(resultSet.getString("type")));
        clipboardEntity.setCreatedAt(Mapper.toLocalDateTime("created_at", resultSet));
        return clipboardEntity;
    }


}
