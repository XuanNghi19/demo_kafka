package com.qkdt.demo_kafka.manager;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveStreamManager {

    private final Map<String, String> activeJobs = new ConcurrentHashMap<>();

    public String registerNewStream() {
        String publicStreamId = UUID.randomUUID().toString();
        String privateJobId = UUID.randomUUID().toString();
        activeJobs.put(publicStreamId, privateJobId);

        return publicStreamId;
    }

    public String getJobId(String publicStreamId) {
        return activeJobs.get(publicStreamId);
    }

    public String removeStream(String publicStreamId) {
        return activeJobs.remove(publicStreamId);
    }

}
