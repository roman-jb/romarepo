package org.example.springback.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/dev2")
public class FileController3 {

    @Value("${rootDir}")
    private String rootDir;

    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) throws MalformedURLException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
        );
        System.out.println(">>> NEW GET REQUEST <<<");
        System.out.println("Raw Path is: " + path );
        path = path.replace("/dev2", ""); //TODO: Make it not capture the dev2 part
        path = path.replace("/", "\\");
        String windowsPath = rootDir + path;
        System.out.println("Windows Path is: " + windowsPath );
        File requestedFile = new File(windowsPath);
        Path filePath = requestedFile.toPath();
        System.out.println("File Path is: " + filePath);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                System.out.println("File found: " + requestedFile.getAbsolutePath() );
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("! File not found: " + requestedFile.getAbsolutePath() );
                return ResponseEntity.status(404).body(null);
            }
        } catch (MalformedURLException e) {
            System.out.println("!!! Something went horribly wrong processing the request for file: " + requestedFile.getAbsolutePath());
            System.out.println("The error is: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
