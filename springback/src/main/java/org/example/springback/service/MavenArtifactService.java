package org.example.springback.service;

import org.example.springback.MavenArtifact;
import org.example.springback.MavenArtifactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MavenArtifactService {

    @Autowired
    private MavenArtifactRepository mavenArtifactRepository;

    public boolean addArtifact(MavenArtifact artifact) {
        try {
            mavenArtifactRepository.save(artifact);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public List<MavenArtifact> findArtifacts(String query) {
        return mavenArtifactRepository
                .findMavenArtifactsByGroupIdContainingIgnoreCaseOrArtifactIdContainingIgnoreCase(query, query);
    }

    public boolean bulkAddArtifacts(List<MavenArtifact> artifacts) {
        try {
            mavenArtifactRepository.saveAll(artifacts);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}