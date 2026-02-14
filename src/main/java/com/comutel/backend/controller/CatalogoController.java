package com.comutel.backend.controller;

import com.comutel.backend.model.Categoria;
import com.comutel.backend.model.TipoCatalogo;
import com.comutel.backend.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    @GetMapping("/tipos")
    public List<TipoCatalogo> listarTipos(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return catalogoService.listarTipos(incluirInactivos);
    }

    @PostMapping("/tipos")
    public TipoCatalogo crearTipo(@RequestBody Map<String, Object> payload) {
        return catalogoService.crearTipo(payload);
    }

    @PutMapping("/tipos/{id}")
    public TipoCatalogo actualizarTipo(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return catalogoService.actualizarTipo(id, payload);
    }

    @DeleteMapping("/tipos/{id}")
    public Map<String, Object> eliminarTipo(@PathVariable Long id) {
        catalogoService.eliminarTipo(id);
        return Map.of("deleted", true, "id", id);
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias(
            @RequestParam(required = false) String processType,
            @RequestParam(defaultValue = "false") boolean incluirInactivas
    ) {
        return catalogoService.listarCategorias(processType, incluirInactivas);
    }

    @PostMapping("/categorias")
    public Categoria crearCategoria(@RequestBody Map<String, Object> payload) {
        return catalogoService.crearCategoria(payload);
    }

    @PutMapping("/categorias/{id}")
    public Categoria actualizarCategoria(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return catalogoService.actualizarCategoria(id, payload);
    }

    @DeleteMapping("/categorias/{id}")
    public Map<String, Object> eliminarCategoria(@PathVariable Long id) {
        catalogoService.eliminarCategoria(id);
        return Map.of("deleted", true, "id", id);
    }
}
