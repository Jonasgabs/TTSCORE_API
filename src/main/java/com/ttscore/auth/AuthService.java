package com.ttscore.auth;

import com.ttscore.auth.dto.AuthResponse;
import com.ttscore.auth.dto.LoginRequest;
import com.ttscore.auth.dto.RegisterRequest;
import com.ttscore.exception.BusinessException;
import com.ttscore.security.JwtService;
import com.ttscore.user.User;
import com.ttscore.user.UserRepository;
import com.ttscore.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);
        return new AuthResponse(token, UserResponse.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.from(user));
    }
}
