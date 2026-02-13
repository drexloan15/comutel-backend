package com.comutel.backend.service;

import com.comutel.backend.dto.TicketDTO;
import com.comutel.backend.dto.UsuarioDTO;
import com.comutel.backend.model.*;
import com.comutel.backend.repository.*;
import com.comutel.backend.workflow.integration.TicketWorkflowBridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private GrupoResolutorRepository grupoRepository;

    @Autowired
    private HistorialTicketRepository historialRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private ActivoRepository activoRepository;

    @Autowired
    private TicketWorkflowBridge ticketWorkflowBridge;

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    @Transactional
    public TicketDTO crearTicket(Ticket ticket) {
        if (ticket.getUsuario() == null || ticket.getUsuario().getId() == null) {
            throw new RuntimeException("Error: El ticket no tiene usuario asignado.");
        }

        Usuario usuario = usuarioRepository.findById(ticket.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ticket.setUsuario(usuario);
        ticket.setEstado(Ticket.Estado.NUEVO);
        ticket.setTecnico(null);
        if (ticket.getProcessType() == null || ticket.getProcessType().isBlank()) {
            ticket.setProcessType(TicketWorkflowBridge.DEFAULT_PROCESS_TYPE);
        }
        if (ticket.getWorkflowKey() == null || ticket.getWorkflowKey().isBlank()) {
            ticket.setWorkflowKey(TicketWorkflowBridge.DEFAULT_WORKFLOW_KEY);
        }

        if (ticket.getPrioridad() == null) {
            ticket.setPrioridad(Ticket.Prioridad.BAJA);
        }
        ticket.calcularVencimiento();

        Ticket ticketGuardado = ticketRepository.save(ticket);
        ticketGuardado = ticketWorkflowBridge.startForTicket(ticketGuardado, usuario.getId());
        enviarCorreoCreacion(ticketGuardado, usuario);
        registrarHistorial(ticketGuardado, usuario, "CREACION", "Ticket creado en el sistema");

        return convertirADTO(ticketGuardado);
    }

    @Transactional
    public TicketDTO atenderTicket(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));

        validarTecnicoParaGrupo(ticket, tecnico);

        if (ticket.getEstado() == Ticket.Estado.RESUELTO || ticket.getEstado() == Ticket.Estado.CERRADO) {
            throw new RuntimeException("No se puede tomar un ticket cerrado o resuelto.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("tecnicoId", tecnicoId);
        if (ticket.getGrupoAsignado() != null) {
            payload.put("grupoId", ticket.getGrupoAsignado().getId());
        }

        Ticket ticketActualizado = ticketWorkflowBridge.fireEvent(ticket, "TAKE_OWNERSHIP", tecnicoId, payload);
        registrarHistorial(ticketActualizado, tecnico, "ATENCION", "Tecnico " + tecnico.getNombre() + " inicio la atencion.");

        return convertirADTO(ticketActualizado);
    }

    @Transactional
    public TicketDTO finalizarTicket(Long ticketId, String notaCierre) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (ticket.getEstado() == Ticket.Estado.RESUELTO || ticket.getEstado() == Ticket.Estado.CERRADO) {
            throw new RuntimeException("El ticket ya esta resuelto.");
        }

        Usuario actor = ticket.getTecnico();
        Map<String, Object> payload = new HashMap<>();
        payload.put("notaCierre", notaCierre);
        Ticket ticketGuardado = ticketWorkflowBridge.fireEvent(
                ticket,
                "RESOLVE",
                actor != null ? actor.getId() : null,
                payload
        );

        try {
            enviarCorreoResolucion(ticketGuardado);
        } catch (Exception e) {
            System.err.println("No se pudo enviar correo: " + e.getMessage());
        }

        String detalleAuditoria = "Ticket resuelto. Solucion tecnica: " + notaCierre;
        registrarHistorial(ticketGuardado, actor, "RESOLUCION", detalleAuditoria);

        return convertirADTO(ticketGuardado);
    }

    @Transactional
    public TicketDTO asignarGrupo(Long ticketId, Long grupoId, Long usuarioActorId) {
        return derivarTicket(ticketId, grupoId, null, usuarioActorId);
    }

    @Transactional
    public TicketDTO derivarTicket(Long ticketId, Long grupoId, Long tecnicoId, Long usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        GrupoResolutor grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario actor = usuarioRepository.findById(usuarioActorId)
                .orElseThrow(() -> new RuntimeException("Usuario actor no encontrado"));

        if (actor.getRol() == Usuario.Rol.CLIENTE) {
            throw new RuntimeException("Solo ADMIN o TECNICO pueden derivar tickets.");
        }

        Usuario tecnicoDestino = null;
        if (tecnicoId != null) {
            tecnicoDestino = usuarioRepository.findById(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Tecnico destino no encontrado"));
            validarTecnicoParaGrupo(grupo, tecnicoDestino);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("grupoId", grupoId);
        if (tecnicoId != null) {
            payload.put("tecnicoId", tecnicoId);
        }

        Ticket ticketGuardado = ticketWorkflowBridge.fireEvent(ticket, "ESCALATE", usuarioActorId, payload);

        String detalle = "Ticket derivado al grupo: " + grupo.getNombre();
        if (tecnicoDestino != null) {
            detalle += ". Tecnico asignado: " + tecnicoDestino.getNombre();
        }
        registrarHistorial(ticketGuardado, actor, "REASIGNACION", detalle);

        return convertirADTO(ticketGuardado);
    }

    @Transactional
    public TicketDTO siguienteEstado(Long ticketId, Long usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Usuario actor = usuarioRepository.findById(usuarioActorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Ticket updated = ticketWorkflowBridge.fireEvent(ticket, "NEXT", actor.getId(), Map.of());
        return convertirADTO(updated);
    }

    @Transactional
    public TicketDTO ejecutarEventoWorkflow(Long ticketId, String eventKey, Long actorId, Map<String, Object> payload) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Ticket updated = ticketWorkflowBridge.fireEvent(ticket, eventKey, actorId, payload == null ? Map.of() : payload);
        Usuario actor = actorId == null ? null : usuarioRepository.findById(actorId).orElse(null);
        registrarHistorial(updated, actor, "WORKFLOW_EVENT", "Evento " + eventKey + " ejecutado sobre workflow.");
        return convertirADTO(updated);
    }

    public List<Comentario> obtenerComentarios(Long ticketId) {
        return comentarioRepository.findByTicketId(ticketId);
    }

    @Transactional
    public Comentario agregarComentario(Long ticketId, Map<String, Object> payload) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Long autorId = Long.valueOf(payload.get("autorId").toString());
        Usuario autor = usuarioRepository.findById(autorId).orElseThrow();
        String texto = payload.get("texto").toString();

        String imagen = null;
        if (payload.containsKey("imagen") && payload.get("imagen") != null) {
            String posibleImagen = payload.get("imagen").toString();
            if (!posibleImagen.isEmpty() && !"null".equals(posibleImagen)) {
                imagen = posibleImagen;
            }
        }

        return comentarioRepository.save(new Comentario(texto, autor, ticket, imagen));
    }

    public Map<String, Long> obtenerMetricas() {
        Map<String, Long> metricas = new HashMap<>();

        metricas.put("total", ticketRepository.count());
        metricas.put("nuevos", ticketRepository.countByEstado(Ticket.Estado.NUEVO));
        metricas.put("proceso", ticketRepository.countByEstado(Ticket.Estado.EN_PROCESO));
        metricas.put("resueltos", ticketRepository.countByEstado(Ticket.Estado.RESUELTO));
        metricas.put("cerrados", ticketRepository.countByEstado(Ticket.Estado.CERRADO));

        long criticos = ticketRepository.findAll().stream()
                .filter(t -> t.getPrioridad() == Ticket.Prioridad.ALTA && t.getEstado() != Ticket.Estado.RESUELTO)
                .count();
        metricas.put("criticos", criticos);

        return metricas;
    }

    public List<TicketDTO> obtenerTodos(Long usuarioId) {
        if (usuarioId == null) {
            throw new RuntimeException("usuarioId es obligatorio");
        }

        List<Ticket> ticketsVisibles;
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (esRolAdministrador(usuario)) {
            ticketsVisibles = ticketRepository.findAll();
        } else if (usuario.getRol() == Usuario.Rol.TECNICO) {
            Set<Long> gruposTecnico = usuario.getGrupos().stream()
                    .map(GrupoResolutor::getId)
                    .collect(Collectors.toSet());

            if (gruposTecnico.isEmpty()) {
                ticketsVisibles = List.of();
            } else {
                ticketsVisibles = ticketRepository.findDistinctByGrupoAsignadoIdIn(gruposTecnico);
            }
        } else {
            ticketsVisibles = ticketRepository.findByUsuarioId(usuarioId);
        }

        return ticketsVisibles.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    public Ticket obtenerPorId(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    private void registrarHistorial(Ticket ticket, Usuario actor, String accion, String detalle) {
        if (actor == null) {
            return;
        }
        HistorialTicket historial = new HistorialTicket(ticket, actor, accion, detalle);
        historialRepository.save(historial);
    }

    private void enviarCorreoCreacion(Ticket ticket, Usuario usuario) {
        try {
            emailSenderService.enviarNotificacion(usuario.getEmail(), "Ticket #" + ticket.getId(), "Recibido: " + ticket.getTitulo());
            emailSenderService.enviarNotificacion("jean.puccio@comutelperu.com", "Nuevo Ticket", "Cliente: " + usuario.getNombre());
        } catch (Exception e) {
            System.err.println("Error email: " + e.getMessage());
        }
    }

    private void enviarCorreoResolucion(Ticket ticket) {
        try {
            emailSenderService.enviarNotificacion(ticket.getUsuario().getEmail(), "Ticket Resuelto", "Tu ticket ha sido resuelto.");
        } catch (Exception e) {
            System.err.println("Error email: " + e.getMessage());
        }
    }

    private TicketDTO convertirADTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitulo(ticket.getTitulo());
        dto.setDescripcion(ticket.getDescripcion());
        dto.setActivos(ticket.getActivosAfectados());

        dto.setEstado(ticket.getEstado() != null ? ticket.getEstado().toString() : "NUEVO");
        dto.setPrioridad(ticket.getPrioridad() != null ? ticket.getPrioridad().toString() : "BAJA");

        if (ticket.getCategoria() != null) {
            dto.setCategoria(ticket.getCategoria().getNombre());
        }

        if (ticket.getGrupoAsignado() != null) {
            dto.setGrupoAsignado(ticket.getGrupoAsignado().getNombre());
        }

        dto.setWorkflowInstanceId(ticket.getWorkflowInstanceId());
        dto.setWorkflowStateKey(ticket.getWorkflowStateKey());
        dto.setProcessType(ticket.getProcessType());

        dto.setFechaCreacion(ticket.getFechaCreacion() != null ? ticket.getFechaCreacion().toString() : null);
        dto.setFechaVencimiento(ticket.getFechaVencimiento() != null ? ticket.getFechaVencimiento().toString() : null);

        if (ticket.getUsuario() != null) {
            Usuario u = ticket.getUsuario();
            dto.setUsuario(new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol().toString()));
        }

        if (ticket.getTecnico() != null) {
            Usuario t = ticket.getTecnico();
            dto.setTecnico(new UsuarioDTO(t.getId(), t.getNombre(), t.getEmail(), t.getRol().toString()));
        }

        return dto;
    }

    public List<HistorialTicket> obtenerHistorial(Long ticketId) {
        return historialRepository.findByTicketIdOrderByFechaDesc(ticketId);
    }

    public TicketDTO obtenerTicketDTO(Long id) {
        Ticket ticket = obtenerPorId(id);
        return convertirADTO(ticket);
    }

    public TicketDTO obtenerTicketDTO(Long id, Long usuarioId) {
        Ticket ticket = obtenerPorId(id);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!puedeVerTicket(usuario, ticket)) {
            throw new RuntimeException("No tienes permisos para ver este ticket.");
        }

        return convertirADTO(ticket);
    }

    public void iniciarChat(Long ticketId, Long usuarioId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario iniciador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String mensaje = "El usuario " + iniciador.getNombre() + " ha iniciado un chat en el ticket #" + ticket.getId();
        System.out.println("LOG: " + mensaje);

        if (!ticket.getUsuario().getId().equals(usuarioId)) {
            emailSenderService.enviarNotificacion(
                    ticket.getUsuario().getEmail(),
                    "Chat iniciado en Ticket #" + ticket.getId(),
                    "Un tecnico ha iniciado el chat para atender tu solicitud."
            );
        }

        if (ticket.getTecnico() != null && !ticket.getTecnico().getId().equals(usuarioId)) {
            emailSenderService.enviarNotificacion(
                    ticket.getTecnico().getEmail(),
                    "Chat iniciado en Ticket #" + ticket.getId(),
                    "El usuario ha iniciado el chat en el ticket que atiendes."
            );
        }
    }

    @Transactional
    public TicketDTO vincularActivo(Long ticketId, Long activoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Activo activo = activoRepository.findById(activoId)
                .orElseThrow(() -> new RuntimeException("Activo no encontrado"));

        if (ticket.getActivosAfectados() == null) {
            ticket.setActivosAfectados(new ArrayList<>());
        }

        if (!ticket.getActivosAfectados().contains(activo)) {
            ticket.getActivosAfectados().add(activo);
            ticketRepository.save(ticket);
        }

        return convertirADTO(ticket);
    }

    @Transactional
    public TicketDTO asignarTecnico(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));

        validarTecnicoParaGrupo(ticket, tecnico);

        if (ticket.getEstado() == Ticket.Estado.RESUELTO || ticket.getEstado() == Ticket.Estado.CERRADO) {
            throw new RuntimeException("No se puede asignar tecnico a un ticket cerrado o resuelto.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("tecnicoId", tecnicoId);
        if (ticket.getGrupoAsignado() != null) {
            payload.put("grupoId", ticket.getGrupoAsignado().getId());
        }
        Ticket savedTicket = ticketWorkflowBridge.fireEvent(ticket, "ASSIGN_TECHNICIAN", tecnicoId, payload);
        registrarHistorial(savedTicket, tecnico, "AUTO_ASIGNACION", "Tecnico se auto-asigno el ticket.");

        return convertirADTO(savedTicket);
    }

    private void validarTecnicoParaGrupo(Ticket ticket, Usuario tecnico) {
        if (ticket.getGrupoAsignado() == null) {
            return;
        }
        validarTecnicoParaGrupo(ticket.getGrupoAsignado(), tecnico);
    }

    private void validarTecnicoParaGrupo(GrupoResolutor grupo, Usuario tecnico) {
        if (tecnico.getRol() != Usuario.Rol.TECNICO && !esRolAdministrador(tecnico)) {
            throw new RuntimeException("El usuario seleccionado no es tecnico.");
        }

        if (esRolAdministrador(tecnico)) {
            return;
        }

        boolean perteneceAlGrupo = tecnico.getGrupos().stream()
                .anyMatch(g -> g.getId().equals(grupo.getId()));

        if (!perteneceAlGrupo) {
            throw new RuntimeException("El tecnico no pertenece al grupo seleccionado. Asignalo al grupo antes de derivar.");
        }
    }

    private boolean puedeVerTicket(Usuario usuario, Ticket ticket) {
        if (esRolAdministrador(usuario)) {
            return true;
        }

        if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            return ticket.getUsuario() != null && ticket.getUsuario().getId().equals(usuario.getId());
        }

        if (ticket.getGrupoAsignado() == null) {
            return false;
        }

        return usuario.getGrupos().stream().anyMatch(g -> g.getId().equals(ticket.getGrupoAsignado().getId()));
    }

    private boolean esRolAdministrador(Usuario usuario) {
        return usuario.getRol() == Usuario.Rol.ADMIN || usuario.getRol() == Usuario.Rol.TESTERADMIN;
    }
}
