package cl.duoc.tecnoshop.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record CompraDto(@NotEmpty List<@Valid Item> items) {
 public record Item(@NotNull Long productoId,@NotNull @Min(1) Integer cantidad) {}
}
