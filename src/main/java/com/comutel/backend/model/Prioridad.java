package com.comutel.backend.model;

public enum Prioridad {
    ALTA(4),    // 4 Horas para resolver
    MEDIA(24),  // 24 Horas (1 día)
    BAJA(48);   // 48 Horas (2 días)

    private final int horasSLA;

    Prioridad(int horasSLA) {
        this.horasSLA = horasSLA;
    }

    public int getHorasSLA() {
        return horasSLA;
    }
}