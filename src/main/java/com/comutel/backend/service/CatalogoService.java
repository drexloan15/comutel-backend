package com.comutel.backend.service;

import com.comutel.backend.model.Categoria;
import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.TipoCatalogo;
import com.comutel.backend.repository.CategoriaRepository;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.repository.TipoCatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CatalogoService {

    @Autowired
    private TipoCatalogoRepository tipoCatalogoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private GrupoResolutorRepository grupoResolutorRepository;

    public List<TipoCatalogo> listarTipos(boolean incluirInactivos) {
        return incluirInactivos
                ? tipoCatalogoRepository.findAllByOrderByNombreAsc()
                : tipoCatalogoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional
    public TipoCatalogo crearTipo(Map<String, Object> payload) {
        TipoCatalogo tipo = new TipoCatalogo();
        tipo.setNombre(requiredString(payload, "nombre"));
        tipo.setDescripcion(optionalString(payload, "descripcion"));
        tipo.setActivo(parseBoolean(payload.get("activo"), true));
        return tipoCatalogoRepository.save(tipo);
    }

    @Transactional
    public TipoCatalogo actualizarTipo(Long tipoId, Map<String, Object> payload) {
        TipoCatalogo tipo = tipoCatalogoRepository.findById(tipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo no encontrado"));
        tipo.setNombre(requiredString(payload, "nombre"));
        tipo.setDescripcion(optionalString(payload, "descripcion"));
        tipo.setActivo(parseBoolean(payload.get("activo"), tipo.isActivo()));
        return tipoCatalogoRepository.save(tipo);
    }

    @Transactional
    public void eliminarTipo(Long tipoId) {
        TipoCatalogo tipo = tipoCatalogoRepository.findById(tipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo no encontrado"));
        tipo.setActivo(false);
        tipoCatalogoRepository.save(tipo);
    }

    public List<Categoria> listarCategorias(String processType, boolean incluirInactivas) {
        if (processType == null || processType.isBlank()) {
            return incluirInactivas
                    ? categoriaRepository.findAllByOrderByNombreAsc()
                    : categoriaRepository.findByActivaTrueOrderByNombreAsc();
        }

        String normalized = processType.trim().toUpperCase(Locale.ROOT);
        return incluirInactivas
                ? categoriaRepository.findByProcessTypeIgnoreCaseOrderByNombreAsc(normalized)
                : categoriaRepository.findByProcessTypeIgnoreCaseAndActivaTrueOrderByNombreAsc(normalized);
    }

    @Transactional
    public Categoria crearCategoria(Map<String, Object> payload) {
        Categoria categoria = new Categoria();
        categoria.setNombre(requiredString(payload, "nombre"));
        categoria.setProcessType(normalizeProcessType(optionalString(payload, "processType")));
        categoria.setActiva(parseBoolean(payload.get("activa"), true));
        categoria.setTipo(resolveTipo(optionalLong(payload, "tipoId")));
        categoria.setGrupoDefault(resolveGrupo(optionalLong(payload, "grupoDefaultId")));
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizarCategoria(Long categoriaId, Map<String, Object> payload) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
        categoria.setNombre(requiredString(payload, "nombre"));
        categoria.setProcessType(normalizeProcessType(optionalString(payload, "processType")));
        categoria.setActiva(parseBoolean(payload.get("activa"), categoria.isActiva()));
        categoria.setTipo(resolveTipo(optionalLong(payload, "tipoId")));
        categoria.setGrupoDefault(resolveGrupo(optionalLong(payload, "grupoDefaultId")));
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminarCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }

    private String requiredString(Map<String, Object> payload, String key) {
        String value = optionalString(payload, key);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " es obligatorio");
        }
        return value.trim();
    }

    private String optionalString(Map<String, Object> payload, String key) {
        Object raw = payload.get(key);
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        if (value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private Long optionalLong(Map<String, Object> payload, String key) {
        Object raw = payload.get(key);
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        if (value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " invalido");
        }
    }

    private boolean parseBoolean(Object raw, boolean fallback) {
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Boolean value) {
            return value;
        }
        String value = String.valueOf(raw);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    private String normalizeProcessType(String processType) {
        return processType == null ? null : processType.trim().toUpperCase(Locale.ROOT);
    }

    private TipoCatalogo resolveTipo(Long tipoId) {
        if (tipoId == null) {
            return null;
        }
        return tipoCatalogoRepository.findById(tipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoId no existe"));
    }

    private GrupoResolutor resolveGrupo(Long grupoId) {
        if (grupoId == null) {
            return null;
        }
        return grupoResolutorRepository.findById(grupoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "grupoDefaultId no existe"));
    }
}
