package org.example.webface.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.webface.FileSystemObject;
import org.example.webface.service.BackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseSpringbackController {

    @Controller
    @RequestMapping("/browse")
    public static class BrowseController {

        @Autowired
        private RestTemplate restTemplate;
        @Autowired
        private BackendService backendService;

        @Value("${backend.api}")
        String backendAPIProperty;

        @Value("${backend.url}")
        String backendURLProperty;

        String backendURL;

        @GetMapping("/**")
        public String browse(HttpServletRequest request, Model model) throws JsonProcessingException {
            System.out.println("[[[ NEW BROWSE REQUEST ]]]");
            String path = (String) request.getAttribute(
                    HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
            );
            backendURL = backendAPIProperty;
            System.out.println("Raw Path is: " + path );
            boolean inRootDirectory = path.equals("/browse") || path.equals("/browse/");
            String backendPath = path;
            backendPath = backendURL + backendPath;
            System.out.println("Backend Path is: " + backendPath);
            String jsonFromBackend = restTemplate.getForObject(backendPath, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            List<FileSystemObject> fileSystemObjects = objectMapper.readValue(jsonFromBackend, new TypeReference<>() {
            });
            List<String> directories = new ArrayList<>();
            Map<String, String> filesMap = new HashMap<>();
            for (FileSystemObject fso : fileSystemObjects) {
                if (fso.getType().equals("Directory")) {
                    directories.add(fso.getName());
                } else {
                    String fileDownloadURL = path + "/" + fso.getName();
                    fileDownloadURL = fileDownloadURL.replace("/browse", "");
                    fileDownloadURL = backendURLProperty + fileDownloadURL;
                    filesMap.put(fso.getName(),fileDownloadURL);
                }
            }
            model.addAttribute("directories", directories);
            model.addAttribute("filesMap", filesMap);
            model.addAttribute("path", path);
            model.addAttribute("backendURLProperty", backendURLProperty);
            model.addAttribute("inRootDirectory", inRootDirectory);
            return "browse-sb";
        }
    }
}
