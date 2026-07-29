package com.main.frotaBackEnd.config;

import com.main.frotaBackEnd.model.Usuario;
import com.main.frotaBackEnd.repository.UserRepository;
import com.main.frotaBackEnd.model.UsuarioDTO;
import com.main.frotaBackEnd.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${api.security.session.inactivity-timeout-ms:1800000}")
    private long inactivityTimeoutMs;

    private static final ConcurrentHashMap<Long, Instant> lastActivity = new ConcurrentHashMap<>();

    public JwtAuthenticationFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (tokenService.validarToken(token)) {
                    UsuarioDTO usuario = tokenService.extrairClaim(token);

                    Optional<Usuario> userOp = userRepository.findById(usuario.getId_usuario());
                    if (userOp.isPresent() && userOp.get().isAtivo()) {
                        Long userId = usuario.getId_usuario();
                        Instant now = Instant.now();
                        Instant lastSeen = lastActivity.get(userId);

                        if (lastSeen != null && now.toEpochMilli() - lastSeen.toEpochMilli() > inactivityTimeoutMs) {
                            lastActivity.remove(userId);
                        } else {
                            lastActivity.put(userId, now);
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().toUpperCase());
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    usuario, null, Collections.singletonList(authority));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            } catch (Exception e) {

            }
        }

        filterChain.doFilter(request, response);
    }
}
