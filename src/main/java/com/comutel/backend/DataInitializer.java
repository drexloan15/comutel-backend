package com.comutel.backend;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.comutel.backend.repository.GrupoResolutorRepository grupoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos si la tabla está vacía (para no duplicar cada vez que reinicies)
        if (usuarioRepository.count() == 0) {

            System.out.println("⚡ Inicializando datos maestros...");

            // 1. Crear al TÉCNICO (Tu usuario)
            Usuario tecnico = new Usuario();
            tecnico.setNombre("Jean Puccio");
            tecnico.setEmail("jean.puccio@comutelperu.com");
            tecnico.setRol(Usuario.Rol.TECNICO);
            // ¡AQUÍ ESTÁ EL TRUCO! Encriptamos la contraseña antes de guardar
            tecnico.setPassword(passwordEncoder.encode("Comutel.2026"));

            usuarioRepository.save(tecnico);
            System.out.println("✅ Usuario Técnico creado: " + tecnico.getEmail());

            // 2. Crear al CLIENTE (Fabio)
            Usuario cliente = new Usuario();
            cliente.setNombre("Weslye Puccio");
            cliente.setEmail("wespuccio1279@gmail.com");
            cliente.setRol(Usuario.Rol.CLIENTE);
            cliente.setPassword(passwordEncoder.encode("Comutel.2026"));

            usuarioRepository.save(cliente);
            System.out.println("✅ Usuario Cliente creado: " + cliente.getEmail());

        } else {
            System.out.println("✅ La base de datos ya tiene usuarios. No se requiere inicialización.");
        }
        if (grupoRepository.count() == 0) {
            System.out.println("⚡ Creando Grupos de Soporte...");
            grupoRepository.save(new GrupoResolutor("Mesa de Ayuda (N1)", "Primer nivel de atención"));
            grupoRepository.save(new GrupoResolutor("Redes y Comunicaciones", "Problemas de internet y VPN"));
            grupoRepository.save(new GrupoResolutor("Soporte en Campo", "Técnicos presenciales"));
            grupoRepository.save(new GrupoResolutor("Desarrollo", "Bugs y errores de software"));
        }
    }
}