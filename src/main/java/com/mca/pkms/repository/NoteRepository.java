package com.mca.pkms.repository;

import com.mca.pkms.entity.Note;
import com.mca.pkms.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @EntityGraph(attributePaths = {"category", "tags"})
    List<Note> findByUserAndDeletedFalseAndArchivedFalseOrderByUpdatedAtDesc(User user);

    @EntityGraph(attributePaths = {"category", "tags"})
    List<Note> findByUserAndDeletedFalseAndArchivedTrueOrderByUpdatedAtDesc(User user);

    @EntityGraph(attributePaths = {"category", "tags"})
    List<Note> findByUserAndDeletedTrueOrderByDeletedAtDesc(User user);

    @EntityGraph(attributePaths = {"category", "tags"})
    Optional<Note> findByIdAndUser(Long id, User user);

    long countByUserAndDeletedFalse(User user);
    long countByUserAndDeletedFalseAndArchivedFalse(User user);
    long countByUserAndDeletedFalseAndArchivedTrue(User user);
    long countByUserAndDeletedTrue(User user);

    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("""
            select distinct n from Note n
            left join n.tags t
            where n.user = :user and n.deleted = false
              and (:archived is null or n.archived = :archived)
              and (:categoryId is null or n.category.id = :categoryId)
              and (:tagIdsEmpty = true or t.id in :tagIds)
            """)
    List<Note> filter(@Param("user") User user,
                      @Param("archived") Boolean archived,
                      @Param("categoryId") Long categoryId,
                      @Param("tagIds") Collection<Long> tagIds,
                      @Param("tagIdsEmpty") boolean tagIdsEmpty);

    @Modifying
    @Query("update Note n set n.category = null where n.category.id = :categoryId and n.user = :user")
    void clearCategory(@Param("categoryId") Long categoryId, @Param("user") User user);
}
