package com.neuroforge.backend.bug.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendIncidentAlert(
            String to,
            String bugTitle,
            String severity) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Critical Incident Alert");

        message.setText(
                "A critical incident has been created.\n\n"
                        + "Bug: " + bugTitle + "\n"
                        + "Severity: " + severity + "\n\n"
                        + "Please investigate immediately.");

        mailSender.send(message);
    }
}