package com.comutel.backend.service;

import com.comutel.backend.model.BrandingConfig;
import com.comutel.backend.repository.BrandingConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BrandingConfigService {

    @Autowired
    private BrandingConfigRepository brandingConfigRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> obtenerConfig() {
        BrandingConfig config = getOrCreateEntity();
        return parseJson(config.getConfigJson());
    }

    @Transactional
    public Map<String, Object> actualizarConfig(Map<String, Object> payload) {
        BrandingConfig config = getOrCreateEntity();
        Map<String, Object> normalized = payload == null ? new LinkedHashMap<>() : payload;
        try {
            config.setConfigJson(objectMapper.writeValueAsString(normalized));
            config.setUpdatedAt(LocalDateTime.now());
            brandingConfigRepository.save(config);
            return normalized;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo serializar la configuracion de branding");
        }
    }

    private BrandingConfig getOrCreateEntity() {
        return brandingConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    BrandingConfig created = new BrandingConfig();
                    created.setConfigJson("{}");
                    created.setUpdatedAt(LocalDateTime.now());
                    return brandingConfigRepository.save(created);
                });
    }

    private Map<String, Object> parseJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La configuracion de branding almacenada es invalida");
        }
    }
}
