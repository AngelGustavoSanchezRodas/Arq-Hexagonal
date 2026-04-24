package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.LoginRequest;
import com.navaja.navajagtbackend.dto.LoginResponse;
import com.navaja.navajagtbackend.dto.RegistroRequest;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.models.PlanUsuario;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import com.navaja.navajagtbackend.security.ServicioJwt;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ServicioJwt servicioJwt;

    public AuthController(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, ServicioJwt servicioJwt) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicioJwt = servicioJwt;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegistroRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setPlan(PlanUsuario.FREE);
        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado correctamente"));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = servicioJwt.generarToken(usuario.getEmail(), Map.of("uid", usuario.getId()));
        return ResponseEntity.ok(new LoginResponse(token));
    }
}

