package fordcare_api.security;

import fordcare_api.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    private static final String TEST_SECRET =
            "test-only-secret-key-for-jwt-tests-123456789";

    @BeforeEach
    void setUp() {

        jwtService =
                new JwtService(TEST_SECRET);

        user = new User();

        user.setId(1L);
        user.setName("Administrador");
        user.setEmail("admin@fordcare.com");
        user.setRoleId(1L);
        user.setDealershipId(10L);
    }

    @Test
    void deveGerarTokenJWT() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveValidarTokenJWT() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        boolean valido =
                jwtService.isTokenValid(token);

        assertTrue(valido);
    }

    @Test
    void deveExtrairEmailDoToken() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "admin@fordcare.com",
                email
        );
    }

    @Test
    void deveConterClaimsCorretas() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        Claims claims =
                jwtService.extractClaims(token);

        assertEquals(
                "admin@fordcare.com",
                claims.getSubject()
        );

        assertEquals(
                "ADMIN",
                claims.get("role")
        );

        assertEquals(
                1,
                claims.get(
                        "userId",
                        Integer.class
                )
        );

        assertEquals(
                10,
                claims.get(
                        "dealershipId",
                        Integer.class
                )
        );
    }

    @Test
    void devePossuirDataDeExpiracao() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        Claims claims =
                jwtService.extractClaims(token);

        Date expiration =
                claims.getExpiration();

        assertNotNull(expiration);

        assertTrue(
                expiration.after(
                        new Date()
                )
        );
    }

    @Test
    void devePossuirDataDeEmissao() {

        String token =
                jwtService.generateToken(
                        user,
                        "ADMIN"
                );

        Claims claims =
                jwtService.extractClaims(token);

        assertNotNull(
                claims.getIssuedAt()
        );
    }

    @Test
    void deveRejeitarChaveJWTInsegura() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new JwtService("chave-curta")
                );

        assertEquals(
                "JWT_SECRET deve possuir pelo menos 32 caracteres",
                exception.getMessage()
        );
    }
}