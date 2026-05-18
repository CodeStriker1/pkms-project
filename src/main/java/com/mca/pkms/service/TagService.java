package com.mca.pkms.service;

import com.mca.pkms.dto.TagForm;
import com.mca.pkms.entity.Tag;
import com.mca.pkms.entity.User;
import com.mca.pkms.exception.BadRequestException;
import com.mca.pkms.exception.ResourceNotFoundException;
import com.mca.pkms.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> list(User user) {
        return tagRepository.findByUserOrderByNameAsc(user);
    }

    public Tag find(Long id, User user) {
        return tagRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
    }

    @Transactional
    public Tag save(TagForm form, User user) {
        if (tagRepository.existsByNameIgnoreCaseAndUser(form.getName().trim(), user)) {
            throw new BadRequestException("Tag already exists.");
        }
        Tag tag = new Tag();
        tag.setName(form.getName().trim());
        tag.setColor(form.getColor());
        tag.setUser(user);
        return tagRepository.save(tag);
    }

    @Transactional
    public void delete(Long id, User user) {
        Tag tag = find(id, user);
        tagRepository.detachFromNotes(tag.getId());
        tagRepository.delete(tag);
    }
}
