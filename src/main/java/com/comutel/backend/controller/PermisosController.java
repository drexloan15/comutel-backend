package com.comutel.backend.controller;

import com.comutel.backend.service.PermisosConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/permisos")
public class PermisosController {

    @Autowired
    private PermisosConfigService permisosConfigService;

    @GetMapping("/config")
    public Map<String, Object> obtenerConfig() {
        return permisosConfigService.obtenerConfig();
    }

    @PutMapping("/config")
    public Map<String, Object> actualizarConfig(@RequestBody Map<String, Object> payload) {
        return permisosConfigService.guardarConfig(payload);
    }
}
