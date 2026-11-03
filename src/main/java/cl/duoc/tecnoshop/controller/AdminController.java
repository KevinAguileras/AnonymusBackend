package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin/usuarios")
public class AdminController {
 private final UsuarioRepository usuarios;
 public AdminController(UsuarioRepository usuarios){this.usuarios=usuarios;}
 @GetMapping public List<Usuario> listar(){return usuarios.findAll();}
 @PutMapping("/{id}/rol") public Usuario rol(@PathVariable Long id,@RequestBody Map<String,String> body){
  Usuario u=usuarios.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  try{u.setRol(Rol.valueOf(body.get("rol")));}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rol inválido");}
  return usuarios.save(u);
 }
}
