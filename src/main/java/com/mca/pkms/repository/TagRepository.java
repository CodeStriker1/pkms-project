package com.mca.pkms.repository;

import com.mca.pkms.entity.Tag;
import com.mca.pkms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUserOrderByNameAsc(User user);
    Set<Tag> findByIdInAndUser(Collection<Long> ids, User user);
    Optional<Tag> findByIdAndUser(Long id, User user);
    boolean existsByNameIgnoreCaseAndUser(String name, User user);

    @Modifying
    @Query(value = "delete from note_tags where tag_id = :tagId", nativeQuery = true)
    void detachFromNotes(@Param("tagId") Long tagId);
}
