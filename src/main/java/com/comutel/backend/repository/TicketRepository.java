package com.comutel.backend.repository;

import com.comutel.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
@Repository

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUsuarioId(Long usuarioId);
    List<Ticket> findDistinctByGrupoAsignadoIdIn(Set<Long> grupoIds);
    long countByEstado(Ticket.Estado estado);
}
