package com.rally.service;

import com.rally.dto.LoginRequest;
import com.rally.dto.RegisterRequest;
import com.rally.dto.UserResponse;
import com.rally.model.User;
import com.rally.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_SESSION_KEY = "AUTHENTICATED_USER_ID";

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public UserResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Store user ID in session
        session.setAttribute(USER_SESSION_KEY, user.getId());
        
        // Create Spring Security authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getId(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        // Set authentication in SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // Store SecurityContext in session
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        
        return UserResponse.from(user);
    }

    public void logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    public UserResponse getCurrentUser(HttpSession session) {
        String userId = getCurrentUserId(session);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

    public String getCurrentUserId(HttpSession session) {
        // First try to get from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return (String) authentication.getPrincipal();
        }
        
        // Fallback to session attribute
        String userId = (String) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            throw new RuntimeException("Not authenticated");
        }
        return userId;
    }
}
