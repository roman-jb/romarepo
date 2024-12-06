package org.example.springback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/api/search")
public class ApiControllerSearch {

    @GetMapping("/**")
    public String searchApi(HttpServletRequest request, @RequestParam("query") String query) {
        System.out.println("]]] NEW SEARCH REQUEST [[[");
        System.out.println("Search Query is: " + query);
        query = query.replace("%20", " ");
        return "Search API is not implemented yet.";
    }
}
