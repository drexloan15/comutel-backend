package com.comutel.backend.controller;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupos")

public class GrupoController {

    @Autowired
    private GrupoResolutorRepository grupoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<GrupoResolutor> listarGrupos() {
        return grupoRepository.findAll();
    }

    // --- AGREGAR ESTO ðŸ‘‡ ---
    @PostMapping
    public GrupoResolutor crearGrupo(@RequestBody GrupoResolutor grupo) {
        return grupoRepository.save(grupo);
    }

    // --- AGREGAR ESTO PARA QUE FUNCIONE EL BOTÃ“N BORRAR ðŸ‘‡ ---
    @DeleteMapping("/{id}")
    public void eliminarGrupo(@PathVariable Long id) {
        grupoRepository.deleteById(id);
    }

    @GetMapping("/{id}/usuarios")
    public List<Usuario> listarUsuariosDelGrupo(@PathVariable Long id) {
        return usuarioService.listarUsuariosPorGrupo(id);
    }

    @PutMapping("/{id}/usuarios")
    public List<Usuario> asignarUsuariosDelGrupo(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object usuariosObj = payload.get("usuarioIds");
        List<Long> usuarioIds = List.of();

        if (usuariosObj instanceof List<?> lista) {
            usuarioIds = lista.stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        return usuarioService.asignarUsuariosAGrupo(id, usuarioIds);
    }
}

