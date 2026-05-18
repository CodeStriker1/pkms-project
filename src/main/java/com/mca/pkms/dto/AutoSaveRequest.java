package com.mca.pkms.dto;

import java.util.HashSet;
import java.util.Set;

public class AutoSaveRequest {
    private String title;
    private String content;
    private Long categoryId;
    private Set<Long> tagIds = new HashSet<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Set<Long> getTagIds() { return tagIds; }
    public void setTagIds(Set<Long> tagIds) { this.tagIds = tagIds == null ? new HashSet<>() : tagIds; }
}
