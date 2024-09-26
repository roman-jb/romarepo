package org.example.webface.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webface.service.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class WebfaceController {

    @Autowired
    private BackendService backendService;

    @GetMapping
    public String index(Model model) {
        String data = backendService.getDataFromBackend();
        model.addAttribute("data", data);
        return "index";
    }

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    @GetMapping("/view-json")
    public String viewJson(Model model) {
        String url = "http://localhost:1234/api/browse";

        // Fetch the JSON data as a String
        String jsonString = restTemplate.getForObject(url, String.class);

        // Parse JSON string into JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add JsonNode to Model
        model.addAttribute("jsonData", jsonNode);

        return "view-json";
    }
}