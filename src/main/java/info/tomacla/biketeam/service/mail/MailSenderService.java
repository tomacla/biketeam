package info.tomacla.biketeam.service.mail;

import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Set;

@Service
public class MailSenderService {

    protected static final Logger log = LoggerFactory.getLogger(MailSenderService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username:undefined}")
    private String fromEmail;

    public void sendDirectly(Team team, Set<String> tos, String subject, String message, ImageDescriptor embedImage) {
        tos.forEach(to -> this.send(team, Set.of(to), null, null, subject, message, embedImage));
    }

    public void sendHiddenly(Team team, Set<String> tos, String subject, String message, ImageDescriptor embedImage) {
        this.send(team, null, null, tos, subject, message, embedImage);
    }

    private void send(Team team, Set<String> tos, Set<String> ccs, Set<String> bccs, String subject, String message, ImageDescriptor embedImage) {

        if (!isSmtpConfigured()) {
            return;
        }

        try {

            log.info("Sending mail to {}, cc {}, bcc {} : [{}]", tos, ccs, bccs, subject);

            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            mimeMessage.setFrom(fromEmail);

            if (tos != null && !tos.isEmpty()) {
                helper.setTo(Arrays.copyOf(tos.toArray(), tos.size(), String[].class));
            } else {
                helper.setTo(fromEmail);
            }

            if (ccs != null) {
                helper.setCc(Arrays.copyOf(ccs.toArray(), ccs.size(), String[].class));
            }

            if (bccs != null) {
                helper.setBcc(Arrays.copyOf(bccs.toArray(), bccs.size(), String[].class));
            }

            helper.setSubject(subject + " - " + team.getName());
            helper.setText(message, true);

            if (embedImage != null) {
                helper.addInline("Image", embedImage.getPath().toFile());
            }

            emailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("Error while sending email : " + e.getMessage(), e);
            throw new RuntimeException("Unable to send email", e);
        }

    }

    public boolean isSmtpConfigured() {
        return emailSender != null && !this.fromEmail.equals("undefined");
    }

}
