package com.comutel.backend.service;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.GrupoResolutorRepository;
import com.comutel.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GrupoResolutorRepository grupoResolutorRepository;

    public Usuario registrarUsuario(Usuario usuario) {
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);
        return usuarioRepository.save(usuario);
    }

    public Usuario login(String emailOrUser, String passwordRaw) {
        Optional<Usuario> usuarioOpt = buscarUsuarioPorLogin(emailOrUser);
        if (usuarioOpt.isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        String passwordGuardada = usuario.getPassword();

        // Compatibilidad con passwords legacy sin hash.
        if (passwordGuardada != null && passwordGuardada.equals(passwordRaw)) {
            usuario.setPassword(passwordEncoder.encode(passwordRaw));
            usuarioRepository.save(usuario);
            return usuario;
        }

        try {
            if (passwordEncoder.matches(passwordRaw, passwordGuardada)) {
                return usuario;
            }
        } catch (IllegalArgumentException ignored) {
            // Password guardada con formato invalido para BCrypt.
        }

        return null;
    }

    private Optional<Usuario> buscarUsuarioPorLogin(String emailOrUser) {
        String login = emailOrUser == null ? "" : emailOrUser.trim();
        if (login.isEmpty()) {
            return Optional.empty();
        }

        if (login.contains("@")) {
            return usuarioRepository.findByEmailIgnoreCase(login);
        }

        Optional<Usuario> porDominioCorporativo = usuarioRepository
                .findByEmailIgnoreCase(login + "@comutelperu.com");
        if (porDominioCorporativo.isPresent()) {
            return porDominioCorporativo;
        }

        return usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail() != null)
                .filter(u -> {
                    String[] partes = u.getEmail().split("@", 2);
                    return partes.length > 0 && partes[0].equalsIgnoreCase(login);
                })
                .findFirst();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public java.util.List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarTecnicosPorGrupo(Long grupoId) {
        if (grupoId == null) {
            return usuarioRepository.findByRol(Usuario.Rol.TECNICO);
        }
        return usuarioRepository.findByRolAndGruposId(Usuario.Rol.TECNICO, grupoId);
    }

    @Transactional
    public Usuario asignarGrupos(Long usuarioId, List<Long> grupoIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() != Usuario.Rol.TECNICO) {
            throw new RuntimeException("Solo se pueden asignar grupos a usuarios con rol TECNICO");
        }

        Set<GrupoResolutor> grupos = new HashSet<>();
        if (grupoIds != null && !grupoIds.isEmpty()) {
            grupos.addAll(grupoResolutorRepository.findAllById(grupoIds));
            if (grupos.size() != grupoIds.size()) {
                throw new RuntimeException("Uno o mas grupos no existen");
            }
        }

        usuario.setGrupos(grupos);
        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
