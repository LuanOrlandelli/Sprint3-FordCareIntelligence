package fordcare_api.security;

import fordcare_api.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomAuthenticationEntryPoint.class
            );

    private final AuditService auditService;

    public CustomAuthenticationEntryPoint(
            AuditService auditService
    ) {
        this.auditService = auditService;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String endpoint =
                request.getRequestURI();

        String ipAddress =
                request.getRemoteAddr();

        logger.warn(
                "UNAUTHORIZED_ACCESS endpoint={} ip={}",
                endpoint,
                ipAddress
        );

        auditService.register(
                "UNAUTHORIZED_ACCESS",
                "ANONYMOUS",
                endpoint,
                ipAddress
        );

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write("""
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "Autenticação necessária para acessar este recurso"
                }
                """);
    }
}