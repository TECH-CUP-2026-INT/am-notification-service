package co.edu.escuelaing.techcup.notifications.email;

public interface EmailSenderPort {

    void send(String to, String subject, String htmlBody);
}
