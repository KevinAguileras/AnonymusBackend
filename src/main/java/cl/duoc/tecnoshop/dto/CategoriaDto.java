package cl.duoc.tecnoshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaDto(@NotBlank @Size(max = 100) String nombre) {}
