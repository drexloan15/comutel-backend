package com.comutel.backend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Ej: "Redes", "Hardware"

    // Aquí definimos qué rol por defecto atiende esta categoría (Mapeo automático)
    @Enumerated(EnumType.STRING)
    private Usuario.Rol rolAsignado;

    // Constructor vacío
    public Categoria() {}

    // Constructor con datos
    public Categoria(String nombre, Usuario.Rol rolAsignado) {
        this.nombre = nombre;
        this.rolAsignado = rolAsignado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Usuario.Rol getRolAsignado() { return rolAsignado; }
    public void setRolAsignado(Usuario.Rol rolAsignado) { this.rolAsignado = rolAsignado; }
}