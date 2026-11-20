package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.config.SecurityConfig;
import cl.duoc.tecnoshop.security.*;
import cl.duoc.tecnoshop.repository.*;
import cl.duoc.tecnoshop.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(controllers={ProductoController.class,CuentaController.class,AuthController.class},properties={"app.jwt.secret=test-only-secret-123456789012345678901234567890","app.jwt.expiration-ms=60000"})
@Import({SecurityConfig.class,JwtService.class,JwtFilter.class})
class SeguridadApiTest {
    @Autowired MockMvc mvc; @Autowired JwtService jwt;
    @MockitoBean ProductoRepository productos; @MockitoBean CategoriaRepository categorias; @MockitoBean UsuarioRepository usuarios;
    String token(Rol rol) {
        Usuario u=new Usuario();u.setNombre("Ana");u.setEmail("ana@example.test");u.setRol(rol);
        when(usuarios.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(usuarios.save(any())).thenAnswer(inv->inv.getArgument(0));
        return "Bearer "+jwt.create(u.getEmail());
    }
    @Test void catalogoPublico() throws Exception { mvc.perform(get("/api/v1/productos")).andExpect(status().isOk()); }
    @Test void cuentaRequiereAutenticacion() throws Exception { mvc.perform(get("/api/v1/cuenta")).andExpect(status().isUnauthorized()); }
    @Test void clienteNoPuedeCrearProductos() throws Exception {
        mvc.perform(post("/api/v1/productos").header("Authorization",token(Rol.CLIENTE)).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden()); verify(productos,never()).save(any());
    }
    @Test void vendedorNoPuedeEliminarProductos() throws Exception {
        mvc.perform(delete("/api/v1/productos/1").header("Authorization",token(Rol.VENDEDOR))).andExpect(status().isForbidden());
    }
    @Test void administradorPuedeCrearProducto() throws Exception {
        when(categorias.findById(1L)).thenReturn(Optional.of(new Categoria()));
        when(productos.save(any())).thenAnswer(inv->inv.getArgument(0));
        mvc.perform(post("/api/v1/productos").header("Authorization",token(Rol.ADMINISTRADOR)).contentType("application/json")
            .content("{\"nombre\":\"Mouse\",\"categoriaId\":1,\"precio\":5000,\"stock\":3}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.stock").value(3));
    }
    @Test void cuentaNoExponeHashYActualizaNombre() throws Exception {
        mvc.perform(put("/api/v1/cuenta").header("Authorization",token(Rol.CLIENTE)).contentType("application/json").content("{\"nombre\":\"Ana nueva\",\"rol\":\"ADMINISTRADOR\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ana nueva"))
            .andExpect(jsonPath("$.rol").value("CLIENTE")).andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
    @Test void registroSinPasswordDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/auth/registro").contentType("application/json").content("{\"nombre\":\"Ana\",\"email\":\"ana@example.test\"}"))
            .andExpect(status().isBadRequest()); verify(usuarios,never()).save(any());
    }
    @Test void tokenInvalidoDevuelve401() throws Exception { mvc.perform(get("/api/v1/cuenta").header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized()); }
}
