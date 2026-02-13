package com.comutel.backend.workflow.service;

import com.comutel.backend.workflow.model.WorkflowOutboxEvent;
import com.comutel.backend.workflow.model.WorkflowOutboxStatus;
import com.comutel.backend.workflow.repository.WorkflowOutboxEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowOutboxProcessorService {

    @Autowired
    private WorkflowOutboxEventRepository outboxRepository;

    @Scheduled(fixedRate = 30000)
    public void processOutbox() {
        List<WorkflowOutboxEvent> pending = outboxRepository
                .findTop100ByStatusAndAvailableAtBeforeOrderByIdAsc(WorkflowOutboxStatus.PENDING, LocalDateTime.now());

        for (WorkflowOutboxEvent event : pending) {
            try {
                // Punto de extension para broker externo (Kafka/Rabbit/etc.)
                System.out.println("WorkflowOutbox publish -> " + event.getEventType() + " aggregate=" + event.getAggregateType() + ":" + event.getAggregateId());

                event.setStatus(WorkflowOutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                event.setStatus(WorkflowOutboxStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setAvailableAt(LocalDateTime.now().plusMinutes(1));
                outboxRepository.save(event);
            }
        }
    }
}
