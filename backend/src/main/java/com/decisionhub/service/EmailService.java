package com.decisionhub.service;

import com.decisionhub.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String frontendUrl;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.frontend-url}") String frontendUrl,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    public void sendPasswordReset(User user, String rawToken) {
        String link = frontendUrl + "/reset-password?token=" + rawToken;
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            // Local development without SMTP. Never rely on this in production.
            log.warn("SMTP not configured. Password reset link for {}: {}", user.getEmail(), link);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(user.getEmail());
        msg.setSubject("DecisionHub password reset");
        msg.setText("Hi " + user.getName() + ",\n\nUse this link to reset your password "
                + "(valid for 30 minutes):\n" + link + "\n\nIf you did not request this, ignore this email.");
        sender.send(msg);
    }
}