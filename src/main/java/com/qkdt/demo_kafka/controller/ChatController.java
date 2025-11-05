package com.qkdt.demo_kafka.controller;

import com.qkdt.demo_kafka.manager.ActiveStreamManager;
import com.qkdt.demo_kafka.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ActiveStreamManager activeStreamManager;
    private final StreamService streamService;

    record ChatRequest(String prompt, String conversationId) {
    }

    record ApiResponse(boolean ok, int code, String message, Object data) {
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest chatRequest) {
        String publicStreamId = activeStreamManager.registerNewStream();
        String privateJobId = activeStreamManager.getJobId(publicStreamId);

        String metadataEvent = String.format(
                "event: metadata\n" +
                        "data: {\"conversationId\" : \"%s\", \"streamId\": \"%s\"\n\n",
                chatRequest.conversationId,
                publicStreamId
        );

        Flux<String> pythonStream = streamService
                .startPythonStream(privateJobId, chatRequest.prompt)
                .map(token -> "data: " + token.replace("\n", "\ndata: ") + "\n\n")
                .doOnTerminate(() -> {
                    log.info("FE call BE: Stream {} kết thúc, dọn dẹp state", publicStreamId);
                    activeStreamManager.removeStream(publicStreamId);
                })
                .doOnCancel(() -> {
                    log.warn("FE call BE: FE huy ket noi stream {}, gui tin hieu /stop den python", publicStreamId);
                    streamService.stopPythonStream(privateJobId).subscribe();
                });

        return Flux.just(metadataEvent).concatWith(pythonStream);

    }

    @PostMapping("/stop")
    public Mono<ResponseEntity<ApiResponse>> stopStream(@RequestBody Map<String, String> request) {
        String publicStreamId = request.get("streamId");
        log.info("FE call BE claimed /stop for streamId {}", publicStreamId);

        String privateJobId = activeStreamManager.getJobId(publicStreamId);

        if (privateJobId == null) {
            log.info("FE call BE cannot find streamId {}", publicStreamId);
            ApiResponse response = new ApiResponse(false, 404, "Stream not found or already stopped", null);
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
        }

        return streamService.stopPythonStream(privateJobId)
                .then(Mono.fromRunnable(() -> {
                    activeStreamManager.removeStream(publicStreamId);
                }))
                .then(Mono.just(ResponseEntity.ok(
                        new ApiResponse(
                                true,
                                200,
                                "stop signal sent",
                                null)
                )));
    }
}
