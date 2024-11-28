package org.example.springback.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.example.springback.ApiUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@RestController
@RequestMapping("/api/browse")
public class ApiControllerBrowse {

    @Value("${rootDir}")
    private String rootDir;

    @GetMapping("/**")
    public ResponseEntity<String> browseApi(HttpServletRequest request) throws IOException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
        );
        System.out.println("[[[ NEW BROWSE REQUEST ]]]");
        System.out.println("Raw Path is: " + path );
        path = path.replace("/api/browse", "");
        path = rootDir + path;
        System.out.println("Windows Path is: " + path );
        return ResponseEntity.ok(ApiUtils.GetDirectoryContent(path));
    }
}
