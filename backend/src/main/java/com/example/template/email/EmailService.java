package com.example.template.email;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public EmailService(@Value("${app.resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

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
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(params);
            log.debug("Sent '{}' to {}", subject, to);
        } catch (ResendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
