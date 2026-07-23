package com.alura.finance_ai.auth.service;

import com.alura.finance_ai.auth.dto.AuthResponse;
import com.alura.finance_ai.auth.dto.RegisterRequest;
import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("El email ya se encuentra registrado");
        }

        User user = new User();
        user.setNombre(request.nombre());
        user.setApellido(request.apellido());
        user.setEmail(request.email());
        user.setContrasena(passwordEncoder.encode(request.contrasena()));

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getNombre(),
                savedUser.getApellido(),
                savedUser.getEmail()
        );
    }
}