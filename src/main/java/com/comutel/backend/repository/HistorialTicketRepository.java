package com.comutel.backend.repository;

import com.comutel.backend.model.HistorialTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialTicketRepository extends JpaRepository<HistorialTicket, Long> {
    // Para ver la cronología de un ticket específico
    List<HistorialTicket> findByTicketIdOrderByFechaDesc(Long ticketId);
}