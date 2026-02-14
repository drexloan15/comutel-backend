package com.comutel.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "process_type")
    private String processType;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_default_id")
    private GrupoResolutor grupoDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id")
    private TipoCatalogo tipo;

    @Enumerated(EnumType.STRING)
    private Usuario.Rol rolAsignado;

    public Categoria() {}

    public Categoria(String nombre, Usuario.Rol rolAsignado) {
        this.nombre = nombre;
        this.rolAsignado = rolAsignado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public GrupoResolutor getGrupoDefault() { return grupoDefault; }
    public void setGrupoDefault(GrupoResolutor grupoDefault) { this.grupoDefault = grupoDefault; }

    public TipoCatalogo getTipo() { return tipo; }
    public void setTipo(TipoCatalogo tipo) { this.tipo = tipo; }

    public Usuario.Rol getRolAsignado() { return rolAsignado; }
    public void setRolAsignado(Usuario.Rol rolAsignado) { this.rolAsignado = rolAsignado; }
}
