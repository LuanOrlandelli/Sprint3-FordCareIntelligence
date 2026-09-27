package fordcare_api.security;

import fordcare_api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final long EXPIRATION = 1000 * 60 * 60 * 2;

    private final String secret;

    public JwtService(
            @Value("${jwt.secret}") String secret
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET deve possuir pelo menos 32 caracteres"
            );
        }

        this.secret = secret;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(
            User user,
            String roleName
    ) {

        Date now = new Date();

        Date expirationDate =
                new Date(
                        now.getTime() + EXPIRATION
                );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", roleName)
                .claim(
                        "dealershipId",
                        user.getDealershipId()
                )
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {

        return extractClaims(token)
                .getSubject();
    }

    public boolean isTokenValid(String token) {

        try {

            Date expiration =
                    extractClaims(token)
                            .getExpiration();

            return expiration.after(
                    new Date()
            );

        } catch (Exception ex) {

            return false;
        }
    }
}