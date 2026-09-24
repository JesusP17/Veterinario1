package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Buscar consultas ordenadas por fecha y hora descendente para una historia clínica
    List<Consulta> findByHistoriaClinicaIdOrderByFechaDescHoraDesc(Long historiaClinicaId);

    // Buscar consultas de una mascota directamente
    @Query("SELECT c FROM Consulta c WHERE c.historiaClinica.mascota.id = :mascotaId ORDER BY c.fecha DESC, c.hora DESC")
    List<Consulta> findByMascotaId(@Param("mascotaId") Long mascotaId);

    // Buscar consultas atendidas por un veterinario
    List<Consulta> findByVeterinarioIdOrderByFechaDescHoraDesc(Long veterinarioId);
}
