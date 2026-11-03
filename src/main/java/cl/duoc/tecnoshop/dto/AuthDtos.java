package cl.duoc.tecnoshop.dto;
import jakarta.validation.constraints.*;
public class AuthDtos {
 public record Registro(@NotBlank String nombre,@Email @NotBlank String email,@Size(min=6) String password) {}
 public record Login(@Email @NotBlank String email,@NotBlank String password) {}
 public record Token(String token,String nombre,String rol) {}
}
