package com.mca.pkms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TagForm {
    @NotBlank
    @Size(max = 60)
    private String name;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$")
    private String color = "#0f766e";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
