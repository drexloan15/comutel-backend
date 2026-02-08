package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;

public interface TicketState {
    // Intentar mover al siguiente estado lógico
    void siguiente(Ticket ticket, Usuario actor);

    // Intentar cancelar o cerrar el ticket
    void cancelar(Ticket ticket, Usuario actor);

    // Asignar un técnico (solo permitido en ciertos estados)
    void asignarTecnico(Ticket ticket, Usuario tecnico, Usuario actor);
}