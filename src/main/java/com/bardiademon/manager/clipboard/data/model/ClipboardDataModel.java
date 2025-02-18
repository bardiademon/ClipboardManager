package com.bardiademon.manager.clipboard.data.model;

import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

public final class ClipboardDataModel<T> {

    private T data;
    private ClipboardType type;

    public ClipboardDataModel() {
    }

    public ClipboardDataModel(T data, ClipboardType type) {
        this.data = data;
        this.type = type;
    }

    public ClipboardType getType() {
        return type;
    }

    public T getData() {
        return data;
    }

    public void setLast(T data, ClipboardType type) {
        this.data = data;
        this.type = type;
    }

    public void clear() {
        if (this.data instanceof String) {
            this.data = (T) "";
        } else {
            this.data = null;
        }
    }

    public boolean equals(T data, ClipboardType type) {
        if (getData() == null || getType() == null || getType().equals(ClipboardType.EMPTY) || data == null || type == null || type.equals(ClipboardType.EMPTY)) {
            return false;
        }
        return getType().equals(type) && getData().equals(data);
    }

    public boolean equalsIfNotDoSet(T data, ClipboardType type) {
        if (equals(data, type)) {
            return true;
        }

        setLast(data, type);
        return false;
    }

}
