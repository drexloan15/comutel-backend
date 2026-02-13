package com.comutel.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "permisos_config")
public class PermisosConfig {

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean rolCliente = false;

    @Column(nullable = false)
    private boolean rolTecnico = false;

    @Column(nullable = false)
    private boolean rolAdmin = true;

    @Column(nullable = false)
    private boolean rolTesterAdmin = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permisos_config_usuarios", joinColumns = @JoinColumn(name = "config_id"))
    @MapKeyColumn(name = "usuario_id")
    @Column(name = "permitido")
    private Map<String, Boolean> usuarios = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permisos_config_grupos", joinColumns = @JoinColumn(name = "config_id"))
    @MapKeyColumn(name = "grupo_id")
    @Column(name = "permitido")
    private Map<String, Boolean> grupos = new HashMap<>();

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRolCliente() {
        return rolCliente;
    }

    public void setRolCliente(boolean rolCliente) {
        this.rolCliente = rolCliente;
    }

    public boolean isRolTecnico() {
        return rolTecnico;
    }

    public void setRolTecnico(boolean rolTecnico) {
        this.rolTecnico = rolTecnico;
    }

    public boolean isRolAdmin() {
        return rolAdmin;
    }

    public void setRolAdmin(boolean rolAdmin) {
        this.rolAdmin = rolAdmin;
    }

    public boolean isRolTesterAdmin() {
        return rolTesterAdmin;
    }

    public void setRolTesterAdmin(boolean rolTesterAdmin) {
        this.rolTesterAdmin = rolTesterAdmin;
    }

    public Map<String, Boolean> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Map<String, Boolean> usuarios) {
        this.usuarios = usuarios;
    }

    public Map<String, Boolean> getGrupos() {
        return grupos;
    }

    public void setGrupos(Map<String, Boolean> grupos) {
        this.grupos = grupos;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
