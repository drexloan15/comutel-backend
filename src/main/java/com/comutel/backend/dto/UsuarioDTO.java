package com.comutel.backend.dto;

public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;

    // Constructor vacío
    public UsuarioDTO() {}

    // Constructor completo
    public UsuarioDTO(Long id, String nombre, String email, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}