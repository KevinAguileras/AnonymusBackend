package cl.duoc.tecnoshop.controller;

import cl.duoc.tecnoshop.model.Usuario;
import cl.duoc.tecnoshop.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cuenta")
public class CuentaController {
    private final UsuarioRepository usuarios;
    public CuentaController(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    public record Perfil(String nombre, String email, String rol) {}
    public record Actualizacion(@NotBlank String nombre) {}
    private Usuario actual(Authentication auth) {
        return usuarios.findByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    private Perfil perfil(Usuario usuario) { return new Perfil(usuario.getNombre(), usuario.getEmail(), usuario.getRol().name()); }
    @GetMapping public Perfil obtener(Authentication auth) { return perfil(actual(auth)); }
    @PutMapping public Perfil actualizar(Authentication auth, @Valid @RequestBody Actualizacion dto) {
        Usuario usuario = actual(auth);
        usuario.setNombre(dto.nombre().trim());
        return perfil(usuarios.save(usuario));
    }
}
