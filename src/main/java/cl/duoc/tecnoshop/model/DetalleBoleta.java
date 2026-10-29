package cl.duoc.tecnoshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="detalles_boleta")
public class DetalleBoleta {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="boleta_id") private Boleta boleta;
 @ManyToOne(optional=false) @JoinColumn(name="producto_id") private Producto producto;
 @Column(nullable=false) private Integer cantidad;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal precioUnitario;
 public DetalleBoleta() {}
 public Long getId(){return id;}
 public Boleta getBoleta(){return boleta;}
 public void setBoleta(Boleta boleta){this.boleta=boleta;}
 public Producto getProducto(){return producto;}
 public void setProducto(Producto producto){this.producto=producto;}
 public Integer getCantidad(){return cantidad;}
 public void setCantidad(Integer cantidad){this.cantidad=cantidad;}
 public BigDecimal getPrecioUnitario(){return precioUnitario;}
 public void setPrecioUnitario(BigDecimal precioUnitario){this.precioUnitario=precioUnitario;}
}
