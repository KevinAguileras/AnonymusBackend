package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.dto.ProductoDto;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/v1/productos")
public class ProductoController {
 private final ProductoRepository repo;private final CategoriaRepository categorias;
 public ProductoController(ProductoRepository repo,CategoriaRepository categorias){this.repo=repo;this.categorias=categorias;}
 @GetMapping public List<Producto> todos(){return repo.findAll();}
 @GetMapping("/{id}") public Producto uno(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private Producto aplicar(Producto p,ProductoDto d){
  Categoria c=categorias.findById(d.categoriaId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Categoría inexistente"));
  p.setNombre(d.nombre());p.setCategoria(c);p.setPrecio(d.precio());p.setImg(d.img());p.setOferta(d.oferta());p.setPrecioAnterior(d.precioAnterior());p.setStock(d.stock());return p;
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Producto crear(@Valid @RequestBody ProductoDto d){return repo.save(aplicar(new Producto(),d));}
 @PutMapping("/{id}") public Producto actualizar(@PathVariable Long id,@Valid @RequestBody ProductoDto d){return repo.save(aplicar(uno(id),d));}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){uno(id);repo.deleteById(id);}
}
