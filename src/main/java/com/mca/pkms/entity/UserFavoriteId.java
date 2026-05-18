package com.mca.pkms.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFavoriteId implements Serializable {
    private Long userId;
    private Long noteId;

    public UserFavoriteId() {
    }

    public UserFavoriteId(Long userId, Long noteId) {
        this.userId = userId;
        this.noteId = noteId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFavoriteId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(noteId, that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, noteId);
    }
}
