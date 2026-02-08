package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ResueltoState implements TicketState {

    @Override
    public void siguiente(Ticket ticket, Usuario actor) {
        // De RESUELTO pasa a CERRADO (Fin del ciclo)
        ticket.setEstado(Ticket.Estado.CERRADO);
        System.out.println("🔒 Ticket CERRADO y archivado.");
    }

    @Override
    public void cancelar(Ticket ticket, Usuario actor) {
        throw new RuntimeException("⚠️ El ticket ya está resuelto. Ciérralo o ábrelo de nuevo.");
    }

    @Override
    public void asignarTecnico(Ticket ticket, Usuario tecnico, Usuario actor) {
        throw new RuntimeException("⚠️ No se puede cambiar técnico en un ticket resuelto.");
    }
}