package com.comutel.backend.controller;

import com.comutel.backend.dto.TicketDTO;
import com.comutel.backend.model.Comentario;
import com.comutel.backend.model.HistorialTicket;
import com.comutel.backend.model.Ticket;
import com.comutel.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:5173")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // 1. Crear
    @PostMapping
    public TicketDTO crearTicket(@RequestBody Ticket ticket) {
        return ticketService.crearTicket(ticket);
    }

    // 2. Ver Todos
    @GetMapping
    public List<TicketDTO> obtenerTodos() {
        return ticketService.obtenerTodos();
    }

    // 3. Atender (Asignar técnico)
    @PutMapping("/{id}/atender/{tecnicoId}")
    public TicketDTO atenderTicket(@PathVariable Long id, @PathVariable Long tecnicoId) {
        return ticketService.atenderTicket(id, tecnicoId);
    }

    // 4. Finalizar con Nota
    @PutMapping("/{id}/finalizar")
    public TicketDTO finalizarTicket(@PathVariable Long id, @RequestBody String notaCierre) {
        // Pasamos la nota al servicio
        return ticketService.finalizarTicket(id, notaCierre);
    }

    // 5. Métricas
    @GetMapping("/metricas")
    public Map<String, Long> obtenerMetricas() {
        return ticketService.obtenerMetricas();
    }

    // 6. Ver Comentarios
    @GetMapping("/{id}/comentarios")
    public List<Comentario> verComentarios(@PathVariable Long id) {
        return ticketService.obtenerComentarios(id);
    }

    // 7. Agregar Comentario
    @PostMapping("/{id}/comentarios")
    public Comentario agregarComentario(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ticketService.agregarComentario(id, payload);
    }
    @PutMapping("/{id}/asignar-grupo/{grupoId}")
    public TicketDTO asignarGrupo(
            @PathVariable Long id,
            @PathVariable Long grupoId,
            @RequestParam Long actorId) { // Enviamos quién lo hizo como parámetro
        return ticketService.asignarGrupo(id, grupoId, actorId);
    }
    @GetMapping("/{id}/historial")
    public List<HistorialTicket> obtenerHistorial(@PathVariable Long id) {
        return ticketService.obtenerHistorial(id);
    }

    // --- AGREGAR ESTE MÉTODO ---
    // 8. Obtener un ticket por ID (Para refrescar el detalle)
    @GetMapping("/{id}")
    public TicketDTO obtenerPorId(@PathVariable Long id) {
        Ticket ticket = ticketService.obtenerPorId(id);
        // Necesitamos convertirlo a DTO manualmente aquí o en el servicio
        // Para hacerlo rápido, usaremos un DTO simple o exponemos la entidad
        // (Lo ideal es usar el convertidor del servicio, pero el método es privado)

        // OPCIÓN RÁPIDA: Crear un método público en el servicio o devolver la entidad
        // Vamos a asumir que exponemos la entidad por ahora para desbloquearte
        // O mejor, hagámoslo bien:
        return ticketService.obtenerTicketDTO(id);
    }


}