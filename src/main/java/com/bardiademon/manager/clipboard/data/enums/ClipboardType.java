package com.bardiademon.manager.clipboard.data.enums;

import java.awt.datatransfer.DataFlavor;

public enum ClipboardType {
    STRING(DataFlavor.stringFlavor),
    FILE(DataFlavor.javaFileListFlavor),
    IMAGE(DataFlavor.imageFlavor),
    ;
    private final DataFlavor flavor;

    ClipboardType(DataFlavor flavor) {
        this.flavor = flavor;
    }

    public DataFlavor getFlavor() {
        return flavor;
    }
}
