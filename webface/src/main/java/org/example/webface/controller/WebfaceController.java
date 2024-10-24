package org.example.webface.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.webface.FileSystemObject;
import org.example.webface.service.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/")
public class WebfaceController {

    public static final String BASE_URL = "http://localhost:1234/api";

    @GetMapping
    public String home() {
        return "home";
    }

    @PostMapping("/search")
    public String search(@RequestParam("searchQuery") String searchQuery, Model model) {
        model.addAttribute("searchQuery", searchQuery);
        return "search";
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
        System.out.println("Directory Name is: " + directoryName); //DEBUG - Remove
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
        System.out.println("Backend API URL: " + lastURL); //DEBUG - Remove
        String jsonFromBackend = restTemplate.getForObject(lastURL, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        List<FileSystemObject> fileSystemObjects = objectMapper.readValue(jsonFromBackend, new TypeReference<>() {
        });
        List<String> directories = new ArrayList<>();
        //List<String> files = new ArrayList<>();
        Map<String, String> filesMap = new HashMap<>();
        for (FileSystemObject fso : fileSystemObjects) {
            if (fso.getType().equals("Directory")) {
                directories.add(fso.getName());
            } else {
                //files.add(fso.getName());
                String fileDownloadURL = lastURL + "/" + fso.getName();
                //System.out.println("[1] Download URL for [" + fso.getName() + "] is: " + fileDownloadURL); //DEBUG - Remove
                fileDownloadURL = fileDownloadURL.replace("/api/browse", "/myrepo");
                //System.out.println("[2] Download URL for [" + fso.getName() + "] is: " + fileDownloadURL); //DEBUG - Remove
                filesMap.put(fso.getName(),fileDownloadURL);
            }
        }
        //if (!isInRootDirectory && !lastURL.equals(BASE_URL + "/browse")) {fileSystemObjects.addFirst(new FileSystemObject("[Parent Directory]", "Directory"));} // Legacy - Remove?
        if (!isInRootDirectory && !lastURL.equals(BASE_URL + "/browse")) {directories.addFirst("[Parent Directory]");}
        //model.addAttribute("fileSystemObjects", fileSystemObjects);
        model.addAttribute("directories", directories);
        //model.addAttribute("files", files);
        model.addAttribute("filesMap", filesMap);
        return "browse";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

}





































