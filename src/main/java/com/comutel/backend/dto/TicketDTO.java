package com.comutel.backend.dto;

import com.comutel.backend.model.Activo;

import java.util.List;

public class TicketDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String estado;
    private String prioridad;
    private Long categoriaId;
    private String categoria;
    private String fechaCreacion;
    private String fechaVencimiento;
    private String grupoAsignado;
    private Long workflowInstanceId;
    private String workflowStateKey;
    private String processType;
    private String workflowKey;

    private UsuarioDTO usuario;
    private UsuarioDTO tecnico;
    private List<Activo> activos;

    public TicketDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(String grupoAsignado) { this.grupoAsignado = grupoAsignado; }
    public Long getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(Long workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
    public String getWorkflowStateKey() { return workflowStateKey; }
    public void setWorkflowStateKey(String workflowStateKey) { this.workflowStateKey = workflowStateKey; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO usuario) { this.usuario = usuario; }
    public UsuarioDTO getTecnico() { return tecnico; }
    public void setTecnico(UsuarioDTO tecnico) { this.tecnico = tecnico; }
    public List<Activo> getActivos() { return activos; }
    public void setActivos(List<Activo> activos) { this.activos = activos; }
}
