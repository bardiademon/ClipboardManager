package com.bardiademon.manager.clipboard.data.model.config;

import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.mapper.config.ShortcutMapper;

public record ConfigShortcutModel(int[] openUI, int[] closeApp, int[] clearAllClipboard, int[] deleteLastData, int[] clearSystemClipboard, int[] restart) {

    @Override
    public String toString() {
        return toJson().encode();
    }

    public JjsonObject toJson() {
        return new JjsonObject()
                .put("open_ui", ShortcutMapper.toString(openUI))
                .put("close_app", ShortcutMapper.toString(closeApp))
                .put("clear_all_clipboard", ShortcutMapper.toString(clearAllClipboard))
                .put("delete_last_data", ShortcutMapper.toString(deleteLastData))
                .put("clear_system_clipboard", ShortcutMapper.toString(clearSystemClipboard))
                .put("restart", ShortcutMapper.toString(restart))
                ;
    }
}
