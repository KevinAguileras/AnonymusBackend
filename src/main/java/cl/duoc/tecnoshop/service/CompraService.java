package cl.duoc.tecnoshop.service;

import cl.duoc.tecnoshop.dto.CompraDto;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
@Service
public class CompraService {
 private final BoletaRepository boletas;private final DetalleBoletaRepository detalles;private final ProductoRepository productos;private final UsuarioRepository usuarios;
 public CompraService(BoletaRepository b,DetalleBoletaRepository d,ProductoRepository p,UsuarioRepository u){boletas=b;detalles=d;productos=p;usuarios=u;}
 @Transactional public Boleta comprar(String email,CompraDto dto){
  Usuario usuario=usuarios.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  Boleta b=new Boleta();b.setUsuario(usuario);b.setEstado("PAGADA");b.setTotal(BigDecimal.ZERO);b=boletas.save(b);
  BigDecimal total=BigDecimal.ZERO;
  for(CompraDto.Item item:dto.items()){
   Producto p=productos.findById(item.productoId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Producto inexistente"));
   if(p.getStock()==null||p.getStock()<item.cantidad())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Stock insuficiente");
   p.setStock(p.getStock()-item.cantidad());productos.save(p);
   DetalleBoleta d=new DetalleBoleta();d.setBoleta(b);d.setProducto(p);d.setCantidad(item.cantidad());d.setPrecioUnitario(p.getPrecio());detalles.save(d);
   total=total.add(p.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
  }
  b.setTotal(total);return boletas.save(b);
 }
}
