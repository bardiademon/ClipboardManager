package com.bardiademon.manager.clipboard.data.model.config;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.enums.UILookAndFeelType;

import java.util.List;

public record ConfigModel(UILookAndFeelType theme, ConfigShortcutModel shortcuts, List<ClipboardType> clipboardTypes, int clipboardSaveCount, ClipboardHandlerPeriod clipboardHandlerPeriod) {

    @Override
    public String toString() {
        return toJson().encode();
    }

    public JjsonObject toJson() {
        return new JjsonObject()
                .put("theme", theme.name())
                .put("shortcuts", shortcuts.toJson())
                .put("clipboard_types", JjsonArray.ofCollection(clipboardTypes.stream().map(Enum::name).toList()))
                .put("clipboard_save_count", clipboardSaveCount)
                .put("clipboard_handler_period", clipboardHandlerPeriod.toJson())
                ;
    }
}
