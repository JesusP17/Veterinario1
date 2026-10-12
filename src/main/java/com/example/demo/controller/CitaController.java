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
import org.springframework.web.bind.annotation.RequestParam;
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
     * Asignar un veterinario a una cita médica y confirmarla considerando su horario y disponibilidad
     * PUT /api/citas/{id}/asignar-veterinario/{vetId}?forzar=false
     */
    @PutMapping("/{id}/asignar-veterinario/{vetId}")
    public ResponseEntity<?> asignarVeterinario(
            @PathVariable Long id, 
            @PathVariable Long vetId,
            @RequestParam(required = false, defaultValue = "false") boolean forzar) {
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
        Usuario vet = vetOpt.get();

        // 1. Validar conflicto de horario: ¿El veterinario ya tiene otra cita activa a esa hora?
        Optional<Cita> conflicto = citaRepository.buscarConflictoVeterinario(vetId, cita.getFecha(), cita.getHora());
        if (conflicto.isPresent() && !conflicto.get().getId().equals(cita.getId())) {
            if (!forzar) {
                response.put("exito", false);
                response.put("conflictoHorario", true);
                response.put("mensaje", "El Dr./Dra. " + vet.getNombre() + " ya tiene otra cita activa agendada para el " + 
                    cita.getFecha() + " a las " + cita.getHora() + ".");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        }

        // 2. Verificar si está dentro del horario laboral del veterinario
        boolean fueraDeTurno = false;
        if (vet.getHorarioInicio() != null && vet.getHorarioFin() != null && cita.getHora() != null) {
            if (cita.getHora().isBefore(vet.getHorarioInicio()) || cita.getHora().isAfter(vet.getHorarioFin())) {
                fueraDeTurno = true;
            }
        }

        cita.setVeterinario(vet);
        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);

        String mensaje = "Cita asignada y confirmada con el " + vet.getNombre() + ".";
        if (fueraDeTurno) {
            mensaje += " (Nota: la hora " + cita.getHora() + " está fuera de su horario regular " + 
                       vet.getHorarioInicio() + " - " + vet.getHorarioFin() + ").";
        }

        response.put("exito", true);
        response.put("mensaje", mensaje);
        response.put("fueraDeTurno", fueraDeTurno);
        response.put("cita", cita);
        return ResponseEntity.ok(response);
    }

    /**
     * Consultar disponibilidad y horarios de veterinarios para una fecha y hora específica
     * GET /api/citas/disponibilidad-veterinarios?fecha=YYYY-MM-DD&hora=HH:mm
     */
    @GetMapping("/disponibilidad-veterinarios")
    public ResponseEntity<?> consultarDisponibilidadVeterinarios(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String hora) {
        
        List<Usuario> veterinarios = usuarioRepository.findByRolIgnoreCase("VETERINARIO");
        LocalDate f = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();
        LocalTime h = (hora != null && !hora.trim().isEmpty()) ? LocalTime.parse(hora) : null;

        List<Map<String, Object>> resultado = new java.util.ArrayList<>();
        for (Usuario v : veterinarios) {
            Map<String, Object> info = new HashMap<>();
            info.put("veterinario", v);
            
            boolean enHorario = true;
            if (h != null && v.getHorarioInicio() != null && v.getHorarioFin() != null) {
                enHorario = !h.isBefore(v.getHorarioInicio()) && !h.isAfter(v.getHorarioFin());
            }
            info.put("enHorario", enHorario);

            boolean ocupado = false;
            if (h != null) {
                Optional<Cita> conflicto = citaRepository.buscarConflictoVeterinario(v.getId(), f, h);
                ocupado = conflicto.isPresent();
            }
            info.put("ocupado", ocupado);
            info.put("disponible", enHorario && !ocupado);

            // Citas activas asignadas a este veterinario en la fecha consultada
            List<Cita> citasDia = citaRepository.findByVeterinarioIdAndFecha(v.getId(), f);
            info.put("totalCitasDia", citasDia.size());
            info.put("citasDia", citasDia);

            resultado.add(info);
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Auto-asignar citas pendientes a veterinarios dependiendo de su horario laboral y disponibilidad
     * POST /api/citas/auto-asignar-por-horario
     */
    @PostMapping("/auto-asignar-por-horario")
    @Transactional
    public ResponseEntity<?> autoAsignarPorHorario() {
        Map<String, Object> response = new HashMap<>();
        List<Cita> citas = citaRepository.findAll();
        List<Usuario> veterinarios = usuarioRepository.findByRolIgnoreCase("VETERINARIO");

        int asignadas = 0;
        List<Map<String, Object>> detalles = new java.util.ArrayList<>();

        for (Cita c : citas) {
            if (c.getVeterinario() == null && !"CANCELADA".equalsIgnoreCase(c.getEstado())) {
                LocalDate fecha = c.getFecha();
                LocalTime hora = c.getHora();
                String servNombre = (c.getServicio() != null ? c.getServicio() : "").toLowerCase();

                Usuario mejorVet = null;

                // 1. Prioridad: Veterinario dentro de su horario, sin conflicto a esa hora y cuya especialidad coincida
                for (Usuario v : veterinarios) {
                    boolean enHorario = (v.getHorarioInicio() == null || v.getHorarioFin() == null) ||
                            (!hora.isBefore(v.getHorarioInicio()) && !hora.isAfter(v.getHorarioFin()));
                    boolean sinConflicto = citaRepository.buscarConflictoVeterinario(v.getId(), fecha, hora).isEmpty();

                    if (enHorario && sinConflicto) {
                        String esp = (v.getEspecialidad() != null ? v.getEspecialidad() : "").toLowerCase();
                        if (!esp.isEmpty() && (servNombre.contains(esp) || esp.contains(servNombre)
                                || (servNombre.contains("consulta") && esp.contains("general"))
                                || (servNombre.contains("cirug") && esp.contains("cirug"))
                                || (servNombre.contains("vacun") && esp.contains("vacun"))
                                || (servNombre.contains("diag") || servNombre.contains("imag") && esp.contains("diag")))) {
                            mejorVet = v;
                            break;
                        }
                    }
                }

                // 2. Si no hubo coincidencia de especialidad, cualquier veterinario en horario laboral y libre
                if (mejorVet == null) {
                    for (Usuario v : veterinarios) {
                        boolean enHorario = (v.getHorarioInicio() == null || v.getHorarioFin() == null) ||
                                (!hora.isBefore(v.getHorarioInicio()) && !hora.isAfter(v.getHorarioFin()));
                        boolean sinConflicto = citaRepository.buscarConflictoVeterinario(v.getId(), fecha, hora).isEmpty();
                        if (enHorario && sinConflicto) {
                            mejorVet = v;
                            break;
                        }
                    }
                }

                if (mejorVet != null) {
                    c.setVeterinario(mejorVet);
                    c.setEstado("CONFIRMADA");
                    citaRepository.save(c);
                    asignadas++;

                    Map<String, Object> d = new HashMap<>();
                    d.put("citaId", c.getId());
                    d.put("fecha", fecha.toString());
                    d.put("hora", hora.toString());
                    d.put("servicio", c.getServicio());
                    d.put("veterinario", mejorVet.getNombre());
                    d.put("turno", mejorVet.getTurno() != null ? mejorVet.getTurno() : "Horario General");
                    detalles.add(d);
                }
            }
        }

        response.put("exito", true);
        response.put("asignadas", asignadas);
        response.put("detalles", detalles);
        response.put("mensaje", "Se auto-asignaron " + asignadas + " citas según el horario laboral y turnos de los veterinarios.");
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

            // 5. Auto-asignar veterinario según especialidad / cargo del servicio
            if (cita.getVeterinario() == null) {
                List<Usuario> veterinarios = usuarioRepository.findByRolIgnoreCase("VETERINARIO");
                String servicioNombre = (cita.getServicio() != null ? cita.getServicio() : "").toLowerCase();
                
                Usuario vetEncontrado = null;
                for (Usuario v : veterinarios) {
                    if (v.getEspecialidad() != null && !v.getEspecialidad().trim().isEmpty()) {
                        String esp = v.getEspecialidad().toLowerCase();
                        if (servicioNombre.contains(esp) || esp.contains(servicioNombre) 
                            || (servicioNombre.contains("consulta") && esp.contains("general"))
                            || (servicioNombre.contains("cirug") && esp.contains("cirug"))
                            || (servicioNombre.contains("vacun") && esp.contains("vacun"))
                            || (servicioNombre.contains("imag") || servicioNombre.contains("diag") && esp.contains("diag"))) {
                            vetEncontrado = v;
                            break;
                        }
                    }
                }
                
                // Fallback: si no hay coincidencia exacta de cargo, asignar al primer veterinario registrado
                if (vetEncontrado == null && !veterinarios.isEmpty()) {
                    vetEncontrado = veterinarios.get(0);
                }
                
                if (vetEncontrado != null) {
                    cita.setVeterinario(vetEncontrado);
                }
            }

            // 6. Asignar estado CONFIRMADA si tiene veterinario, o PENDIENTE
            if (cita.getEstado() == null || cita.getEstado().trim().isEmpty()) {
                cita.setEstado(cita.getVeterinario() != null ? "CONFIRMADA" : "PENDIENTE");
            }

            // 7. Guardar la cita
            Cita citaGuardada = citaRepository.save(cita);

            response.put("exito", true);
            response.put("mensaje", cita.getVeterinario() != null ? 
                "Cita agendada y asignada automáticamente al " + cita.getVeterinario().getNombre() + " (" + (cita.getVeterinario().getEspecialidad() != null ? cita.getVeterinario().getEspecialidad() : "Veterinario") + ")." : 
                "Cita solicitada exitosamente. Estado: PENDIENTE.");
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