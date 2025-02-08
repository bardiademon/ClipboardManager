package com.bardiademon.manager.clipboard.data.model;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

import java.util.List;

public record ConfigModel(List<ClipboardType> clipboardTypes, int clipboardSaveCount) {

    @Override
    public String toString() {
        return new JjsonObject()
                .put("clipboard_types", JjsonArray.ofCollection(clipboardTypes().stream().map(Enum::name).toList()))
                .put("clipboard_save_count", clipboardSaveCount())
                .encode();
    }
}
