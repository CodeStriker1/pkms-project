package com.mca.pkms.repository;

import com.mca.pkms.entity.Favorite;
import com.mca.pkms.entity.User;
import com.mca.pkms.entity.UserFavoriteId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, UserFavoriteId> {
    boolean existsByUser_IdAndNote_Id(Long userId, Long noteId);
    void deleteByUser_IdAndNote_Id(Long userId, Long noteId);
    void deleteByNote_Id(Long noteId);

    @EntityGraph(attributePaths = {"note", "note.category", "note.tags"})
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
}
