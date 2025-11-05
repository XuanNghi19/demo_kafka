package com.qkdt.demo_kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class StreamService {

    private static final Logger log = LoggerFactory.getLogger(StreamService.class);

    private final WebClient webClient;

    record AiRequest(String jobId, String prompt) {}
    record AiStopRequest(String jobId) {}

    public Flux<String> startPythonStream(String jobId, String prompt) {
        log.info("Call python with jobId {}", jobId);
        AiRequest request = new AiRequest(jobId, prompt);

        return webClient.post()
                .uri("/api/v1/generate/stream")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(dataBuffer -> {
                    String token = dataBuffer.toString(StandardCharsets.UTF_8);
                    DataBufferUtils.release(dataBuffer);
                    return token;
                })
                .doOnNext(token -> log.info("Call python: claim token '{}' ", token.trim()))
                .doOnError(err -> log.info("Call python: stream error '{}' ", err.getMessage()));

    }

    public Mono<Void> stopPythonStream(String jobId) {
        log.info("send /stop signal to python {}", jobId);
        AiStopRequest stopRequest = new AiStopRequest(jobId);

        return webClient.post()
                .uri("/api/v1/generate/stop")
                .bodyValue(stopRequest)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
