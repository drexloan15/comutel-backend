package com.comutel.backend.service;

import com.comutel.backend.dto.TicketDTO;
import com.comutel.backend.dto.UsuarioDTO;
import com.comutel.backend.model.*;
import com.comutel.backend.pattern.TicketState;
import com.comutel.backend.pattern.TicketStateFactory;
import com.comutel.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // 🏭 INYECCIÓN DE LA FÁBRICA (El cerebro de los estados)
    @Autowired
    private TicketStateFactory stateFactory;

    // --- 1. CREAR TICKET ---
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

        // Calcular SLA y Prioridad
        if (ticket.getPrioridad() == null) ticket.setPrioridad(Prioridad.BAJA);
        ticket.calcularVencimiento();

        Ticket ticketGuardado = ticketRepository.save(ticket);
        enviarCorreoCreacion(ticketGuardado, usuario);

        // 📝 Auditoría inicial
        registrarHistorial(ticketGuardado, usuario, "CREACIÓN", "Ticket creado en el sistema");

        return convertirADTO(ticketGuardado);
    }

    // --- 2. ATENDER TICKET (Con Auditoría) ---
    @Transactional
    public TicketDTO atenderTicket(Long ticketId, Long tecnicoId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        // A. Obtenemos el comportamiento del estado actual
        TicketState estadoActual = stateFactory.getState(ticket.getEstado());

        // B. Ejecutamos las reglas de negocio
        estadoActual.asignarTecnico(ticket, tecnico, tecnico);
        estadoActual.siguiente(ticket, tecnico);

        Ticket ticketActualizado = ticketRepository.save(ticket);

        // 📝 Auditoría: Guardamos que el técnico lo tomó
        registrarHistorial(ticket, tecnico, "ATENCIÓN", "Técnico " + tecnico.getNombre() + " inició la atención.");

        return convertirADTO(ticketActualizado);
    }

    // --- 3. FINALIZAR TICKET (Con Auditoría) ---
    @Transactional
    public TicketDTO finalizarTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // A. Obtenemos el comportamiento
        TicketState estadoActual = stateFactory.getState(ticket.getEstado());

        // B. Ejecutamos la transición
        Usuario actor = ticket.getTecnico();
        estadoActual.siguiente(ticket, actor);

        Ticket ticketGuardado = ticketRepository.save(ticket);
        enviarCorreoResolucion(ticketGuardado);

        // 📝 Auditoría: Guardamos el cierre
        registrarHistorial(ticket, actor, "RESOLUCIÓN", "Ticket resuelto y cerrado.");

        return convertirADTO(ticketGuardado);
    }

    // --- 4. NUEVA FUNCIONALIDAD: ASIGNAR A GRUPO ---
    @Transactional
    public TicketDTO asignarGrupo(Long ticketId, Long grupoId, Long usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        GrupoResolutor grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        Usuario actor = usuarioRepository.findById(usuarioActorId)
                .orElseThrow(() -> new RuntimeException("Usuario actor no encontrado"));

        // Lógica de Negocio
        ticket.setGrupoAsignado(grupo);
        ticket.setTecnico(null); // Al cambiar de grupo, se limpia el técnico anterior
        ticket.setEstado(Ticket.Estado.EN_PROCESO); // Pasa a proceso automáticamente

        Ticket ticketGuardado = ticketRepository.save(ticket);

        // 📝 Auditoría: Guardar en el historial
        registrarHistorial(ticket, actor, "REASIGNACIÓN", "Ticket derivado al grupo: " + grupo.getNombre());

        return convertirADTO(ticketGuardado);
    }

    // --- 5. MÉTODO GENÉRICO: AVANZAR ESTADO ---
    @Transactional
    public TicketDTO siguienteEstado(Long ticketId, Long usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Usuario actor = usuarioRepository.findById(usuarioActorId).orElseThrow();

        TicketState estadoActual = stateFactory.getState(ticket.getEstado());
        estadoActual.siguiente(ticket, actor);

        return convertirADTO(ticketRepository.save(ticket));
    }

    // --- 6. MÉTODOS AUXILIARES ---

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
            if (!posibleImagen.isEmpty() && !posibleImagen.equals("null")) imagen = posibleImagen;
        }

        // Registrar comentario también como actividad si deseas, o dejarlo separado
        return comentarioRepository.save(new Comentario(texto, autor, ticket, imagen));
    }

    public Map<String, Long> obtenerMetricas() {
        Map<String, Long> metricas = new HashMap<>();
        metricas.put("total", ticketRepository.count());
        metricas.put("nuevos", ticketRepository.countByEstado(Ticket.Estado.NUEVO));
        metricas.put("proceso", ticketRepository.countByEstado(Ticket.Estado.EN_PROCESO));
        metricas.put("resueltos", ticketRepository.countByEstado(Ticket.Estado.RESUELTO));
        return metricas;
    }

    public List<TicketDTO> obtenerTodos() {
        return ticketRepository.findAll().stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    public Ticket obtenerPorId(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    // --- MÉTODO PRIVADO PARA AUDITORÍA ---
    private void registrarHistorial(Ticket ticket, Usuario actor, String accion, String detalle) {
        if (actor == null) return; // Evitar null pointer si no hay actor definido
        HistorialTicket historial = new HistorialTicket(ticket, actor, accion, detalle);
        historialRepository.save(historial);
    }

    private void enviarCorreoCreacion(Ticket ticket, Usuario usuario) {
        try {
            emailSenderService.enviarNotificacion(usuario.getEmail(), "Ticket #" + ticket.getId(), "Recibido: " + ticket.getTitulo());
            emailSenderService.enviarNotificacion("jean.puccio@comutelperu.com", "🚨 Nuevo Ticket", "Cliente: " + usuario.getNombre());
        } catch (Exception e) { System.err.println("Error email: " + e.getMessage()); }
    }

    private void enviarCorreoResolucion(Ticket ticket) {
        try {
            emailSenderService.enviarNotificacion(ticket.getUsuario().getEmail(), "Ticket Resuelto", "Tu ticket ha sido resuelto.");
        } catch (Exception e) { System.err.println("Error email: " + e.getMessage()); }
    }

    // --- CONVERTIDOR DTO (Corregido y Limpio) ---
    private TicketDTO convertirADTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitulo(ticket.getTitulo());
        dto.setDescripcion(ticket.getDescripcion());

        dto.setEstado(ticket.getEstado() != null ? ticket.getEstado().toString() : "NUEVO");
        dto.setPrioridad(ticket.getPrioridad() != null ? ticket.getPrioridad().toString() : "BAJA");

        if (ticket.getCategoria() != null) dto.setCategoria(ticket.getCategoria().getNombre());

        if (ticket.getGrupoAsignado() != null) {
            dto.setGrupoAsignado(ticket.getGrupoAsignado().getNombre());
        }

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
}