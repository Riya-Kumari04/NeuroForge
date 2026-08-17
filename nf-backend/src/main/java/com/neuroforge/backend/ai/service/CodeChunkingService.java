package com.neuroforge.backend.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeChunkingService {

    // Frontend now handles chunking to avoid JVM memory issues
    // Backend only processes individual chunks
    
    public List<String> chunkCode(String sourceCode) {
        // No-op - frontend handles chunking
        List<String> chunks = new ArrayList<>();
        if (sourceCode != null && !sourceCode.isEmpty()) {
            chunks.add(sourceCode);
        }
        return chunks;
    }

    public boolean requiresChunking(String sourceCode) {
        // Frontend determines chunking, backend always processes as single chunk
        return false;
    }
}
