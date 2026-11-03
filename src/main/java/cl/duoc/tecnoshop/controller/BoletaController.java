package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.dto.CompraDto;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.*;
import cl.duoc.tecnoshop.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/v1/boletas")
public class BoletaController {
 private final BoletaRepository boletas;private final DetalleBoletaRepository detalles;private final UsuarioRepository usuarios;private final CompraService compras;
 public BoletaController(BoletaRepository b,DetalleBoletaRepository d,UsuarioRepository u,CompraService c){boletas=b;detalles=d;usuarios=u;compras=c;}
 private boolean empleado(Authentication auth){return auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMINISTRADOR")||a.getAuthority().equals("ROLE_VENDEDOR"));}
 @GetMapping public List<Boleta> listar(Authentication auth){
  if(empleado(auth))return boletas.findAll();
  Usuario u=usuarios.findByEmail(auth.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));return boletas.findByUsuarioId(u.getId());
 }
 @GetMapping("/{id}") public Map<String,Object> detalle(@PathVariable Long id,Authentication auth){
  Boleta b=boletas.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!empleado(auth)&&!b.getUsuario().getEmail().equals(auth.getName()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return Map.of("boleta",b,"detalles",detalles.findByBoletaId(id));
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Boleta comprar(@Valid @RequestBody CompraDto dto,Authentication auth){return compras.comprar(auth.getName(),dto);}
}
