package com.comutel.backend.service;

import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // <--- Nuestra herramienta nueva

    public Usuario registrarUsuario(Usuario usuario) {
        // ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String passwordRaw) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // COMPARAR: (Contraseña que escribió, Contraseña encriptada en BD)
            if (passwordEncoder.matches(passwordRaw, usuario.getPassword())) {
                return usuario; // Login exitoso
            }
        }
        return null; // Login fallido
    }

    // Método auxiliar para buscar por ID (necesario para otros services)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }
    public java.util.List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // 2. Eliminar usuario
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}