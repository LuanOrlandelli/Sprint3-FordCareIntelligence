package fordcare_api.service;

import fordcare_api.dto.request.LoginRequest;
import fordcare_api.dto.response.LoginResponse;
import fordcare_api.entity.Role;
import fordcare_api.entity.User;
import fordcare_api.exception.InvalidCredentialsException;
import fordcare_api.exception.ResourceNotFoundException;
import fordcare_api.repository.RoleRepository;
import fordcare_api.repository.UserRepository;
import fordcare_api.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress =
                httpRequest.getRemoteAddr();

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElse(null);

        /*
         * A mesma mensagem é utilizada tanto para usuário
         * inexistente quanto para senha incorreta.
         *
         * Isso reduz enumeração de usuários.
         */
        if (user == null) {

            logger.warn(
                    "LOGIN_FAILED email={} ip={}",
                    request.getEmail(),
                    ipAddress
            );

            auditService.register(
                    "LOGIN_FAILED",
                    request.getEmail(),
                    "/auth/login",
                    ipAddress
            );

            throw new InvalidCredentialsException(
                    "Usuário ou senha inválidos"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            logger.warn(
                    "LOGIN_FAILED email={} ip={}",
                    request.getEmail(),
                    ipAddress
            );

            auditService.register(
                    "LOGIN_FAILED",
                    request.getEmail(),
                    "/auth/login",
                    ipAddress
            );

            throw new InvalidCredentialsException(
                    "Usuário ou senha inválidos"
            );
        }

        Role role =
                roleRepository
                        .findById(user.getRoleId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Perfil de acesso não encontrado"
                                        )
                        );

        String token =
                jwtService.generateToken(
                        user,
                        role.getName()
                );

        logger.info(
                "LOGIN_SUCCESS user={} role={} ip={}",
                user.getEmail(),
                role.getName(),
                ipAddress
        );

        auditService.register(
                "LOGIN_SUCCESS",
                user.getEmail(),
                "/auth/login",
                ipAddress
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoleId(),
                role.getName(),
                user.getDealershipId(),
                token,
                "Login realizado com sucesso"
        );
    }
}