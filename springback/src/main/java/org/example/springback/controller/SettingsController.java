package org.example.springback.controller;

import org.example.springback.MavenArtifact;
import org.example.springback.MavenArtifactRepository;
import org.example.springback.Utils;
import org.example.springback.service.MavenArtifactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class SettingsController {

    @Value("${rootDir}")
    private String rootDir;

    @Autowired
    MavenArtifactService mavenArtifactService;

    @GetMapping("/settings/reindexrepo")
    public String reindexRepo() {
        System.out.println("Reindexing Repo...");
        List<String> poms = Utils.findPoms(rootDir);
        List<MavenArtifact> artifacts = new ArrayList<>();
        for (String pomString : poms) {
            try {
                System.out.println("Currently processing: " + pomString);
                artifacts.add(Utils.indexArtifact(pomString));
            }
            catch (Exception e) {
                System.out.println("!!! Something went wrong processing POM: " + pomString);
                System.out.println("The error is: " + e.getMessage());
                e.printStackTrace();
            }
        }
        mavenArtifactService.bulkAddArtifacts(artifacts);
        return "Reindex complete!";
    }

    @GetMapping("/settings/checksums")
    public String checksums() {
        System.out.println("Calculating Checksums...");
        return "Calculating Checksums...";
    }
}
