package fordcare_api.security;

import fordcare_api.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CustomAccessDeniedHandler.class
            );

    private final AuditService auditService;

    public CustomAccessDeniedHandler(
            AuditService auditService
    ) {
        this.auditService = auditService;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        String endpoint =
                request.getRequestURI();

        String ipAddress =
                request.getRemoteAddr();

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication != null
                        ? authentication.getName()
                        : "UNKNOWN";

        logger.warn(
                "ACCESS_DENIED user={} endpoint={} ip={}",
                username,
                endpoint,
                ipAddress
        );

        auditService.register(
                "ACCESS_DENIED",
                username,
                endpoint,
                ipAddress
        );

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write("""
                {
                    "status": 403,
                    "error": "Forbidden",
                    "message": "Você não possui permissão para acessar este recurso"
                }
                """);
    }
}