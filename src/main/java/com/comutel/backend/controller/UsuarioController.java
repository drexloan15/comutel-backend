package com.comutel.backend.controller;

import com.comutel.backend.model.Usuario;
import com.comutel.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // --- TUS MÉTODOS DE AUTENTICACIÓN (EXISTENTES) ---

    // 1. REGISTRO (Encripta contraseña)
    // Se usa tanto para registro público como para el admin
    @PostMapping("/registro")
    public Usuario registrarUsuario(@RequestBody Usuario usuario) {
        return usuarioService.registrarUsuario(usuario);
    }

    // 2. LOGIN (Valida credenciales)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email = credenciales.get("email");
        String password = credenciales.get("password");

        Usuario usuario = usuarioService.login(email, password);

        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }

    // --- NUEVOS MÉTODOS PARA EL PANEL DE ADMIN (FUSIONADOS) ---

    // 3. LISTAR TODOS (Para la tabla de AdminUsers.jsx)
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarTodos();
    }

    // 4. CREAR USUARIO DESDE ADMIN
    // El frontend AdminUsers.jsx envía un POST directo a /api/usuarios
    // Reutilizamos la lógica de 'registrarUsuario' para que encripte la contraseña
    @PostMapping
    public Usuario crearUsuarioDesdeAdmin(@RequestBody Usuario usuario) {
        return usuarioService.registrarUsuario(usuario);
    }

    // 5. ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
    @GetMapping("/reparar-admin/{email}/{nuevaPassword}")
    public Usuario repararAdmin(@PathVariable String email, @PathVariable String nuevaPassword) {
        List<Usuario> usuarios = usuarioService.listarTodos();

        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setRol(Usuario.Rol.ADMIN);      // 1. Te vuelve ADMIN
                u.setPassword(nuevaPassword);     // 2. Pone la nueva contraseña (texto plano)
                return usuarioService.registrarUsuario(u); // 3. La encripta UNA sola vez y guarda
            }
        }
        return null; // Si no encuentra el usuario
    }

}