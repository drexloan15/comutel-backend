package com.comutel.backend;

import com.comutel.backend.model.GrupoResolutor;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.comutel.backend.repository.GrupoResolutorRepository grupoRepository;

    @Value("${app.default-password:change_me}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {

            System.out.println("Inicializando datos maestros...");

            Usuario tecnico = new Usuario();
            tecnico.setNombre("Jean Puccio");
            tecnico.setEmail("jean.puccio@comutelperu.com");
            tecnico.setRol(Usuario.Rol.TECNICO);
            tecnico.setPassword(passwordEncoder.encode(defaultPassword));

            usuarioRepository.save(tecnico);
            System.out.println("Usuario Tecnico creado: " + tecnico.getEmail());

            Usuario cliente = new Usuario();
            cliente.setNombre("Weslye Puccio");
            cliente.setEmail("wespuccio1279@gmail.com");
            cliente.setRol(Usuario.Rol.CLIENTE);
            cliente.setPassword(passwordEncoder.encode(defaultPassword));

            usuarioRepository.save(cliente);
            System.out.println("Usuario Cliente creado: " + cliente.getEmail());

        } else {
            System.out.println("La base de datos ya tiene usuarios. No se requiere inicializacion.");
        }

        if (grupoRepository.count() == 0) {
            System.out.println("Creando Grupos de Soporte...");
            grupoRepository.save(new GrupoResolutor("Mesa de Ayuda (N1)", "Primer nivel de atencion"));
            grupoRepository.save(new GrupoResolutor("Redes y Comunicaciones", "Problemas de internet y VPN"));
            grupoRepository.save(new GrupoResolutor("Soporte en Campo", "Tecnicos presenciales"));
            grupoRepository.save(new GrupoResolutor("Desarrollo", "Bugs y errores de software"));
        }
    }
}
