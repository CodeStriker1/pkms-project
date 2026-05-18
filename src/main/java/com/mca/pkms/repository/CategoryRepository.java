package com.mca.pkms.repository;

import com.mca.pkms.entity.Category;
import com.mca.pkms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserOrderByNameAsc(User user);
    Optional<Category> findByIdAndUser(Long id, User user);
    boolean existsByNameIgnoreCaseAndUser(String name, User user);
}
