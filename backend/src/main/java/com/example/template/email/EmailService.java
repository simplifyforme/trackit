package com.example.template.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    /**
     * Sends confirmation email asynchronously so the register endpoint returns immediately.
     * Note: @Async runs after the calling method returns, which in practice means the DB
     * transaction has already committed — the token is safe to use when the user clicks the link.
     */
    @Async
    public void sendVerificationEmail(String to, String token) {
        String link = frontendBaseUrl + "/confirm?token=" + token;
        send(to, "Confirm your email address", """
                <html><body>
                <h2>Welcome!</h2>
                <p>Click the link below to confirm your email address:</p>
                <p><a href="%s">Confirm Email</a></p>
                <p>This link expires in 24 hours.</p>
                <p>If you didn't create an account, you can safely ignore this email.</p>
                </body></html>
                """.formatted(link));
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        send(to, "Reset your password", """
                <html><body>
                <h2>Password Reset</h2>
                <p>Click the link below to set a new password:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link expires in 1 hour.</p>
                <p>If you didn't request a password reset, you can safely ignore this email.</p>
                </body></html>
                """.formatted(link));
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.debug("Sent '{}' to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
