package com.mca.pkms.service;

import com.mca.pkms.dto.CategoryForm;
import com.mca.pkms.entity.Category;
import com.mca.pkms.entity.User;
import com.mca.pkms.exception.BadRequestException;
import com.mca.pkms.exception.ResourceNotFoundException;
import com.mca.pkms.repository.CategoryRepository;
import com.mca.pkms.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final NoteRepository noteRepository;

    public CategoryService(CategoryRepository categoryRepository, NoteRepository noteRepository) {
        this.categoryRepository = categoryRepository;
        this.noteRepository = noteRepository;
    }

    public List<Category> list(User user) {
        return categoryRepository.findByUserOrderByNameAsc(user);
    }

    public Category find(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    @Transactional
    public Category save(CategoryForm form, User user) {
        if (categoryRepository.existsByNameIgnoreCaseAndUser(form.getName().trim(), user)) {
            throw new BadRequestException("Category already exists.");
        }
        Category category = new Category();
        apply(category, form, user);
        return categoryRepository.save(category);
    }

    @Transactional
    public void update(Long id, CategoryForm form, User user) {
        Category category = find(id, user);
        apply(category, form, user);
        categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id, User user) {
        Category category = find(id, user);
        noteRepository.clearCategory(category.getId(), user);
        categoryRepository.delete(category);
    }

    private void apply(Category category, CategoryForm form, User user) {
        category.setName(form.getName().trim());
        category.setDescription(form.getDescription());
        category.setColor(form.getColor());
        category.setUser(user);
    }
}
