package com.example.demo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_expediente", nullable = false, unique = true, length = 50)
    private String numeroExpediente;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mascota_id", nullable = false, unique = true)
    private Mascota mascota;

    @Column(name = "fecha_apertura")
    private LocalDate fechaApertura;

    @Column(length = 500)
    private String antecedentes; // Cirugías previas, condiciones crónicas, etc.

    @Column(length = 250)
    private String alergias = "Ninguna reportada";

    @Column(name = "vacunas_al_dia")
    private Boolean vacunasAlDia = true;

    // Constructor vacío (requerido por JPA)
    public HistoriaClinica() {
        this.fechaApertura = LocalDate.now();
    }

    // Constructor con parámetros
    public HistoriaClinica(Mascota mascota, String numeroExpediente, String antecedentes, String alergias) {
        this.mascota = mascota;
        this.numeroExpediente = numeroExpediente;
        this.fechaApertura = LocalDate.now();
        this.antecedentes = antecedentes;
        this.alergias = alergias != null && !alergias.trim().isEmpty() ? alergias : "Ninguna reportada";
        this.vacunasAlDia = true;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDate fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public Boolean getVacunasAlDia() {
        return vacunasAlDia;
    }

    public void setVacunasAlDia(Boolean vacunasAlDia) {
        this.vacunasAlDia = vacunasAlDia;
    }
}
