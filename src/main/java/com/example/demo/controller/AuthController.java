package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint de inicio de sesión
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Por favor ingresa correo y contraseña.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Si el usuario tiene contraseña guardada, validarla (si no tiene, se actualiza)
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    if (!usuario.getPassword().equals(password)) {
                        response.put("exito", false);
                        response.put("mensaje", "Contraseña incorrecta. Inténtalo de nuevo.");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                    }
                } else {
                    // Si no tenía contraseña, asignar la proporcionada
                    usuario.setPassword(password);
                    usuarioRepository.save(usuario);
                }

                String token = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                response.put("exito", true);
                response.put("mensaje", "¡Bienvenido de vuelta, " + usuario.getNombre() + "!");
                response.put("token", token);
                response.put("hora_inicio", timestamp);

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", usuario.getId());
                userData.put("nombre", usuario.getNombre());
                userData.put("email", usuario.getEmail());
                userData.put("rol", usuario.getRol() != null ? usuario.getRol() : "CLIENTE");
                userData.put("telefono", usuario.getTelefono());
                userData.put("direccion", usuario.getDireccion());
                response.put("usuario", userData);

                return ResponseEntity.ok(response);
            } else {
                response.put("exito", false);
                response.put("mensaje", "No existe una cuenta con el correo: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al procesar la solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint de registro de usuario
     * POST /api/auth/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registro(@RequestBody Map<String, String> userData) {
        Map<String, Object> response = new HashMap<>();
        String nombre = userData.get("nombre");
        String email = userData.get("email");
        String password = userData.get("password");

        if (nombre == null || nombre.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Todos los campos son obligatorios.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Optional<Usuario> existente = usuarioRepository.findByEmail(email.trim().toLowerCase());
            if (existente.isPresent()) {
                response.put("exito", false);
                response.put("mensaje", "Ya existe una cuenta con ese correo electrónico.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            Usuario nuevo = new Usuario(nombre.trim(), email.trim().toLowerCase(), password);
            Usuario guardado = usuarioRepository.save(nuevo);

            String token = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            response.put("exito", true);
            response.put("mensaje", "¡Cuenta creada exitosamente! Bienvenido, " + guardado.getNombre());
            response.put("token", token);
            response.put("hora_inicio", timestamp);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", guardado.getId());
            userMap.put("nombre", guardado.getNombre());
            userMap.put("email", guardado.getEmail());
            userMap.put("rol", "Usuario");
            response.put("usuario", userMap);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
