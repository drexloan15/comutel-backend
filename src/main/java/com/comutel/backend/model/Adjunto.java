package com.comutel.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Adjunto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;
    private String tipoContenido;
    private String url;

    private LocalDateTime fechaSubida;

    @JsonIgnore
    @ManyToOne
    private Ticket ticket;

    @JsonIgnore
    @ManyToOne
    private Usuario subidoPor;

    public Adjunto() {
        this.fechaSubida = LocalDateTime.now();
    }
}

