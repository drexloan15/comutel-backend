package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class NuevoState implements TicketState {

    @Override
    public void siguiente(Ticket ticket, Usuario actor) {
        // De NUEVO pasa a EN_PROCESO
        // Regla: Solo un técnico o admin puede moverlo
        if (actor.getRol() == Usuario.Rol.CLIENTE) {
            throw new RuntimeException("❌ Error: Los clientes no pueden autogestionar el ticket.");
        }

        ticket.setEstado(Ticket.Estado.EN_PROCESO);
        System.out.println("🔄 Cambio de estado: NUEVO -> EN_PROCESO");
    }

    @Override
    public void cancelar(Ticket ticket, Usuario actor) {
        // Un ticket NUEVO sí se puede cancelar (ej: fue un error)
        ticket.setEstado(Ticket.Estado.CERRADO);
        System.out.println("🚫 Ticket cancelado directamente.");
    }

    @Override
    public void asignarTecnico(Ticket ticket, Usuario tecnico, Usuario actor) {
        // En estado NUEVO, sí permitimos asignar técnico
        if (actor.getRol() != Usuario.Rol.ADMIN && actor.getRol() != Usuario.Rol.TECNICO) {
            throw new RuntimeException("❌ Solo personal autorizado puede asignar técnicos.");
        }
        ticket.setTecnico(tecnico);
        System.out.println("👨‍🔧 Técnico " + tecnico.getNombre() + " asignado al ticket.");
    }
}