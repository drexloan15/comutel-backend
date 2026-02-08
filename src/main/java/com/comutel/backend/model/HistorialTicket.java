package com.comutel.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_tickets")
public class HistorialTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private Usuario actor; // ¿Quién hizo el cambio?

    private String accion; // Ej: "CAMBIO_ESTADO", "ASIGNACION_GRUPO", "COMENTARIO"
    private String detalle; // Ej: "Cambió de NUEVO a EN_PROCESO"

    private LocalDateTime fecha;

    public HistorialTicket() {
        this.fecha = LocalDateTime.now();
    }

    public HistorialTicket(Ticket ticket, Usuario actor, String accion, String detalle) {
        this.ticket = ticket;
        this.actor = actor;
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public Usuario getActor() { return actor; }
    public void setActor(Usuario actor) { this.actor = actor; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public LocalDateTime getFecha() { return fecha; }
}