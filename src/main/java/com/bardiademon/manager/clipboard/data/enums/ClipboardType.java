package com.bardiademon.manager.clipboard.data.enums;

import java.awt.datatransfer.DataFlavor;

public enum ClipboardType {
    EMPTY(null),
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


    public static ClipboardType[] getValues() {
        return new ClipboardType[]{STRING, FILE, IMAGE};
    }
}
