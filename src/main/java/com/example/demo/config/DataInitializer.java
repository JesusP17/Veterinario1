package com.example.demo.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.model.Cita;
import com.example.demo.model.Mascota;
import com.example.demo.model.Servicio;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.MascotaRepository;
import com.example.demo.repository.ServicioRepository;
import com.example.demo.repository.UsuarioRepository;


import com.example.demo.model.Consulta;
import com.example.demo.model.HistoriaClinica;
import com.example.demo.repository.ConsultaRepository;
import com.example.demo.repository.HistoriaClinicaRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, 
                                      ServicioRepository servicioRepository,
                                      MascotaRepository mascotaRepository,
                                      CitaRepository citaRepository,
                                      HistoriaClinicaRepository historiaClinicaRepository,
                                      ConsultaRepository consultaRepository) {
                                    
        return args -> {
            // 1. Inicializar Catálogo de Servicios si está vacío
            if (servicioRepository.count() == 0) {
                servicioRepository.saveAll(Arrays.asList(
                    new Servicio("Consulta General", "Revisiones completas de salud, diagnóstico preciso y seguimiento personalizado para tu mascota.", 45000.0, 30, "Consulta", "🩺"),
                    new Servicio("Diagnóstico por Imagen", "Ecografías, radiografías digitales y tomografías para un diagnóstico temprano y preciso.", 95000.0, 45, "Diagnóstico", "🔬"),
                    new Servicio("Vacunación y Desparasitación", "Planes de vacunación completos y personalizados según la especie, raza y estilo de vida.", 35000.0, 20, "Prevención", "💉"),
                    new Servicio("Cirugía Especializada", "Procedimientos quirúrgicos de alta complejidad con equipamiento moderno y anestesia segura.", 250000.0, 90, "Cirugía", "🏥"),
                    new Servicio("Atención de Urgencia", "Atención médica prioritaria e inmediata para situaciones críticas las 24 horas.", 80000.0, 60, "Urgencia", "🚨"),
                    new Servicio("Estética y Spa", "Baños medicados, cortes de pelo según raza, limpieza dental y tratamientos de bienestar integral.", 40000.0, 60, "Estética", "✂️")
                ));
                System.out.println(">> [DataInitializer] Catálogo institucional de 6 servicios Canopolis inicializado con éxito en MySQL.");
            }

            // 2. Inicializar Usuarios Base con Roles Estándar si no existen
            if (usuarioRepository.findByEmail("admin@canopolis.com").isEmpty()) {
                Usuario admin = new Usuario("Administrador Canopolis", "admin@canopolis.com", "admin123", "ADMINISTRADOR", "3001234567", "Sede Central Canopolis");
                usuarioRepository.save(admin);
                System.out.println(">> [DataInitializer] Usuario Administrador creado: admin@canopolis.com / admin123");
            }

            Usuario vet = usuarioRepository.findByEmail("dr.garcia@canopolis.com").orElse(null);
            if (vet == null) {
                vet = new Usuario("Dr. David García", "dr.garcia@canopolis.com", "vet123", "VETERINARIO", "3109876543", "Consultorio 1 - Canopolis");
                vet = usuarioRepository.save(vet);
                System.out.println(">> [DataInitializer] Usuario Veterinario creado: dr.garcia@canopolis.com / vet123");
            }

            Usuario cliente = usuarioRepository.findByEmail("cliente@canopolis.com").orElse(null);
            if (cliente == null) {
                cliente = new Usuario("Camila Restrepo", "cliente@canopolis.com", "cliente123", "CLIENTE", "3205554321", "Calle 10 # 45-20");
                cliente = usuarioRepository.save(cliente);
                System.out.println(">> [DataInitializer] Usuario Cliente de prueba creado: cliente@canopolis.com / cliente123");
            }

            // 3. Mascota de prueba para el cliente si no tiene
            if (cliente != null && mascotaRepository.findByPropietarioId(cliente.getId()).isEmpty()) {
                Mascota toby = new Mascota("Toby", "Perro", "Golden Retriever", "MACHO", LocalDate.of(2022, 5, 10), 28.5, cliente);
                toby = mascotaRepository.save(toby);

                // Apertura de Historia Clínica
                HistoriaClinica hc = new HistoriaClinica(toby, "HC-0001-2026", "Esterilizado en 2023. Sin alergias previas.", "Ninguna");
                hc = historiaClinicaRepository.save(hc);

                // Consulta médica previa registrada
                Consulta consultaAnterior = new Consulta(
                    hc, vet, null,
                    LocalDate.now().minusMonths(1), LocalTime.of(9, 30),
                    "Vacunación anual y desparasitación",
                    "Paciente activo y alerta. Mucosas rosadas. Sin vómitos ni diarrea.",
                    28.0,
                    "Paciente canino clínicamente sano. Esquema de inmunización al día.",
                    "Aplicación de vacuna séxtuple canina y refuerzo antirrábico.",
                    "Simparica Trio 20-40kg (1 comprimido masticable mensual)"
                );
                consultaAnterior.setRecomendaciones("Mantener hidratación adecuada y evitar ejercicio intenso por 24 horas posteriores a la vacuna.");
                consultaRepository.save(consultaAnterior);
                System.out.println(">> [DataInitializer] Historia clínica y consulta médica previa inicializadas para Toby.");

                // Cita asignada de prueba para el Dr. David García
                if (citaRepository.count() == 0) {
                    Cita cita = new Cita(toby, "Consulta General", LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Control médico de rutina y chequeo dermatológico");
                    cita.setVeterinario(vet);
                    cita.setEstado("CONFIRMADA");
                    citaRepository.save(cita);
                    System.out.println(">> [DataInitializer] Cita médica de demostración asignada al Dr. David García para mañana a las 10:00 AM.");
                }
            }

           
        };
    }
}
