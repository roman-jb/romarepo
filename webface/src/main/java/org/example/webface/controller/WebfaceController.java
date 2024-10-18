package org.example.webface.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webface.FileSystemObject;
import org.example.webface.service.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class WebfaceController {

    public static final String BASE_URL = "http://localhost:1234/api";

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
    private RestTemplate restTemplate;

    String lastURL;
    @GetMapping("/browse")
    public String browse(@RequestParam(defaultValue = "") String directoryName, Model model) throws JsonProcessingException {
        //TODO: Refactor / optimize?
        //System.out.println("Directory Name is: " + directoryName); //DEBUG - Remove
        boolean isInRootDirectory = directoryName.isEmpty();
//        vvv Looks nice, but doesn't work properly vvv
//        lastURL = (isInRootDirectory) ? BASE_URL + "/browse" : lastURL + "/" + directoryName;
//        lastURL = (directoryName.equals("[Parent Directory]")) ? lastURL.substring(0, lastURL.lastIndexOf("/")) : lastURL ;
        if (isInRootDirectory) {
            lastURL = BASE_URL + "/browse";
        } else if (directoryName.equals("[Parent Directory]")) {
            lastURL = lastURL.substring(0, lastURL.lastIndexOf("/"));
        } else {
            lastURL = lastURL + "/" + directoryName;
        }
        //System.out.println("Backend URL: " + lastURL); //DEBUG - Remove
        String jsonFromBackend = restTemplate.getForObject(lastURL, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        List<FileSystemObject> fileSystemObjects = objectMapper.readValue(jsonFromBackend, new TypeReference<>() {
        });
        if (!isInRootDirectory && !lastURL.equals(BASE_URL + "/browse")) {fileSystemObjects.addFirst(new FileSystemObject("[Parent Directory]", "Directory"));}
        model.addAttribute("fileSystemObjects", fileSystemObjects);
        return "browse";
    }
}





































