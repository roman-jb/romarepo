package org.example.webface.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webface.FileSystemObject;
import org.example.webface.service.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class WebfaceController {

    @GetMapping
    public String home(Model model) {
        return "home";
    }

    @Autowired
    private BackendService backendService;

    @GetMapping("/dev")
    public String index(Model model) {
        String data = backendService.getDataFromBackend();
        model.addAttribute("data", data);
        return "dev";
    }

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    @GetMapping("/browse")
    public String viewJson(Model model) throws JsonProcessingException {
        String url = "http://localhost:1234/api/browse";

        // Fetch the JSON data as a String
        String jsonString = restTemplate.getForObject(url, String.class);

        // Parse JSON into a list of Directory objects
        ObjectMapper objectMapper = new ObjectMapper();
        List<FileSystemObject> fileSystemObjects = objectMapper.readValue(jsonString, new TypeReference<>() {
        });

        // Add the list to the model
        model.addAttribute("fileSystemObjects", fileSystemObjects);

        return "browse";
    }
}