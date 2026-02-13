package com.comutel.backend.service;

import com.comutel.backend.model.Ticket;
import com.comutel.backend.model.Usuario;
import com.comutel.backend.repository.TicketRepository;
import com.comutel.backend.repository.UsuarioRepository;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailTicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${app.imap.host}")
    private String imapHost;

    @Value("${app.imap.port}")
    private String imapPort;

    @Value("${app.imap.user}")
    private String imapUser;

    @Value("${app.imap.password}")
    private String imapPassword;

    @Scheduled(fixedRate = 120000)
    public void revisarCorreo() {
        System.out.println("Robot: Revisando bandeja de entrada...");

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", imapPort);
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.starttls.enable", "true");

        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect(imapHost, imapUser, imapPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : messages) {
                crearTicketDesdeCorreo(message);
                message.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void crearTicketDesdeCorreo(Message message) throws Exception {
        String titulo = message.getSubject();
        String remitente = ((InternetAddress) message.getFrom()[0]).getAddress();

        Usuario usuarioCliente = usuarioRepository.findByEmail(remitente).orElse(null);

        if (usuarioCliente == null) {
            System.out.println("ALERTA: Correo rechazado de " + remitente + " (No es usuario registrado).");
            return;
        }

        System.out.println("Usuario verificado: " + usuarioCliente.getNombre());

        String descripcionSucia = "Enviado por email";
        if (message.getContent() instanceof String) {
            descripcionSucia = (String) message.getContent();
        } else if (message.getContent() instanceof Multipart multipart) {
            BodyPart part = multipart.getBodyPart(0);
            descripcionSucia = part.getContent().toString();
        }

        String descripcionLimpia = descripcionSucia.replaceAll("\\<.*?\\>", "");
        descripcionLimpia = limpiarCuerpoCorreo(descripcionLimpia);

        Ticket ticket = new Ticket();
        ticket.setTitulo(titulo);
        ticket.setDescripcion(descripcionLimpia);
        ticket.setPrioridad(Ticket.Prioridad.MEDIA);
        ticket.calcularVencimiento();
        ticket.setEstado(Ticket.Estado.NUEVO);
        ticket.setUsuario(usuarioCliente);
        ticket.setTecnico(null);

        ticketRepository.save(ticket);
        System.out.println("Ticket creado exitosamente para: " + usuarioCliente.getNombre());
    }

    private String limpiarCuerpoCorreo(String texto) {
        if (texto == null) return "";

        String[] separadores = {
                "--",
                "Saludos",
                "Cordialmente",
                "Atentamente",
                "Aviso Legal",
                "De:",
                "From:"
        };

        for (String separador : separadores) {
            if (texto.contains(separador)) {
                texto = texto.substring(0, texto.indexOf(separador));
            }
        }

        return texto.trim();
    }
}
