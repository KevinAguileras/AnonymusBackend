package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.model.Categoria;
import cl.duoc.tecnoshop.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/v1/categorias")
public class CategoriaController {
 private final CategoriaRepository repo;
 public CategoriaController(CategoriaRepository repo){this.repo=repo;}
 @GetMapping public List<Categoria> todos(){return repo.findAll();}
 @GetMapping("/{id}") public Categoria uno(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Categoria crear(@RequestBody Categoria c){return repo.save(c);}
 @PutMapping("/{id}") public Categoria actualizar(@PathVariable Long id,@RequestBody Categoria dto){Categoria c=uno(id);c.setNombre(dto.getNombre());return repo.save(c);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){uno(id);repo.deleteById(id);}
}
