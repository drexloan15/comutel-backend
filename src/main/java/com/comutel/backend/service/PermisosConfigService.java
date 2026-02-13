package com.comutel.backend.service;

import com.comutel.backend.model.PermisosConfig;
import com.comutel.backend.repository.PermisosConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PermisosConfigService {

    private static final Long CONFIG_ID = 1L;

    @Autowired
    private PermisosConfigRepository permisosConfigRepository;

    public Map<String, Object> obtenerConfig() {
        PermisosConfig entity = permisosConfigRepository.findById(CONFIG_ID)
                .orElseGet(this::buildDefaultEntity);
        return toMap(entity);
    }

    @Transactional
    public Map<String, Object> guardarConfig(Map<String, Object> payload) {
        PermisosConfig entity = permisosConfigRepository.findById(CONFIG_ID)
                .orElseGet(this::buildDefaultEntity);
        aplicarPayload(entity, payload);
        permisosConfigRepository.save(entity);
        return toMap(entity);
    }

    private PermisosConfig buildDefaultEntity() {
        PermisosConfig entity = new PermisosConfig();
        entity.setId(CONFIG_ID);
        entity.setRolCliente(false);
        entity.setRolTecnico(false);
        entity.setRolAdmin(true);
        entity.setRolTesterAdmin(true);
        entity.setUsuarios(new HashMap<>());
        entity.setGrupos(new HashMap<>());
        return entity;
    }

    @SuppressWarnings("unchecked")
    private void aplicarPayload(PermisosConfig entity, Map<String, Object> payload) {
        if (payload == null) {
            return;
        }

        Object rolesObj = payload.get("roles");
        if (rolesObj instanceof Map<?, ?> rolesMap) {
            entity.setRolCliente(toBoolean(rolesMap.get("CLIENTE"), entity.isRolCliente()));
            entity.setRolTecnico(toBoolean(rolesMap.get("TECNICO"), entity.isRolTecnico()));
            entity.setRolAdmin(toBoolean(rolesMap.get("ADMIN"), entity.isRolAdmin()));
            entity.setRolTesterAdmin(toBoolean(rolesMap.get("TESTERADMIN"), entity.isRolTesterAdmin()));
        }

        Object usuariosObj = payload.get("usuarios");
        if (usuariosObj instanceof Map<?, ?> usuariosMap) {
            Map<String, Boolean> usuarios = new HashMap<>();
            for (Map.Entry<?, ?> entry : usuariosMap.entrySet()) {
                if (entry.getKey() == null) continue;
                Boolean parsed = parseBoolean(entry.getValue());
                if (parsed != null) {
                    usuarios.put(String.valueOf(entry.getKey()), parsed);
                }
            }
            entity.setUsuarios(usuarios);
        }

        Object gruposObj = payload.get("grupos");
        if (gruposObj instanceof Map<?, ?> gruposMap) {
            Map<String, Boolean> grupos = new HashMap<>();
            for (Map.Entry<?, ?> entry : gruposMap.entrySet()) {
                if (entry.getKey() == null) continue;
                Boolean parsed = parseBoolean(entry.getValue());
                if (parsed != null) {
                    grupos.put(String.valueOf(entry.getKey()), parsed);
                }
            }
            entity.setGrupos(grupos);
        }
    }

    private Map<String, Object> toMap(PermisosConfig entity) {
        Map<String, Boolean> roles = new LinkedHashMap<>();
        roles.put("CLIENTE", entity.isRolCliente());
        roles.put("TECNICO", entity.isRolTecnico());
        roles.put("ADMIN", entity.isRolAdmin());
        roles.put("TESTERADMIN", entity.isRolTesterAdmin());

        Map<String, Object> config = new HashMap<>();
        config.put("roles", roles);
        config.put("usuarios", entity.getUsuarios() != null ? entity.getUsuarios() : new HashMap<String, Boolean>());
        config.put("grupos", entity.getGrupos() != null ? entity.getGrupos() : new HashMap<String, Boolean>());
        return config;
    }

    private boolean toBoolean(Object raw, boolean fallback) {
        Boolean parsed = parseBoolean(raw);
        return parsed != null ? parsed : fallback;
    }

    private Boolean parseBoolean(Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        if (raw instanceof String s) {
            if ("true".equalsIgnoreCase(s)) return true;
            if ("false".equalsIgnoreCase(s)) return false;
        }
        return null;
    }
}
