package com.neuroforge.backend.specification.service;

import com.neuroforge.backend.specification.dto.response.GenerateSpecificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
public class HallucinationDetectionService {

    // Configurable list of forbidden technologies/terms organized by category
    
    // Technical assumptions
    private static final Set<String> TECHNICAL_TERMS = new HashSet<>(Arrays.asList(
            "database",
            "API",
            "framework",
            "microservice",
            "cloud",
            "server",
            "backend",
            "frontend",
            "MongoDB",
            "PostgreSQL",
            "MySQL",
            "Oracle",
            "Redis",
            "REST",
            "GraphQL",
            "gRPC",
            "WebSockets",
            "Spring Boot",
            "Django",
            "Flask",
            "Node.js",
            "React",
            "Angular",
            "Vue",
            "Docker",
            "Kubernetes"
    ));
    
    // Platform assumptions
    private static final Set<String> PLATFORM_TERMS = new HashSet<>(Arrays.asList(
            "web application",
            "mobile application",
            "browser",
            "iOS",
            "Android"
    ));
    
    // Process assumptions
    private static final Set<String> PROCESS_TERMS = new HashSet<>(Arrays.asList(
            "background service",
            "cron job",
            "batch processing"
    ));
    
    // Security assumptions
    private static final Set<String> SECURITY_TERMS = new HashSet<>(Arrays.asList(
            "encryption",
            "AES",
            "AES-256",
            "PCI",
            "PCI-DSS",
            "authentication",
            "JWT",
            "OAuth",
            "SAML",
            "LDAP"
    ));
    
    // Location assumptions
    private static final Set<String> LOCATION_TERMS = new HashSet<>(Arrays.asList(
            "GPS",
            "Google Maps",
            "Google Maps API",
            "geolocation"
    ));
    
    // Notification assumptions
    private static final Set<String> NOTIFICATION_TERMS = new HashSet<>(Arrays.asList(
            "SMS",
            "Email",
            "QR Code",
            "Twilio",
            "SendGrid",
            "push notification"
    ));
    
    // Payment assumptions
    private static final Set<String> PAYMENT_TERMS = new HashSet<>(Arrays.asList(
            "UPI",
            "Wallet",
            "Stripe",
            "Razorpay",
            "PayPal",
            "payment gateway"
    ));
    
    // Cloud/Infrastructure assumptions
    private static final Set<String> INFRASTRUCTURE_TERMS = new HashSet<>(Arrays.asList(
            "AWS",
            "Azure",
            "Google Cloud",
            "Firebase",
            "serverless"
    ));
    
    // Combined forbidden terms for backward compatibility
    private static final Set<String> FORBIDDEN_TERMS = new HashSet<>();
    
    static {
        FORBIDDEN_TERMS.addAll(TECHNICAL_TERMS);
        FORBIDDEN_TERMS.addAll(PLATFORM_TERMS);
        FORBIDDEN_TERMS.addAll(PROCESS_TERMS);
        FORBIDDEN_TERMS.addAll(SECURITY_TERMS);
        FORBIDDEN_TERMS.addAll(LOCATION_TERMS);
        FORBIDDEN_TERMS.addAll(NOTIFICATION_TERMS);
        FORBIDDEN_TERMS.addAll(PAYMENT_TERMS);
        FORBIDDEN_TERMS.addAll(INFRASTRUCTURE_TERMS);
    }

    /**
     * Detects if the generated specification contains forbidden technologies
     * that were not mentioned in the original user requirement
     */
    public HallucinationResult detectHallucination(String userRequirement, GenerateSpecificationResponse response) {
        log.debug("Starting hallucination detection");
        
        Set<String> detectedTerms = new HashSet<>();
        
        // Check all text fields for forbidden terms
        checkTextForForbiddenTerms(userRequirement, response.getDescription(), detectedTerms);
        checkArrayForForbiddenTerms(userRequirement, response.getUserStories(), detectedTerms);
        checkArrayForForbiddenTerms(userRequirement, response.getAcceptanceCriteria(), detectedTerms);
        checkArrayForForbiddenTerms(userRequirement, response.getFunctionalRequirements(), detectedTerms);
        checkArrayForForbiddenTerms(userRequirement, response.getNonFunctionalRequirements(), detectedTerms);
        
        boolean hasHallucination = !detectedTerms.isEmpty();
        
        if (hasHallucination) {
            log.warn("Hallucination detected. Forbidden terms found: {}", detectedTerms);
        } else {
            log.debug("No hallucination detected");
        }
        
        return new HallucinationResult(hasHallucination, detectedTerms);
    }

    /**
     * Checks a text field for forbidden terms
     */
    private void checkTextForForbiddenTerms(String userRequirement, String text, Set<String> detectedTerms) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        for (String forbiddenTerm : FORBIDDEN_TERMS) {
            // Only flag if the term appears in the response but NOT in the user requirement
            if (containsTerm(text, forbiddenTerm) && !containsTerm(userRequirement, forbiddenTerm)) {
                detectedTerms.add(forbiddenTerm);
            }
        }
    }

    /**
     * Checks an array of strings for forbidden terms
     */
    private void checkArrayForForbiddenTerms(String userRequirement, List<String> items, Set<String> detectedTerms) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        for (String item : items) {
            checkTextForForbiddenTerms(userRequirement, item, detectedTerms);
        }
    }

    /**
     * Case-insensitive check if a term is present in text
     * Uses word boundaries to avoid false positives from substrings
     */
    private boolean containsTerm(String text, String term) {
        // Use word boundaries to avoid matching substrings
        // Example: "email" should not match "emailing" as a standalone word
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(term) + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    /**
     * Result class for hallucination detection
     */
    public static class HallucinationResult {
        private final boolean hasHallucination;
        private final Set<String> detectedTerms;

        public HallucinationResult(boolean hasHallucination, Set<String> detectedTerms) {
            this.hasHallucination = hasHallucination;
            this.detectedTerms = detectedTerms;
        }

        public boolean hasHallucination() {
            return hasHallucination;
        }

        public Set<String> getDetectedTerms() {
            return detectedTerms;
        }
    }
}
