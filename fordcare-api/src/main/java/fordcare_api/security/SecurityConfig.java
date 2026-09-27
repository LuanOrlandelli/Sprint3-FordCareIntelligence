package fordcare_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(cors -> {})

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // MONITORAMENTO INTERNO
                        // =====================================

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/prometheus"
                        ).permitAll()

                        // =====================================
                        // AUTENTICAÇÃO
                        // =====================================

                        .requestMatchers(
                                "/auth/login"
                        ).permitAll()

                        // =====================================
                        // DOCUMENTAÇÃO
                        // =====================================

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =====================================
                        // RBAC - INSIGHTS
                        // =====================================

                        .requestMatchers(
                                "/insights/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =====================================
                        // RBAC - CUSTOMERS
                        // =====================================

                        .requestMatchers(
                                "/customers/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST",
                                "DEALER_MANAGER"
                        )

                        // =====================================
                        // RBAC - RECOMMENDATIONS
                        // =====================================

                        .requestMatchers(
                                "/recommendations/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST",
                                "DEALER_MANAGER"
                        )

                        // =====================================
                        // RBAC - LEADS
                        // =====================================

                        .requestMatchers(
                                "/leads/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "DEALER_MANAGER"
                        )

                        // =====================================
                        // RBAC - PREDICTIONS
                        // =====================================

                        .requestMatchers(
                                "/predictions/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "ANALYST"
                        )

                        // =====================================
                        // DEMAIS ENDPOINTS
                        // =====================================

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        rateLimitFilter,
                        SecurityContextHolderFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}