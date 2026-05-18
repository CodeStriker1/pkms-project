package com.mca.pkms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryForm {
    @NotBlank
    @Size(max = 80)
    private String name;

    @Size(max = 255)
    private String description;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    private String color = "#2563eb";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
