package com.comutel.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    // --- NUEVOS CAMPOS ---
    @Enumerated(EnumType.STRING)
    private Prioridad prioridad; // Relación con el Enum (ALTA, MEDIA, BAJA)

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria; // Relación con la nueva tabla Categoría

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento; // Se calcula automático (Creación + Horas SLA)

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Cliente

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico; // Técnico asignado

    // Enum interno de Estados (Si no lo tenías separado)
    public enum Estado {
        NUEVO, EN_PROCESO, PENDIENTE, RESUELTO, CERRADO
    }

    // Constructor vacío obligatorio para JPA
    public Ticket() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = Estado.NUEVO;
    }

    // --- LÓGICA DE NEGOCIO EN LA ENTIDAD ---

    // Método para calcular cuándo vence el ticket basado en su prioridad
    public void calcularVencimiento() {
        if (this.fechaCreacion != null && this.prioridad != null) {
            this.fechaVencimiento = this.fechaCreacion.plusHours(this.prioridad.getHorasSLA());
        }
    }
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private GrupoResolutor grupoAsignado;

    // Getters y Setters (Asegúrate de tenerlos todos, especialmente los nuevos)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getTecnico() { return tecnico; }
    public void setTecnico(Usuario tecnico) { this.tecnico = tecnico; }
    public GrupoResolutor getGrupoAsignado() { return grupoAsignado; }
    public void setGrupoAsignado(GrupoResolutor grupoAsignado) { this.grupoAsignado = grupoAsignado; }
}