package com.acleda.bsonlineshop.notification;

import com.acleda.bsonlineshop.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class OtpMailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromOverride;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public OtpMailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends OTP or throws when mail cannot be delivered — used when
     * {@code app.auth.registration.require-delivered-email=true}.
     */
    public void sendOtpMandatory(String toEmail, String code, String purpose) {
        if (mailSender == null) {
            throw new BusinessException("Mail is not configured; cannot deliver registration OTP.");
        }
        String from = resolveFromAddress();
        if (!StringUtils.hasText(from)) {
            throw new BusinessException(
                    "Email From address missing: set app.mail.from or spring.mail.username for OTP delivery.");
        }
        doSendMail(toEmail, code, purpose, from);
    }

    /**
     * Best-effort email; when SMTP is missing, logs DEBUG and returns — rely on DB OTP or {@code app.otp.demo-code} in dev.
     */
    public void sendOtpEmail(String toEmail, String code, String purpose) {
        if (mailSender == null) {
            log.debug("SMTP not configured (no SPRING_MAIL_HOST); OTP generated for {} but not emailed.", toEmail);
            return;
        }
        String from = resolveFromAddress();
        if (!StringUtils.hasText(from)) {
            throw new BusinessException(
                    "Email From address missing: set app.mail.from or spring.mail.username for OTP delivery.");
        }

        doSendMail(toEmail, code, purpose, from);
    }

    private void doSendMail(String toEmail, String code, String purpose, String from) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subjectForPurpose(purpose));
        message.setText(bodyText(code, purpose));

        try {
            mailSender.send(message);
            log.debug("OTP email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send OTP email to {}", toEmail, ex);
            throw new BusinessException("Unable to send verification email. Please try again later.");
        }
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(fromOverride)) {
            return fromOverride;
        }
        if (StringUtils.hasText(springMailUsername) && springMailUsername.contains("@")) {
            return springMailUsername;
        }
        return "";
    }

    private static String subjectForPurpose(String purpose) {
        if (purpose == null) {
            return "Your verification code";
        }
        return switch (purpose.toUpperCase()) {
            case "PASSWORD_RESET" -> "Reset your Beauty Skin password";
            case "REGISTER_EMAIL" -> "Confirm your Beauty Skin account";
            default -> "Your Beauty Skin verification code";
        };
    }

    private String bodyText(String code, String purpose) {
        String purposeLine = purpose != null ? purpose : "verification";
        return """
                Your verification code is: %s

                This code expires in %d minutes.

                Purpose: %s

                If you did not request this code, you can ignore this email.
                """
                .formatted(code, otpExpirationMinutes, purposeLine);
    }
}
