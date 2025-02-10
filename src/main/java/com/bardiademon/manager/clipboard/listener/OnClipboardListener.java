package com.bardiademon.manager.clipboard.listener;

import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import io.vertx.core.Future;

import java.io.File;
import java.util.List;

public interface OnClipboardListener {
    void onString(String data);

    void onImage(File image);

    void onFile(List<File> files);

    Future<Void> onData(ClipboardType clipboardType, Object data);
}
