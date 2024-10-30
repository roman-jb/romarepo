package org.example.webface.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BackendService {

    private final RestTemplate restTemplate;

    public BackendService() {
        this.restTemplate = new RestTemplate();
    }

    public String getDataFromBackend() {
        String backendUrl = "http://127.0.0.1:1234/api";
        return restTemplate.getForObject(backendUrl, String.class);
    }
}