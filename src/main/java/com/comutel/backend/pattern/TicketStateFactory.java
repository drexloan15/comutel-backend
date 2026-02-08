package com.comutel.backend.pattern;

import com.comutel.backend.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketStateFactory {

    @Autowired private NuevoState nuevoState;
    @Autowired private EnProcesoState enProcesoState;
    @Autowired private ResueltoState resueltoState;
    @Autowired private CerradoState cerradoState;

    public TicketState getState(Ticket.Estado estado) {
        if (estado == null) return nuevoState; // Por defecto

        switch (estado) {
            case NUEVO: return nuevoState;
            case EN_PROCESO: return enProcesoState;
            case RESUELTO: return resueltoState;
            case CERRADO: return cerradoState;
            // Si agregas más estados (ej: PENDIENTE), añádelos aquí
            default: return nuevoState;
        }
    }
}