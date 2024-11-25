package org.example.springback.controller;

import org.example.springback.ExampleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("dev")
public class FileController2 {

    // Handling GET requests
    @GetMapping("/{id}")
    public ResponseEntity<String> getExample(@PathVariable("id") Long id) {
        // Logic to handle GET request
        String response = "Handling GET request for ID: " + id;
        return ResponseEntity.ok(response);
    }

    // Handling PUT requests
    @PutMapping("/{id}")
    public ResponseEntity<String> updateExample(
            @PathVariable("id") Long id,
            @RequestBody ExampleRequest request) {
        // Logic to handle PUT request
        String response = "Handling PUT request to update ID: " + id + " with data: " + request;
        return ResponseEntity.ok(response);
    }
}