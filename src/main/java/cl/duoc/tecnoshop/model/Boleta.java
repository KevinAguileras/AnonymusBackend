package cl.duoc.tecnoshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="boletas")
public class Boleta {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="usuario_id") private Usuario usuario;
 @Column(nullable=false) private LocalDateTime fecha=LocalDateTime.now();
 @Column(nullable=false,precision=12,scale=2) private BigDecimal total;
 private String estado;
 public Boleta() {}
 public Long getId(){return id;}
 public Usuario getUsuario(){return usuario;}
 public void setUsuario(Usuario usuario){this.usuario=usuario;}
 public LocalDateTime getFecha(){return fecha;}
 public void setFecha(LocalDateTime fecha){this.fecha=fecha;}
 public BigDecimal getTotal(){return total;}
 public void setTotal(BigDecimal total){this.total=total;}
 public String getEstado(){return estado;}
 public void setEstado(String estado){this.estado=estado;}
}
