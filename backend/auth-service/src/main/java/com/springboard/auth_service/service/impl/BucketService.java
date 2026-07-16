package com.springboard.auth_service.service.impl;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();


    public Bucket resolveBucket(String key,Bandwidth bandwidth){

        return cache.computeIfAbsent(key,
                k -> Bucket.builder()
                        .addLimit(bandwidth)
                        .build());

    }
}
