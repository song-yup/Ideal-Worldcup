package com.example.springJWT.jwt;

import com.example.springJWT.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter { // 로그인 시 username 과 password 를 받아서 꺼내는 필터

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 형식의 요청 본문을 파싱
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 60*60*1000L);
        response.addHeader("Authorization", "Bearer " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}