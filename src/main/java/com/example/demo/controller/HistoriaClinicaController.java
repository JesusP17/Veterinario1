package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Cita;
import com.example.demo.model.Consulta;
import com.example.demo.model.HistoriaClinica;
import com.example.demo.model.Mascota;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.ConsultaRepository;
import com.example.demo.repository.HistoriaClinicaRepository;
import com.example.demo.repository.MascotaRepository;
import com.example.demo.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/historias-clinicas")
@CrossOrigin(origins = "*")
public class HistoriaClinicaController {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Consultar la Historia Clínica completa de una Mascota
     * GET /api/historias-clinicas/mascota/{mascotaId}
     */
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<?> obtenerPorMascota(@PathVariable Long mascotaId) {
        Optional<Mascota> mascotaOpt = mascotaRepository.findById(mascotaId);
        if (mascotaOpt.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("exito", false);
            err.put("mensaje", "Mascota no encontrada.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        Mascota mascota = mascotaOpt.get();
        // Si no tiene historia clínica, inicializarla automáticamente con número único
        HistoriaClinica historia = historiaClinicaRepository.findByMascotaId(mascotaId)
            .orElseGet(() -> {
                String exp = "HC-" + String.format("%04d", mascota.getId()) + "-" + LocalDate.now().getYear();
                HistoriaClinica nueva = new HistoriaClinica(mascota, exp, "Sin antecedentes reportados", "Ninguna");
                return historiaClinicaRepository.save(nueva);
            });

        List<Consulta> consultas = consultaRepository.findByHistoriaClinicaIdOrderByFechaDescHoraDesc(historia.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("exito", true);
        response.put("historiaClinica", historia);
        response.put("mascota", mascota);
        response.put("consultas", consultas);

        return ResponseEntity.ok(response);
    }

    /**
     * Registrar una nueva atención médica (Consulta) vinculada a la mascota
     * POST /api/historias-clinicas/consultas
     */
    @PostMapping("/consultas")
    @Transactional
    public ResponseEntity<?> registrarConsulta(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long mascotaId = payload.get("mascotaId") != null ? Long.valueOf(payload.get("mascotaId").toString()) : null;
            Long veterinarioId = payload.get("veterinarioId") != null ? Long.valueOf(payload.get("veterinarioId").toString()) : null;
            Long citaId = payload.get("citaId") != null && !payload.get("citaId").toString().isEmpty() 
                          ? Long.valueOf(payload.get("citaId").toString()) : null;

            if (mascotaId == null || veterinarioId == null) {
                response.put("exito", false);
                response.put("mensaje", "Se requiere el ID de la mascota y del veterinario.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Optional<Mascota> mascotaOpt = mascotaRepository.findById(mascotaId);
            if (mascotaOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "La mascota no existe.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Optional<Usuario> vetOpt = usuarioRepository.findById(veterinarioId);
            if (vetOpt.isEmpty()) {
                response.put("exito", false);
                response.put("mensaje", "El médico veterinario no existe.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Mascota mascota = mascotaOpt.get();
            Usuario veterinario = vetOpt.get();

            // 1. Obtener o crear la Historia Clínica de la mascota
            HistoriaClinica historia = historiaClinicaRepository.findByMascotaId(mascotaId)
                .orElseGet(() -> {
                    String exp = "HC-" + String.format("%04d", mascota.getId()) + "-" + LocalDate.now().getYear();
                    HistoriaClinica nueva = new HistoriaClinica(mascota, exp, "Sin antecedentes reportados", "Ninguna");
                    return historiaClinicaRepository.save(nueva);
                });

            // 2. Crear el objeto Consulta
            Consulta consulta = new Consulta();
            consulta.setHistoriaClinica(historia);
            consulta.setVeterinario(veterinario);
            consulta.setFecha(LocalDate.now());
            consulta.setHora(LocalTime.now());

            consulta.setMotivo(payload.get("motivo") != null ? payload.get("motivo").toString() : "Consulta Médica");
            consulta.setSintomas(payload.get("sintomas") != null ? payload.get("sintomas").toString() : "");
            
            Double peso = payload.get("peso") != null ? Double.valueOf(payload.get("peso").toString()) : mascota.getPeso();
            consulta.setPeso(peso);
            // Actualizar peso reciente de la mascota
            if (peso != null && peso > 0) {
                mascota.setPeso(peso);
                mascotaRepository.save(mascota);
            }

            consulta.setTemperatura(payload.get("temperatura") != null ? payload.get("temperatura").toString() : "38.5 °C");
            consulta.setDiagnostico(payload.get("diagnostico") != null ? payload.get("diagnostico").toString() : "Sin diagnóstico especificado");
            consulta.setTratamiento(payload.get("tratamiento") != null ? payload.get("tratamiento").toString() : "Tratamiento general");
            consulta.setMedicamentos(payload.get("medicamentos") != null ? payload.get("medicamentos").toString() : "Sin prescripción farmacológica");
            consulta.setRecomendaciones(payload.get("recomendaciones") != null ? payload.get("recomendaciones").toString() : "");
            consulta.setObservaciones(payload.get("observaciones") != null ? payload.get("observaciones").toString() : "");

            // 3. Vincular con Cita si fue originada desde una cita previa
            if (citaId != null) {
                Optional<Cita> citaOpt = citaRepository.findById(citaId);
                if (citaOpt.isPresent()) {
                    Cita cita = citaOpt.get();
                    cita.setEstado("ATENDIDA");
                    citaRepository.save(cita);
                    consulta.setCita(cita);
                }
            }

            Consulta consultaGuardada = consultaRepository.save(consulta);

            response.put("exito", true);
            response.put("mensaje", "Atención médica registrada exitosamente en la Historia Clínica.");
            response.put("consulta", consultaGuardada);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al registrar la consulta médica: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
