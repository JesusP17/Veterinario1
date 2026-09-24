package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación con Mascota (Muchas citas pueden ser para la misma mascota)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;
    
    @Column(nullable = false, length = 100)
    private String servicio; // Ej: Consulta General, Vacunación, Cirugía
    
    // Relación opcional con la entidad Servicio (Fase 1)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "servicio_id", nullable = true)
    private Servicio servicioEntidad;

    // Relación opcional con el médico Veterinario asignado (Fase 3)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinario_id", nullable = true)
    private Usuario veterinario;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(nullable = false)
    private LocalTime hora;
    
    @Column(length = 500)
    private String motivo;
    
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE"; // PENDIENTE, CONFIRMADA, CANCELADA, REPROGRAMADA
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    // Constructor vacío (requerido por JPA)
    public Cita() {
        this.fechaRegistro = LocalDateTime.now();
    }
    
    // Constructor con parámetros básicos
    public Cita(Mascota mascota, String servicio, LocalDate fecha, LocalTime hora, String motivo) {
        this.mascota = mascota;
        this.servicio = servicio;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = "PENDIENTE";
        this.fechaRegistro = LocalDateTime.now();
    }

    // Constructor completo con entidad Servicio
    public Cita(Mascota mascota, Servicio servicioEntidad, LocalDate fecha, LocalTime hora, String motivo) {
        this.mascota = mascota;
        this.servicioEntidad = servicioEntidad;
        this.servicio = servicioEntidad != null ? servicioEntidad.getNombre() : "Consulta General";
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = "PENDIENTE";
        this.fechaRegistro = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Mascota getMascota() {
        return mascota;
    }
    
    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }
    
    public String getServicio() {
        return servicio;
    }
    
    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public Servicio getServicioEntidad() {
        return servicioEntidad;
    }

    public void setServicioEntidad(Servicio servicioEntidad) {
        this.servicioEntidad = servicioEntidad;
        if (servicioEntidad != null) {
            this.servicio = servicioEntidad.getNombre();
        }
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public LocalTime getHora() {
        return hora;
    }
    
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Usuario veterinario) {
        this.veterinario = veterinario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
