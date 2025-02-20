package com.bardiademon.manager.clipboard.data.model.config;

import com.bardiademon.Jjson.object.JjsonObject;

public record ClipboardHandlerPeriod(int clipboardHandlerMills, int clipboardImageHandlerSec) {

    public JjsonObject toJson() {
        return new JjsonObject()
                .put("clipboard_handler_mills", clipboardHandlerMills)
                .put("clipboard_image_handler_sec", clipboardImageHandlerSec);
    }

    @Override
    public String toString() {
        return toJson().encode();
    }
}