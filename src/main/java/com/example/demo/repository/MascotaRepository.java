package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Mascota;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    
    // Método personalizado para buscar todas las mascotas de un mismo dueño
    List<Mascota> findByPropietarioId(Long propietarioId);
    
    // Método para buscar mascotas por nombre (útil para búsquedas)
    List<Mascota> findByNombreContainingIgnoreCase(String nombre);

    // Buscar si ya existe una mascota con ese nombre para un propietario (evita duplicados)
    Optional<Mascota> findByNombreAndPropietarioId(String nombre, Long propietarioId);
}