package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener todos los usuarios registrados
     * GET /api/usuarios
     */
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtener todos los usuarios con rol de VETERINARIO
     * GET /api/usuarios/veterinarios
     */
    @GetMapping("/veterinarios")
    public List<Usuario> listarVeterinarios() {
        return usuarioRepository.findByRolIgnoreCase("VETERINARIO");
    }

    /**
     * Obtener usuarios por rol (CLIENTE, VETERINARIO, ADMINISTRADOR)
     * GET /api/usuarios/rol/{rol}
     */
    @GetMapping("/rol/{rol}")
    public List<Usuario> listarPorRol(@PathVariable String rol) {
        return usuarioRepository.findByRolIgnoreCase(rol);
    }

    /**
     * Obtener un usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isPresent()) {
            return ResponseEntity.ok(uOpt.get());
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("exito", false);
        resp.put("mensaje", "Usuario no encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
    }

    /**
     * Actualizar el rol de un usuario registrado (Uso administrativo)
     * PUT /api/usuarios/{id}/rol
     */
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String nuevoRol = payload.get("rol");

        if (nuevoRol == null || nuevoRol.trim().isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "El rol es obligatorio.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setRol(nuevoRol.trim().toUpperCase());
        usuarioRepository.save(usuario);

        response.put("exito", true);
        response.put("mensaje", "Rol actualizado exitosamente a: " + usuario.getRol());
        response.put("usuario", usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar datos generales de un usuario
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datos) {
        Map<String, Object> response = new HashMap<>();
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Usuario usuario = usuarioOpt.get();
        if (datos.getNombre() != null) usuario.setNombre(datos.getNombre());
        if (datos.getEmail() != null) usuario.setEmail(datos.getEmail());
        if (datos.getRol() != null) usuario.setRol(datos.getRol());
        if (datos.getTelefono() != null) usuario.setTelefono(datos.getTelefono());
        if (datos.getDireccion() != null) usuario.setDireccion(datos.getDireccion());

        usuarioRepository.save(usuario);

        response.put("exito", true);
        response.put("mensaje", "Datos de usuario actualizados correctamente.");
        response.put("usuario", usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un usuario por ID
     * DELETE /api/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        if (!usuarioRepository.existsById(id)) {
            response.put("exito", false);
            response.put("mensaje", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        usuarioRepository.deleteById(id);
        response.put("exito", true);
        response.put("mensaje", "Usuario eliminado del sistema.");
        return ResponseEntity.ok(response);
    }
}
