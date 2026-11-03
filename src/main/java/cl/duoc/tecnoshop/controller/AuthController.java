package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.dto.AuthDtos.*;
import cl.duoc.tecnoshop.model.Usuario;
import cl.duoc.tecnoshop.repository.UsuarioRepository;
import cl.duoc.tecnoshop.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final UsuarioRepository usuarios;private final PasswordEncoder encoder;private final JwtService jwt;
 public AuthController(UsuarioRepository usuarios,PasswordEncoder encoder,JwtService jwt){this.usuarios=usuarios;this.encoder=encoder;this.jwt=jwt;}
 @PostMapping("/registro") public ResponseEntity<Token> registro(@Valid @RequestBody Registro dto){
  if(usuarios.existsByEmail(dto.email()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Correo registrado");
  Usuario u=new Usuario();u.setNombre(dto.nombre());u.setEmail(dto.email());u.setPasswordHash(encoder.encode(dto.password()));
  usuarios.save(u);return ResponseEntity.status(201).body(new Token(jwt.create(u.getEmail()),u.getNombre(),u.getRol().name()));
 }
 @PostMapping("/login") public Token login(@Valid @RequestBody Login dto){
  Usuario u=usuarios.findByEmail(dto.email()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales inválidas"));
  if(!encoder.matches(dto.password(),u.getPasswordHash()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales inválidas");
  return new Token(jwt.create(u.getEmail()),u.getNombre(),u.getRol().name());
 }
}
