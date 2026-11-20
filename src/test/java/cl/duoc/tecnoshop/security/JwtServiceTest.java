package cl.duoc.tecnoshop.security;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.JwtException;
import static org.junit.jupiter.api.Assertions.*;
class JwtServiceTest {
    private final String secret = "test-only-secret-123456789012345678901234567890";
    @Test void recuperaIdentidad() {
        var jwt = new JwtService(secret, 60000);
        assertEquals("ana@example.test", jwt.subject(jwt.create("ana@example.test")));
    }
    @Test void rechazaFirmaDeOtraClave() {
        var jwt = new JwtService(secret, 60000);
        var other = new JwtService("another-test-secret-123456789012345678901234567890", 60000);
        assertThrows(JwtException.class, () -> other.subject(jwt.create("ana@example.test")));
    }
    @Test void rechazaTokenVencido() {
        var jwt = new JwtService(secret, -10000);
        assertThrows(JwtException.class, () -> jwt.subject(jwt.create("ana@example.test")));
    }
}
