package com.mca.pkms.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorite {
    @EmbeddedId
    private UserFavoriteId id = new UserFavoriteId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("noteId")
    private Note note;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Favorite() {
    }

    public Favorite(User user, Note note) {
        this.user = user;
        this.note = note;
        this.id = new UserFavoriteId(user.getId(), note.getId());
    }

    public UserFavoriteId getId() { return id; }
    public void setId(UserFavoriteId id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
