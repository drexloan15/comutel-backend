package com.comutel.backend.controller;

import com.comutel.backend.workflow.engine.dto.TransitionOption;
import com.comutel.backend.workflow.engine.dto.TransitionResult;
import com.comutel.backend.workflow.service.WorkflowRuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows/runtime")
public class WorkflowRuntimeController {

    @Autowired
    private WorkflowRuntimeService workflowRuntimeService;

    @GetMapping("/instances/entity/{entityType}/{entityId}")
    public Map<String, Object> obtenerPorEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return workflowRuntimeService.obtenerPorEntityView(entityType, entityId);
    }

    @GetMapping("/instances/{instanceId}/transitions")
    public List<TransitionOption> obtenerTransicionesDisponibles(@PathVariable Long instanceId, @RequestParam(required = false) Long actorId) {
        return workflowRuntimeService.obtenerTransicionesDisponibles(instanceId, actorId);
    }

    @PostMapping("/instances/{instanceId}/transitions/{eventKey}")
    public TransitionResult ejecutarTransicion(
            @PathVariable Long instanceId,
            @PathVariable String eventKey,
            @RequestParam(required = false) Long actorId,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        return workflowRuntimeService.ejecutarTransicion(instanceId, eventKey, actorId, payload == null ? Map.of() : payload);
    }

    @GetMapping("/instances/{instanceId}/log")
    public List<Map<String, Object>> obtenerLog(@PathVariable Long instanceId) {
        return workflowRuntimeService.obtenerLogView(instanceId);
    }

    @GetMapping("/instances/{instanceId}/tasks")
    public List<Map<String, Object>> obtenerTareas(@PathVariable Long instanceId) {
        return workflowRuntimeService.obtenerTareasView(instanceId);
    }
}
