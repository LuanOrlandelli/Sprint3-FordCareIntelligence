package fordcare_api.security;

import fordcare_api.controller.LeadController;
import fordcare_api.entity.User;
import fordcare_api.repository.UserRepository;
import fordcare_api.service.AuditService;
import fordcare_api.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LeadController.class)
@Import({
        SecurityConfig.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RateLimitFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LeadService leadService;

    @MockitoBean
    private AuditService auditService;

    @Test
    void acessoSemTokenDeveRetornar401()
            throws Exception {

        mockMvc.perform(
                        get("/leads")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void adminComTokenValidoDeveAcessarLeads()
            throws Exception {

        User admin = criarUsuario(
                1L,
                "Administrador",
                "admin@fordcare.com",
                1L,
                10L
        );

        String token =
                jwtService.generateToken(
                        admin,
                        "ADMIN"
                );

        when(
                userRepository.findByEmail(
                        "admin@fordcare.com"
                )
        ).thenReturn(
                Optional.of(admin)
        );

        when(
                leadService.listLeads()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/leads")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void dealerManagerComTokenValidoDeveAcessarLeads()
            throws Exception {

        User manager = criarUsuario(
                2L,
                "Gerente",
                "manager@fordcare.com",
                3L,
                10L
        );

        String token =
                jwtService.generateToken(
                        manager,
                        "DEALER_MANAGER"
                );

        when(
                userRepository.findByEmail(
                        "manager@fordcare.com"
                )
        ).thenReturn(
                Optional.of(manager)
        );

        when(
                leadService.listLeads()
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/leads")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void analystNaoDeveAcessarLeads()
            throws Exception {

        User analyst = criarUsuario(
                3L,
                "Analista",
                "analyst@fordcare.com",
                2L,
                10L
        );

        String token =
                jwtService.generateToken(
                        analyst,
                        "ANALYST"
                );

        when(
                userRepository.findByEmail(
                        "analyst@fordcare.com"
                )
        ).thenReturn(
                Optional.of(analyst)
        );

        mockMvc.perform(
                        get("/leads")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    private User criarUsuario(
            Long id,
            String nome,
            String email,
            Long roleId,
            Long dealershipId
    ) {

        User user =
                new User();

        user.setId(id);
        user.setName(nome);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setDealershipId(dealershipId);

        return user;
    }
}