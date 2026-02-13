package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowSlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowSlaPolicyRepository extends JpaRepository<WorkflowSlaPolicy, Long> {
    Optional<WorkflowSlaPolicy> findByName(String name);
}
