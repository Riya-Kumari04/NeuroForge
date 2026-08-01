package com.neuroforge.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildReviewPrompt(String language, String sourceCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced software code reviewer.\n");
        sb.append("Review only the submitted source code.\n\n");
        sb.append("Evaluate the code across the following criteria:\n");
        sb.append("1. Correctness\n");
        sb.append("2. Security\n");
        sb.append("3. Performance\n");
        sb.append("4. Readability\n");
        sb.append("5. Maintainability\n\n");
        sb.append("Return ONLY valid JSON. Do not include markdown. Do not include explanations outside JSON.\n\n");
        sb.append("The required JSON schema is:\n");
        sb.append("{\n");
        sb.append("  \"overallScore\": integer,\n");
        sb.append("  \"summary\": \"string\",\n");
        sb.append("  \"issues\": [\n");
        sb.append("    {\n");
        sb.append("      \"line\": integer,\n");
        sb.append("      \"severity\": \"HIGH | MEDIUM | LOW | INFO\",\n");
        sb.append("      \"category\": \"string\",\n");
        sb.append("      \"description\": \"string\",\n");
        sb.append("      \"suggestion\": \"string\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("Programming Language: ").append(language).append("\n\n");
        sb.append("Source Code:\n");
        sb.append(sourceCode);

        return sb.toString();
    }
}
