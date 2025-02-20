package com.bardiademon.manager.clipboard.data.entity;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.exception.JjsonException;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;

import java.time.LocalDateTime;

public class ClipboardEntity {

    private int id;
    private String name;
    private String data;
    private ClipboardType type;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ClipboardType getType() {
        return type;
    }

    public void setType(ClipboardType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        if (type.equals(ClipboardType.STRING)) {
            return data.substring(0, Math.min(data.length(), 100));
        } else if (type.equals(ClipboardType.FILE)) {
            try {
                return String.format("Clipboard: %d file(s)", JjsonArray.ofString(data).size());
            } catch (JjsonException ignored) {
                return "Clipboard: file(s)";
            }
        } else if (type.equals(ClipboardType.IMAGE)) {
            return "Clipboard: Image";
        }
        return type.name();
    }

    public static ClipboardEntity emptyStringEntity() {
        ClipboardEntity clipboardEntity = new ClipboardEntity();
        clipboardEntity.setData("");
        clipboardEntity.setCreatedAt(LocalDateTime.now());
        clipboardEntity.setId(0);
        clipboardEntity.setType(ClipboardType.STRING);
        return clipboardEntity;
    }
}
