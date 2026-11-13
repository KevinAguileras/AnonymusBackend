package cl.duoc.tecnoshop.controller;

import cl.duoc.tecnoshop.dto.CategoriaDto;
import cl.duoc.tecnoshop.model.Categoria;
import cl.duoc.tecnoshop.repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {
 private final CategoriaRepository repo;
 public CategoriaController(CategoriaRepository repo) { this.repo = repo; }

 @GetMapping public List<Categoria> todos() { return repo.findAll(); }
 @GetMapping("/{id}") public Categoria uno(@PathVariable Long id) {
  return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
 }
 private String validarNombre(String nombre, Long idActual) {
  String limpio = nombre.trim();
  repo.findByNombreIgnoreCase(limpio).ifPresent(existente -> {
   if (!existente.getId().equals(idActual))
    throw new ResponseStatusException(HttpStatus.CONFLICT, "La categoría ya existe");
  });
  return limpio;
 }
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public Categoria crear(@Valid @RequestBody CategoriaDto dto) {
  Categoria c = new Categoria();
  c.setNombre(validarNombre(dto.nombre(), null));
  return repo.save(c);
 }
 @PutMapping("/{id}") public Categoria actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto dto) {
  Categoria c = uno(id);
  c.setNombre(validarNombre(dto.nombre(), id));
  return repo.save(c);
 }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void eliminar(@PathVariable Long id) {
  uno(id);
  repo.deleteById(id);
 }
}
