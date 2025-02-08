package com.bardiademon.manager.clipboard.listener;

import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

import java.io.File;
import java.util.List;

public interface OnClipboardListener {
    void onString(String data);

    void onImage(File image);

    void onFile(List<File> files);

    void onData(ClipboardType clipboardType, Object data);
}
