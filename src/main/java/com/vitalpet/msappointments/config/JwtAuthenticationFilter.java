package com.vitalpet.msappointments.config;

import com.vitalpet.msappointments.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = jwtService.extractClaims(token);

            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            Object userId = claims.get("userId");

            if (email == null || email.isBlank()
                    || role == null
                    || !(userId instanceof Number)
                    || (!role.equals("CLIENT")
                        && !role.equals("ADMIN"))) {

                throw new IllegalArgumentException("JWT incompleto");
            }

            Long authenticatedUserId =
                    ((Number) userId).longValue();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                        authenticatedUserId,
                        null,
                        List.of(
                            new SimpleGrantedAuthority(
                                "ROLE_" + role
                            )
                        )
                    );

            // Conservamos el correo para comprobar
            // los datos enviados al crear la cita.
            authentication.setDetails(email);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
