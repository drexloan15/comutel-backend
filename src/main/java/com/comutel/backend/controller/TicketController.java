package com.comutel.backend.controller;

import com.comutel.backend.dto.TicketDTO;
import com.comutel.backend.model.*;
import com.comutel.backend.repository.ActivoRepository;
import com.comutel.backend.repository.ComentarioRepository;
import com.comutel.backend.repository.UsuarioRepository;
import com.comutel.backend.service.EmailSenderService;
import com.comutel.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EmailSenderService emailService;

    @Autowired
    private ActivoRepository activoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @PostMapping
    public TicketDTO crearTicket(@RequestBody Map<String, Object> payload) {
        Ticket ticket = new Ticket();
        ticket.setTitulo((String) payload.get("titulo"));
        ticket.setDescripcion((String) payload.get("descripcion"));
        if (payload.get("processType") != null) {
            ticket.setProcessType(payload.get("processType").toString());
        }
        if (payload.get("workflowKey") != null) {
            ticket.setWorkflowKey(payload.get("workflowKey").toString());
        }

        String prioridadStr = (String) payload.get("prioridad");
        if (prioridadStr != null && !prioridadStr.isBlank()) {
            try {
                ticket.setPrioridad(Ticket.Prioridad.valueOf(prioridadStr.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(BAD_REQUEST, "prioridad invalida");
            }
        }

        Object userIdObj = payload.get("usuarioId");
        if (userIdObj != null) {
            Long usuarioId = Long.valueOf(userIdObj.toString());
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Usuario no encontrado"));
            ticket.setUsuario(usuario);
        } else {
            throw new ResponseStatusException(BAD_REQUEST, "usuarioId es obligatorio");
        }

        return ticketService.crearTicket(ticket);
    }

    @GetMapping
    public List<TicketDTO> obtenerTodos(@RequestParam Long usuarioId) {
        return ticketService.obtenerTodos(usuarioId);
    }

    @PutMapping("/{id}/atender/{tecnicoId}")
    public TicketDTO atenderTicket(@PathVariable Long id, @PathVariable Long tecnicoId) {
        return ticketService.atenderTicket(id, tecnicoId);
    }

    @PutMapping("/{id}/finalizar")
    public TicketDTO finalizarTicket(@PathVariable Long id, @RequestBody String notaCierre) {
        return ticketService.finalizarTicket(id, notaCierre);
    }

    @GetMapping("/metricas")
    public Map<String, Long> obtenerMetricas() {
        return ticketService.obtenerMetricas();
    }

    @GetMapping("/{id}/comentarios")
    public List<Comentario> verComentarios(@PathVariable Long id) {
        return comentarioRepository.findByTicketId(id);
    }

    @PostMapping("/{id}/comentarios")
    public Comentario agregarComentario(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return ticketService.agregarComentario(id, payload);
    }

    @PutMapping("/{id}/asignar-grupo/{grupoId}")
    public TicketDTO asignarGrupo(@PathVariable Long id, @PathVariable Long grupoId, @RequestParam Long actorId) {
        return ticketService.asignarGrupo(id, grupoId, actorId);
    }

    @PutMapping("/{id}/derivar")
    public TicketDTO derivarTicket(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object grupoIdObj = payload.get("grupoId");
        Object actorIdObj = payload.get("actorId");

        if (grupoIdObj == null || actorIdObj == null) {
            throw new ResponseStatusException(BAD_REQUEST, "grupoId y actorId son obligatorios");
        }

        Long grupoId = Long.valueOf(grupoIdObj.toString());
        Long actorId = Long.valueOf(actorIdObj.toString());

        Long tecnicoId = null;
        if (payload.get("tecnicoId") != null && !payload.get("tecnicoId").toString().isBlank()) {
            tecnicoId = Long.valueOf(payload.get("tecnicoId").toString());
        }

        return ticketService.derivarTicket(id, grupoId, tecnicoId, actorId);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialTicket> obtenerHistorial(@PathVariable Long id) {
        return ticketService.obtenerHistorial(id);
    }

    @GetMapping("/{id}")
    public TicketDTO obtenerPorId(@PathVariable Long id, @RequestParam Long usuarioId) {
        return ticketService.obtenerTicketDTO(id, usuarioId);
    }

    @PostMapping("/{id}/iniciar-chat")
    public void notificarInicioChat(@PathVariable Long id, @RequestParam Long usuarioId) {
        ticketService.iniciarChat(id, usuarioId);
    }

    @PostMapping("/{id}/enviar-correo")
    public void enviarCorreoManual(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        TicketDTO ticket = ticketService.obtenerTicketDTO(id);
        if (ticket.getUsuario() != null) {
            emailService.enviarNotificacion(ticket.getUsuario().getEmail(), payload.get("asunto"), payload.get("mensaje"));
        }
    }

    @PutMapping("/{id}/vincular-activo/{activoId}")
    public TicketDTO vincularActivo(@PathVariable Long id, @PathVariable Long activoId) {
        return ticketService.vincularActivo(id, activoId);
    }

    @GetMapping("/activos")
    public List<Activo> listarActivos() {
        return activoRepository.findAll();
    }

    @PostMapping("/activos")
    public Activo crearActivo(@RequestBody Activo activo) {
        return activoRepository.save(activo);
    }

    @PutMapping("/{id}/asignar/{tecnicoId}")
    public ResponseEntity<TicketDTO> asignarTecnico(@PathVariable Long id, @PathVariable Long tecnicoId) {
        TicketDTO ticketActualizado = ticketService.asignarTecnico(id, tecnicoId);
        return ResponseEntity.ok(ticketActualizado);
    }

    @PostMapping("/{id}/transition/{eventKey}")
    public TicketDTO ejecutarEventoWorkflow(
            @PathVariable Long id,
            @PathVariable String eventKey,
            @RequestParam(required = false) Long actorId,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        return ticketService.ejecutarEventoWorkflow(id, eventKey, actorId, payload);
    }
}

