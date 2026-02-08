package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CerradoState implements TicketState {

    @Override
    public void siguiente(Ticket ticket, Usuario actor) {
        throw new RuntimeException("⛔ Error: El ticket ya está CERRADO. No tiene más estados.");
    }

    @Override
    public void cancelar(Ticket ticket, Usuario actor) {
        throw new RuntimeException("⛔ Error: El ticket ya está cerrado.");
    }

    @Override
    public void asignarTecnico(Ticket ticket, Usuario tecnico, Usuario actor) {
        throw new RuntimeException("⛔ Error: No se puede asignar técnico a un ticket cerrado.");
    }
}