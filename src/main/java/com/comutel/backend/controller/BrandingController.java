package com.comutel.backend.controller;

import com.comutel.backend.service.BrandingConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/branding")
public class BrandingController {

    @Autowired
    private BrandingConfigService brandingConfigService;

    @GetMapping("/config")
    public Map<String, Object> obtenerConfig() {
        return brandingConfigService.obtenerConfig();
    }

    @PutMapping("/config")
    public Map<String, Object> actualizarConfig(@RequestBody(required = false) Map<String, Object> payload) {
        return brandingConfigService.actualizarConfig(payload);
    }
}
