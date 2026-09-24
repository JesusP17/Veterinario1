package com.example.demo.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api")
public class TestConexionController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint para verificar el estado de la conexión con MySQL en XAMPP.
     * URL: http://localhost:8080/api/conexion
     */
    @GetMapping("/conexion")
    public ResponseEntity<Map<String, Object>> verificarConexion() {
        Map<String, Object> respuesta = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            respuesta.put("estado", "EXITOSO");
            respuesta.put("mensaje", "Conexión establecida correctamente con MySQL (XAMPP)");
            respuesta.put("base_de_datos", connection.getCatalog());
            respuesta.put("url_datasource", connection.getMetaData().getURL());
            respuesta.put("usuario_db", connection.getMetaData().getUserName());
            respuesta.put("total_usuarios_registrados", usuarioRepository.count());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("estado", "ERROR");
            respuesta.put("mensaje", "No se pudo conectar a MySQL: " + e.getMessage());
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }

    /**
     * Obtener todos los usuarios de la base de datos.
     * URL: http://localhost:8080/api/usuarios
     */
    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Crear un nuevo usuario en la base de datos.
     * URL: http://localhost:8080/api/usuarios
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(nuevoUsuario);
    }
}
