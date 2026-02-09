package com.comutel.backend.controller;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.repository.GrupoResolutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
@CrossOrigin(origins = "http://localhost:5173") // Permite conexión con React
public class GrupoController {

    @Autowired
    private GrupoResolutorRepository grupoRepository;

    @GetMapping
    public List<GrupoResolutor> listarGrupos() {
        return grupoRepository.findAll();
    }

    // --- AGREGAR ESTO 👇 ---
    @PostMapping
    public GrupoResolutor crearGrupo(@RequestBody GrupoResolutor grupo) {
        return grupoRepository.save(grupo);
    }

    // --- AGREGAR ESTO PARA QUE FUNCIONE EL BOTÓN BORRAR 👇 ---
    @DeleteMapping("/{id}")
    public void eliminarGrupo(@PathVariable Long id) {
        grupoRepository.deleteById(id);
    }
}