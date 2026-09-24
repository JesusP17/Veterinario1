package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Cita;
import com.example.demo.model.Mascota;
import com.example.demo.model.Servicio;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.MascotaRepository;
import com.example.demo.model.Usuario;
import com.example.demo.repository.ServicioRepository;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener todas las citas
     * GET /api/citas
     */
    @GetMapping
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    /**
     * Obtener una cita específica por ID
     * GET /api/citas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCitaPorId(@PathVariable Long id) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isPresent()) {
            return ResponseEntity.ok(citaOpt.get());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("exito", false);
        response.put("mensaje", "Cita no encontrada.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Obtener citas por estado (PENDIENTE, CONFIRMADA, CANCELADA, etc.)
     * GET /api/citas/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public List<Cita> listarCitasPorEstado(@PathVariable String estado) {
        return citaRepository.findByEstado(estado.toUpperCase());
    }

    /**
     * Obtener citas de una mascota específica
     * GET /api/citas/mascota/{mascotaId}
     */
    @GetMapping("/mascota/{mascotaId}")
    public List<Cita> listarCitasPorMascota(@PathVariable Long mascotaId) {
        return citaRepository.findByMascotaId(mascotaId);
    }

    /**
     * Obtener citas de un cliente / propietario específico
     * GET /api/citas/cliente/{propietarioId}
     */
    @GetMapping("/cliente/{propietarioId}")
    public ResponseEntity<List<Cita>> listarCitasPorCliente(@PathVariable Long propietarioId) {
        List<Cita> citas = citaRepository.findByPropietarioId(propietarioId);
        return ResponseEntity.ok(citas);
    }

    /**
     * Obtener citas para un veterinario (asignadas a él o citas pendientes sin asignar)
     * GET /api/citas/veterinario/{vetId}
     */
    @GetMapping("/veterinario/{vetId}")
    public ResponseEntity<List<Cita>> listarCitasVeterinario(@PathVariable Long vetId) {
        List<Cita> citas = citaRepository.findCitasParaVeterinario(vetId);
        return ResponseEntity.ok(citas);
    }

    /**
     * Asignar un veterinario a una cita médica y confirmarla
     * PUT /api/citas/{id}/asignar-veterinario/{vetId}
     */
    @PutMapping("/{id}/asignar-veterinario/{vetId}")
    public ResponseEntity<?> asignarVeterinario(@PathVariable Long id, @PathVariable Long vetId) {
        Map<String, Object> response = new HashMap<>();
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<Usuario> vetOpt = usuarioRepository.findById(vetId);
        if (vetOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Médico Veterinario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Cita cita = citaOpt.get();
        cita.setVeterinario(vetOpt.get());
        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);

        response.put("exito", true);
        response.put("mensaje", "Cita asignada y confirmada con el " + vetOpt.get().getNombre() + ".");
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Marcar una cita como ATENDIDA por el veterinario
     * PUT /api/citas/{id}/atender
     */
    @PutMapping("/{id}/atender")
    public ResponseEntity<?> atenderCita(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Cita cita = citaOpt.get();
        cita.setEstado("ATENDIDA");
        citaRepository.save(cita);

        response.put("exito", true);
        response.put("mensaje", "Cita marcada como ATENDIDA.");
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Registrar una nueva cita (Con validación de horario RN-04)
     * POST /api/citas
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> registrarCita(@RequestBody Cita cita) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validar que la mascota esté especificada y exista en base de datos
            if (cita.getMascota() == null || cita.getMascota().getId() == null) {
                response.put("exito", false);
                response.put("mensaje", "Debes especificar la mascota para agendar la cita.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<Mascota> mascotaOpt = mascotaRepository.findById(cita.getMascota().getId());
            if (mascotaOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "La mascota seleccionada no existe en la base de datos.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            cita.setMascota(mascotaOpt.get());

            // 2. Validar fecha y hora
            if (cita.getFecha() == null || cita.getHora() == null) {
                response.put("exito", false);
                response.put("mensaje", "La fecha y la hora son obligatorias.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (cita.getFecha().isBefore(LocalDate.now())) {
                response.put("exito", false);
                response.put("mensaje", "No es posible agendar citas en fechas pasadas.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 3. Vincular con entidad Servicio si se suministró o resolver por nombre
            if (cita.getServicioEntidad() != null && cita.getServicioEntidad().getId() != null) {
                Optional<Servicio> servOpt = servicioRepository.findById(cita.getServicioEntidad().getId());
                servOpt.ifPresent(cita::setServicioEntidad);
            } else if (cita.getServicio() != null) {
                Optional<Servicio> servOpt = servicioRepository.findByNombre(cita.getServicio());
                servOpt.ifPresent(cita::setServicioEntidad);
            }

            if (cita.getServicio() == null || cita.getServicio().trim().isEmpty()) {
                cita.setServicio("Consulta General");
            }

            // 4. Validar Regla de Negocio RN-04: No reservar horario ocupado (excluyendo canceladas)
            Optional<Cita> citaExistente = citaRepository.buscarConflictoHorario(cita.getFecha(), cita.getHora());
            if (citaExistente.isPresent()) {
                response.put("exito", false);
                response.put("mensaje", "El horario seleccionado ya está ocupado por otra cita. Por favor elige otro horario.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // 5. Asignar estado PENDIENTE por defecto
            if (cita.getEstado() == null || cita.getEstado().trim().isEmpty()) {
                cita.setEstado("PENDIENTE");
            }

            // 6. Guardar la cita
            Cita citaGuardada = citaRepository.save(cita);

            response.put("exito", true);
            response.put("mensaje", "Cita solicitada exitosamente. Estado: PENDIENTE.");
            response.put("cita", citaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al registrar la cita: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cancelar cita (Permitido para cliente, veterinario o administrador)
     * PUT /api/citas/{id}/cancelar
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Cita cita = citaOpt.get();
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())) {
            response.put("exito", false);
            response.put("mensaje", "La cita ya se encuentra cancelada.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        cita.setEstado("CANCELADA");
        citaRepository.save(cita);

        response.put("exito", true);
        response.put("mensaje", "La cita ha sido cancelada y el horario ha quedado liberado.");
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirmar cita (Para rol Veterinario o Administrador)
     * PUT /api/citas/{id}/confirmar
     */
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarCita(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Cita cita = citaOpt.get();
        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);

        response.put("exito", true);
        response.put("mensaje", "La cita ha sido confirmada exitosamente.");
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Reprogramar fecha y hora de una cita
     * PUT /api/citas/{id}/reprogramar
     */
    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<?> reprogramarCita(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String strFecha = payload.get("fecha");
        String strHora = payload.get("hora");

        if (strFecha == null || strHora == null) {
            response.put("exito", false);
            response.put("mensaje", "Debe proporcionar la nueva fecha y hora.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            LocalDate nuevaFecha = LocalDate.parse(strFecha);
            LocalTime nuevaHora = LocalTime.parse(strHora);

            if (nuevaFecha.isBefore(LocalDate.now())) {
                response.put("exito", false);
                response.put("mensaje", "La nueva fecha no puede estar en el pasado.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Validar que el nuevo horario no esté ocupado por otra cita activa
            Optional<Cita> colision = citaRepository.buscarConflictoHorario(nuevaFecha, nuevaHora);
            if (colision.isPresent() && !colision.get().getId().equals(id)) {
                response.put("exito", false);
                response.put("mensaje", "El nuevo horario seleccionado ya está ocupado.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            Cita cita = citaOpt.get();
            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
            cita.setEstado("REPROGRAMADA");
            citaRepository.save(cita);

            response.put("exito", true);
            response.put("mensaje", "Cita reprogramada exitosamente para el " + nuevaFecha + " a las " + nuevaHora + ".");
            response.put("cita", cita);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            response.put("exito", false);
            response.put("mensaje", "Formato de fecha u hora no válido (Esperado: YYYY-MM-DD y HH:mm).");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Actualizar estado genérico de una cita
     * PUT /api/citas/{id}/estado
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoCita(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String nuevoEstado = request.get("estado");

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "El estado es requerido.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Cita cita = citaOpt.get();
        cita.setEstado(nuevoEstado.trim().toUpperCase());
        citaRepository.save(cita);

        response.put("exito", true);
        response.put("mensaje", "Estado de la cita actualizado a: " + nuevoEstado.trim().toUpperCase());
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar físicamente una cita (Uso administrativo)
     * DELETE /api/citas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        if (!citaRepository.existsById(id)) {
            response.put("exito", false);
            response.put("mensaje", "Cita no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        citaRepository.deleteById(id);
        response.put("exito", true);
        response.put("mensaje", "Cita eliminada exitosamente.");
        return ResponseEntity.ok(response);
    }
}