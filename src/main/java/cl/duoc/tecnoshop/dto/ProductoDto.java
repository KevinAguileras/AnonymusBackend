package cl.duoc.tecnoshop.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductoDto(@NotBlank String nombre,@NotNull Long categoriaId,@NotNull @DecimalMin("0.0") BigDecimal precio,String img,boolean oferta,BigDecimal precioAnterior,@NotNull @Min(0) Integer stock) {}
