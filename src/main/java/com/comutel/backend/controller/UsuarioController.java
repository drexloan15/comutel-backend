package com.comutel.backend.controller;

import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.UsuarioRepository;
import com.comutel.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email = credenciales.getOrDefault("email", "").trim();
        String password = credenciales.getOrDefault("password", "");

        Usuario usuario = usuarioService.login(email, password);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }

        return ResponseEntity.status(401).body("Credenciales invalidas");
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        if (usuario.getRol() == null) {
            usuario.setRol(Usuario.Rol.CLIENTE);
        }
        return usuarioService.registrarUsuario(usuario);
    }

    @GetMapping("/tecnicos")
    public List<Usuario> listarTecnicos(@RequestParam(required = false) Long grupoId) {
        return usuarioService.listarTecnicosPorGrupo(grupoId);
    }

    @GetMapping("/grupos/{grupoId}")
    public List<Usuario> listarUsuariosDeGrupo(@PathVariable Long grupoId) {
        return usuarioService.listarUsuariosPorGrupo(grupoId);
    }

    @PutMapping("/{id}/grupos")
    public Usuario asignarGrupos(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Object gruposObj = payload.get("grupoIds");
        List<Long> grupoIds = List.of();

        if (gruposObj instanceof List<?> lista) {
            grupoIds = lista.stream()
                    .map(Object::toString)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        return usuarioService.asignarGrupos(id, grupoIds);
    }

    @PutMapping("/{id}/rol")
    public Usuario actualizarRol(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String rol = payload.getOrDefault("rol", "").trim();
        if (rol.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "rol es obligatorio");
        }

        try {
            Usuario.Rol nuevoRol = Usuario.Rol.valueOf(rol.toUpperCase());
            return usuarioService.actualizarRol(id, nuevoRol);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "rol invalido");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se puede eliminar: El usuario tiene tickets asociados.");
        }
    }

    @GetMapping("/reparar-admin/{email}/{password}")
    public Usuario repararAdmin(@PathVariable String email, @PathVariable String password) {
        return repararAdminInternal(email, password);
    }

    @PostMapping("/reparar-admin")
    public ResponseEntity<?> repararAdminSeguro(@RequestBody Map<String, String> request) {
        String email = request.getOrDefault("email", "").trim();
        String password = request.getOrDefault("password", "");

        if (email.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("email y password son obligatorios");
        }

        Usuario usuario = repararAdminInternal(email, password);
        return ResponseEntity.status(HttpStatus.OK).body(usuario);
    }

    private Usuario repararAdminInternal(String email, String password) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(email);
        Usuario u = existente.orElse(new Usuario());
        u.setEmail(email);
        u.setNombre("Super Admin");
        u.setPassword(password);
        u.setRol(Usuario.Rol.ADMIN);
        return usuarioService.registrarUsuario(u);
    }
}

