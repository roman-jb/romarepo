package org.example.springback.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springback.MavenArtifact;
import org.example.springback.service.MavenArtifactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class ApiControllerSearch {

    @Autowired
    private MavenArtifactService mavenArtifactService;

    @GetMapping("/**")
    public String searchApi(HttpServletRequest request, @RequestParam("query") String query) throws JsonProcessingException {
        System.out.println("]]] NEW SEARCH REQUEST [[[");
        System.out.println("Search Query is: " + query);
        //query = query.replace("%20", " ");
        List<MavenArtifact> results = mavenArtifactService.findArtifacts(query);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(results);
    }
}
