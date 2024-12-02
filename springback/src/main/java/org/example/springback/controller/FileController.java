package org.example.springback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;


@RestController
@RequestMapping("/springback")
public class FileController {

    @Value("${rootDir}")
    private String rootDir;

    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
        );
        System.out.println(">>> NEW GET REQUEST <<<");
        System.out.println("Raw Path is: " + path );
        path = path.replace("/springback", ""); //TODO: Make it not capture the dev2 part
        //path = path.replace("/", "\\"); //Excessive - Java/SB parses it automatically?
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

    @PutMapping("/**")
    public ResponseEntity<?> handleFileUpload(@RequestBody byte[] fileBytes, HttpServletRequest request) {
        System.out.println("<<< NEW PUT REQUEST >>>");
        // Extract the file path from the request URL
        String requestURL = request.getRequestURI();
        System.out.println("Raw request URL is: " + requestURL);
        requestURL = requestURL.replace("/springback", ""); //TODO: Make it not capture the dev2 part
        System.out.println("Request URL is: " + requestURL);
        String filePath = rootDir + requestURL;
        System.out.println("File Path is: " + filePath);

        // Ensure directories exist
        //TODO: This should return a status - success or error.
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        // Save the file
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(fileBytes);
            return ResponseEntity.ok("File uploaded successfully: " + filePath);
        }
        catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + filePath);
        }
    }
}
