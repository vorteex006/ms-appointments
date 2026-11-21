package com.vitalpet.msappointments.config;

import com.vitalpet.msappointments.service.JwtService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .exceptionHandling(exceptions -> exceptions

                .authenticationEntryPoint((request, response, ex) ->
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                    )
                )

                .accessDeniedHandler((request, response, ex) ->
                    response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN
                    )
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Preflight del navegador.
                .requestMatchers(
                    HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Documentacion Swagger.
                .requestMatchers(
                    "/api/appointments/v3/api-docs",
                    "/api/appointments/v3/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Listado general: ADMIN.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments"
                ).hasRole("ADMIN")

                // Crear cita: CLIENT o ADMIN.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/appointments"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Cambiar estado:
                // ADMIN o CLIENT; el controlador limita
                // al CLIENT a cancelar sus propias citas.
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/appointments/*/status"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Eliminar definitivamente: ADMIN.
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/appointments/*"
                ).hasRole("ADMIN")

                // Verificar existencia: ADMIN.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments/*/exists"
                ).hasRole("ADMIN")

                // Listar por propietario.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments/*/owner"
                ).hasAnyRole("CLIENT", "ADMIN")

                // Consultar una cita individual.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/appointments/*"
                ).hasAnyRole("CLIENT", "ADMIN")

                .anyRequest().denyAll()
            )

            .addFilterBefore(
                new JwtAuthenticationFilter(jwtService),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
