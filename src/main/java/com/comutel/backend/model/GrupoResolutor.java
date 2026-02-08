package com.comutel.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grupos_resolutores")
public class GrupoResolutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Ej: "Soporte Nivel 1", "Infraestructura", "Campo"

    private String descripcion;

    // Constructor vacío
    public GrupoResolutor() {}

    // Constructor rápido
    public GrupoResolutor(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}