package cl.duoc.tecnoshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="productos")
public class Producto {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private String nombre;
 @ManyToOne(optional=false) @JoinColumn(name="categoria_id") private Categoria categoria;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal precio;
 private String img;
 private boolean oferta;
 @Column(precision=12,scale=2) private BigDecimal precioAnterior;
 private Integer stock=0;
 public Producto() {}
 public Long getId(){return id;}
 public String getNombre(){return nombre;}
 public void setNombre(String nombre){this.nombre=nombre;}
 public Categoria getCategoria(){return categoria;}
 public void setCategoria(Categoria categoria){this.categoria=categoria;}
 public BigDecimal getPrecio(){return precio;}
 public void setPrecio(BigDecimal precio){this.precio=precio;}
 public String getImg(){return img;}
 public void setImg(String img){this.img=img;}
 public boolean isOferta(){return oferta;}
 public void setOferta(boolean oferta){this.oferta=oferta;}
 public BigDecimal getPrecioAnterior(){return precioAnterior;}
 public void setPrecioAnterior(BigDecimal precioAnterior){this.precioAnterior=precioAnterior;}
 public Integer getStock(){return stock;}
 public void setStock(Integer stock){this.stock=stock;}
}
