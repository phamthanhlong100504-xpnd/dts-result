package com.dts.result.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer:dts-identity-service}")
    private String jwtIssuer;

    private static List<?> listClaim(Object raw) {
        return raw instanceof List<?> values ? values : List.of();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireIssuer(jwtIssuer)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Identity service issue `sub` = userId (UUID). Đọc từ `sub` để đồng bộ với
                // practice/progress/examination/content-builder (không dùng claim `userId`).
                String userIdStr = claims.getSubject();
                if (userIdStr != null) {
                    UUID userId = UUID.fromString(userIdStr);
                    // Map only signed Identity-issued claims. A role without its specific grant
                    // never authorizes Admin reporting. Do not infer permissions from ROLE_ADMIN.
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    for (Object role : listClaim(claims.get("roles"))) {
                        if (role instanceof String name && name.matches("ROLE_[A-Z0-9_]{1,40}"))
                            authorities.add(new SimpleGrantedAuthority(name));
                    }
                    for (Object permission : listClaim(claims.get("permissions"))) {
                        if (permission instanceof String name && name.matches("[a-z][a-z0-9-]{0,63}:[a-z][a-z0-9-]{0,40}"))
                            authorities.add(new SimpleGrantedAuthority("PERM_" + name));
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token is invalid, ignore and let SecurityConfig handle it (will return 401/403)
            }
        }

        filterChain.doFilter(request, response);
    }
}
