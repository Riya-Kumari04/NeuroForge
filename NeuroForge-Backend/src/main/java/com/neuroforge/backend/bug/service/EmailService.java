package com.neuroforge.backend.bug.service;

public interface EmailService {

    void sendIncidentAlert(
            String to,
            String bugTitle,
            String severity);
}