package com.bardiademon.manager.clipboard.data.entity;

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
        return data.substring(0, Math.min(data.length(), 50));
    }
}
