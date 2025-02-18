package com.bardiademon.manager.clipboard.data.model;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.mapper.ConfigMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record ConfigModel(int[] uiShortcut, List<ClipboardType> clipboardTypes, int clipboardSaveCount) {

    @Override
    public String toString() {
        return new JjsonObject()
                .put("ui_shortcut", Arrays.stream(uiShortcut).mapToObj(ConfigMapper::mapKeyCode).collect(Collectors.joining("+")))
                .put("clipboard_types", JjsonArray.ofCollection(clipboardTypes().stream().map(Enum::name).toList()))
                .put("clipboard_save_count", clipboardSaveCount())
                .encode();
    }
}
