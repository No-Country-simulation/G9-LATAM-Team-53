package com.alura.finance_ai.auth.service;

import com.alura.finance_ai.auth.dto.AuthResponse;
import com.alura.finance_ai.auth.dto.LoginRequest;
import com.alura.finance_ai.auth.dto.RegisterRequest;
import com.alura.finance_ai.auth.model.TokenInvalido;
import com.alura.finance_ai.auth.model.User;
import com.alura.finance_ai.auth.repository.TokenInvalidoRepository;
import com.alura.finance_ai.auth.repository.UserRepository;
import com.alura.finance_ai.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenInvalidoRepository tokenInvalidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       TokenInvalidoRepository tokenInvalidoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tokenInvalidoRepository = tokenInvalidoRepository;
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
        user.setPasswordHash(passwordEncoder.encode(request.contrasena()));

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getUserId(),
                savedUser.getNombre(),
                savedUser.getApellido(),
                savedUser.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Credenciales invalidas:: email no encontado"));

        if (!passwordEncoder.matches(request.contrasena(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales invalidas: contrasena incorrecta");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getNombre(),
                user.getApellido(),
                user.getEmail()
        );
    }

    public void logout(String token) {
        if (!tokenInvalidoRepository.existsByToken(token)) {
            tokenInvalidoRepository.save(TokenInvalido.of(token));
        }
    }
}
