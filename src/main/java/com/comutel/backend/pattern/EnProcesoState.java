package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class EnProcesoState implements TicketState {

    @Override
    public void siguiente(Ticket ticket, Usuario actor) {
        // De EN_PROCESO pasa a RESUELTO
        // Validación: No se puede resolver si no hay técnico asignado
        if (ticket.getTecnico() == null) {
            throw new RuntimeException("❌ No se puede resolver sin un técnico responsable.");
        }

        ticket.setEstado(Ticket.Estado.RESUELTO);
        System.out.println("✅ El trabajo ha terminado. Ticket RESUELTO.");
    }

    @Override
    public void cancelar(Ticket ticket, Usuario actor) {
        ticket.setEstado(Ticket.Estado.CERRADO);
        System.out.println("🚫 Ticket cerrado desde producción.");
    }

    @Override
    public void asignarTecnico(Ticket ticket, Usuario tecnico, Usuario actor) {
        // Se permite reasignar (cambiar de técnico)
        ticket.setTecnico(tecnico);
        System.out.println("🔄 Cambio de técnico responsable.");
    }
}