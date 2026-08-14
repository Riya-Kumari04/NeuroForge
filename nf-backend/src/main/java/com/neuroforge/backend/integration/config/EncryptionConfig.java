package com.neuroforge.backend.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class EncryptionConfig {

    @Value("${github.token.encryption.key}")
    private String encryptionKeyBase64;

    @Bean
    public SecretKey encryptionKey() {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isEmpty()) {
            throw new IllegalStateException("GitHub token encryption key not configured. Set GITHUB_TOKEN_ENCRYPTION_KEY environment variable.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
