package com.example.product.config;

import com.example.product.exception.ErrorResponse;
import com.example.product.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final Pattern BRACKET_PATTERN = Pattern.compile("[\\[\\]]");
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        if (!isValidBearerToken(authHeader)) {
            handleMissingToken(response);
            return;
        }
        
        final String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            processValidToken(token);
        } catch (Exception e) {
            handleAuthenticationError(response, e);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isValidBearerToken(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }
    
    private void processValidToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new JwtException("Invalid or expired token");
        }
        
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        
        List<GrantedAuthority> authorities = parseRoles(role);
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private List<GrantedAuthority> parseRoles(String role) {
        String cleanRole = BRACKET_PATTERN.matcher(role).replaceAll("");
        
        if (cleanRole.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(cleanRole.split(","))
                .map(String::trim)
                .filter(roleStr -> !roleStr.isEmpty())
                .map(roleStr -> new SimpleGrantedAuthority(ROLE_PREFIX + roleStr))
                .collect(Collectors.toList());
    }
    
    private void handleMissingToken(HttpServletResponse response) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                "Missing or invalid Authorization header",
                HttpServletResponse.SC_UNAUTHORIZED,
                LocalDateTime.now(),
                null
        );
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid token: " + e.getMessage(),
                HttpServletResponse.SC_UNAUTHORIZED,
                LocalDateTime.now(),
                null
        );
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
