package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Buscar si ya existe una cita en una fecha y hora específica (método original)
    Cita findByFechaAndHora(LocalDate fecha, LocalTime hora);

    // RN-04: Buscar conflicto de horario excluyendo citas con estado CANCELADA
    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.hora = :hora AND c.estado <> 'CANCELADA'")
    Optional<Cita> buscarConflictoHorario(@Param("fecha") LocalDate fecha, @Param("hora") LocalTime hora);

    // Buscar citas por su estado (PENDIENTE, CONFIRMADA, etc.)
    List<Cita> findByEstado(String estado);
    
    // Buscar todas las citas de una mascota específica
    List<Cita> findByMascotaId(Long mascotaId);

    // Buscar citas por propietario de mascota ordenadas cronológicamente
    @Query("SELECT c FROM Cita c WHERE c.mascota.propietario.id = :propietarioId ORDER BY c.fecha DESC, c.hora DESC")
    List<Cita> findByPropietarioId(@Param("propietarioId") Long propietarioId);

    // Buscar citas para un día determinado ordenadas por hora (para panel médico)
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    // Buscar citas asignadas a un veterinario ordenadas cronológicamente
    @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :vetId ORDER BY c.fecha ASC, c.hora ASC")
    List<Cita> findByVeterinarioId(@Param("vetId") Long vetId);

    // Buscar citas para el panel veterinario: asignadas a él o citas PENDIENTES sin asignar
    @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :vetId OR (c.veterinario IS NULL AND c.estado = 'PENDIENTE') ORDER BY c.fecha ASC, c.hora ASC")
    List<Cita> findCitasParaVeterinario(@Param("vetId") Long vetId);
}