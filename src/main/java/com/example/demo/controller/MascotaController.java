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

import com.example.demo.model.Mascota;
import com.example.demo.model.Usuario;
import com.example.demo.repository.MascotaRepository;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener todas las mascotas
     * GET /api/mascotas
     */
    @GetMapping
    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    /**
     * Obtener mascotas por propietario
     * GET /api/mascotas/propietario/{propietarioId}
     */
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> obtenerMascotasPorPropietario(@PathVariable Long propietarioId) {
        try {
            List<Mascota> mascotas = mascotaRepository.findByPropietarioId(propietarioId);
            if (mascotas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(mascotas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener una mascota por ID
     * GET /api/mascotas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerMascotaPorId(@PathVariable Long id) {
        Optional<Mascota> mascotaOpt = mascotaRepository.findById(id);
        if (mascotaOpt.isPresent()) {
            return ResponseEntity.ok(mascotaOpt.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Registrar una nueva mascota
     * POST /api/mascotas
     */
    @PostMapping
    public ResponseEntity<?> registrarMascota(@RequestBody Mascota mascota) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validar que el propietario existe
            if (mascota.getPropietario() == null || mascota.getPropietario().getId() == null) {
                response.put("exito", false);
                response.put("mensaje", "El propietario no fue especificado.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<Usuario> propietarioOpt = usuarioRepository.findById(mascota.getPropietario().getId());
            if (propietarioOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "El propietario no existe en la base de datos.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Verificar si ya existe una mascota con ese nombre para ese propietario (evitar duplicados)
            Optional<Mascota> mascotaExistenteOpt = mascotaRepository.findByNombreAndPropietarioId(
                mascota.getNombre(), mascota.getPropietario().getId()
            );

            if (mascotaExistenteOpt.isPresent()) {
                Mascota mascotaExistente = mascotaExistenteOpt.get();
                response.put("exito", true);
                response.put("mensaje", "Mascota existente recuperada.");
                response.put("mascota", mascotaExistente);
                return ResponseEntity.ok(response);
            }

            // Asignar el propietario completo
            mascota.setPropietario(propietarioOpt.get());

            // Guardar la mascota nueva
            Mascota mascotaGuardada = mascotaRepository.save(mascota);

            response.put("exito", true);
            response.put("mensaje", "Mascota registrada exitosamente.");
            response.put("mascota", mascotaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al registrar mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualizar una mascota
     * PUT /api/mascotas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable Long id, @RequestBody Mascota mascotaActualizada) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Mascota> mascotaOpt = mascotaRepository.findById(id);
            if (mascotaOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "Mascota no encontrada.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Mascota mascota = mascotaOpt.get();
            
            // Actualizar campos
            mascota.setNombre(mascotaActualizada.getNombre());
            mascota.setEspecie(mascotaActualizada.getEspecie());
            mascota.setRaza(mascotaActualizada.getRaza());
            mascota.setSexo(mascotaActualizada.getSexo());
            mascota.setFechaNacimiento(mascotaActualizada.getFechaNacimiento());
            mascota.setPeso(mascotaActualizada.getPeso());

            Mascota mascotaGuardada = mascotaRepository.save(mascota);

            response.put("exito", true);
            response.put("mensaje", "Mascota actualizada exitosamente.");
            response.put("mascota", mascotaGuardada);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al actualizar mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Eliminar una mascota
     * DELETE /api/mascotas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Mascota> mascotaOpt = mascotaRepository.findById(id);
            if (mascotaOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "Mascota no encontrada.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            mascotaRepository.deleteById(id);

            response.put("exito", true);
            response.put("mensaje", "Mascota eliminada exitosamente.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al eliminar mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}