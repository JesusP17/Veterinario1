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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Servicio;
import com.example.demo.repository.ServicioRepository;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Obtener todos los servicios activos
     * GET /api/servicios
     */
    @GetMapping
    public List<Servicio> listarServiciosActivos() {
        return servicioRepository.findByActivoTrue();
    }

    /**
     * Obtener todos los servicios (incluyendo inactivos, para administración)
     * GET /api/servicios/todos
     */
    @GetMapping("/todos")
    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }

    /**
     * Obtener servicio por ID
     * GET /api/servicios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        Map<String, Object> err = new HashMap<>();
        err.put("exito", false);
        err.put("mensaje", "Servicio no encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    /**
     * Crear un nuevo servicio clínico
     * POST /api/servicios
     */
    @PostMapping
    public ResponseEntity<?> crearServicio(@RequestBody Servicio servicio) {
        Map<String, Object> response = new HashMap<>();
        if (servicio.getNombre() == null || servicio.getNombre().trim().isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "El nombre del servicio es obligatorio.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (servicio.getPrecio() == null || servicio.getPrecio() < 0) {
            response.put("exito", false);
            response.put("mensaje", "El precio debe ser un valor numérico válido mayor o igual a 0.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Servicio guardado = servicioRepository.save(servicio);
        response.put("exito", true);
        response.put("mensaje", "Servicio creado exitosamente.");
        response.put("servicio", guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar servicio existente
     * PUT /api/servicios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarServicio(@PathVariable Long id, @RequestBody Servicio servicioActualizado) {
        Map<String, Object> response = new HashMap<>();
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Servicio no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Servicio servicio = opt.get();
        servicio.setNombre(servicioActualizado.getNombre());
        servicio.setDescripcion(servicioActualizado.getDescripcion());
        servicio.setPrecio(servicioActualizado.getPrecio());
        servicio.setDuracionMinutos(servicioActualizado.getDuracionMinutos());
        servicio.setCategoria(servicioActualizado.getCategoria());
        servicio.setIcono(servicioActualizado.getIcono());
        if (servicioActualizado.getActivo() != null) {
            servicio.setActivo(servicioActualizado.getActivo());
        }

        Servicio guardado = servicioRepository.save(servicio);
        response.put("exito", true);
        response.put("mensaje", "Servicio actualizado exitosamente.");
        response.put("servicio", guardado);
        return ResponseEntity.ok(response);
    }

    /**
     * Desactivar lógicamente un servicio
     * DELETE /api/servicios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarServicio(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Servicio> opt = servicioRepository.findById(id);
        if (opt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Servicio no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Servicio servicio = opt.get();
        servicio.setActivo(false);
        servicioRepository.save(servicio);

        response.put("exito", true);
        response.put("mensaje", "Servicio desactivado correctamente.");
        return ResponseEntity.ok(response);
    }
}
