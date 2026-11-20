package cl.duoc.tecnoshop.service;
import cl.duoc.tecnoshop.dto.CompraDto;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CompraServiceTest {
    BoletaRepository boletas; DetalleBoletaRepository detalles; ProductoRepository productos; UsuarioRepository usuarios; CompraService service;
    @BeforeEach void setup() {
        boletas=mock(BoletaRepository.class); detalles=mock(DetalleBoletaRepository.class); productos=mock(ProductoRepository.class); usuarios=mock(UsuarioRepository.class);
        service=new CompraService(boletas,detalles,productos,usuarios);
        when(usuarios.findByEmail("ana@example.test")).thenReturn(Optional.of(new Usuario()));
        when(boletas.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
    Producto product(int stock) { var p=new Producto(); p.setStock(stock); p.setPrecio(new BigDecimal("1990.50")); return p; }
    CompraDto compra(int cantidad) { return new CompraDto(List.of(new CompraDto.Item(1L,cantidad))); }
    @Test void calculaTotalConPrecioServidorYDescuentaStock() {
        var p=product(5); when(productos.findById(1L)).thenReturn(Optional.of(p));
        var result=service.comprar("ana@example.test", compra(2));
        assertEquals(new BigDecimal("3981.00"),result.getTotal()); assertEquals(3,p.getStock());
        var captor=org.mockito.ArgumentCaptor.forClass(DetalleBoleta.class); verify(detalles).save(captor.capture());
        assertEquals(2,captor.getValue().getCantidad()); assertEquals(p.getPrecio(),captor.getValue().getPrecioUnitario());
    }
    @Test void rechazaStockInsuficiente() {
        var p=product(1); when(productos.findById(1L)).thenReturn(Optional.of(p));
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.comprar("ana@example.test",compra(2))).getStatusCode().value());
        assertEquals(1,p.getStock()); verify(detalles,never()).save(any());
    }
    @Test void rechazaProductoInexistente() {
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.comprar("ana@example.test",compra(1))).getStatusCode().value());
    }
    @Test void rechazaUsuarioInexistente() {
        assertEquals(401,assertThrows(ResponseStatusException.class,()->service.comprar("nadie@example.test",compra(1))).getStatusCode().value());
        verify(boletas,never()).save(any());
    }
}
