package cl.duoc.tecnoshop.controller;
import cl.duoc.tecnoshop.dto.AuthDtos.*;
import cl.duoc.tecnoshop.model.*;
import cl.duoc.tecnoshop.repository.UsuarioRepository;
import cl.duoc.tecnoshop.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthControllerTest {
    UsuarioRepository usuarios; AuthController controller; BCryptPasswordEncoder encoder;
    @BeforeEach void setup() {
        usuarios = mock(UsuarioRepository.class); encoder = new BCryptPasswordEncoder();
        controller = new AuthController(usuarios, encoder, new JwtService("test-only-secret-123456789012345678901234567890", 60000));
    }
    @Test void registroGuardaHashYRolCliente() {
        var result = controller.registro(new Registro("Ana", "ana@example.test", "Prueba123"));
        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertTrue(encoder.matches("Prueba123", captor.getValue().getPasswordHash()));
        assertNotEquals("Prueba123", captor.getValue().getPasswordHash());
        assertEquals(Rol.CLIENTE, captor.getValue().getRol());
        assertEquals(201, result.getStatusCode().value());
        assertEquals("CLIENTE", result.getBody().rol());
    }
    @Test void correoDuplicadoNoSeGuarda() {
        when(usuarios.existsByEmail("ana@example.test")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class, () -> controller.registro(new Registro("Ana", "ana@example.test", "Prueba123")));
        assertEquals(409, error.getStatusCode().value()); verify(usuarios, never()).save(any());
    }
    @Test void passwordIncorrectoEsRechazado() {
        Usuario u = new Usuario(); u.setPasswordHash(encoder.encode("correcta"));
        when(usuarios.findByEmail("ana@example.test")).thenReturn(Optional.of(u));
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> controller.login(new Login("ana@example.test", "incorrecta"))).getStatusCode().value());
    }
    @Test void correoDesconocidoEsRechazado() {
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> controller.login(new Login("nadie@example.test", "incorrecta"))).getStatusCode().value());
    }
    @Test void loginDevuelveTokenYRolPersistido() {
        Usuario u = new Usuario(); u.setEmail("ana@example.test"); u.setNombre("Ana"); u.setRol(Rol.VENDEDOR); u.setPasswordHash(encoder.encode("correcta"));
        when(usuarios.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        var token = controller.login(new Login(u.getEmail(), "correcta"));
        assertEquals("VENDEDOR", token.rol()); assertNotNull(token.token());
    }
}
