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

    public MavenArtifact addArtifact(String groupId, String artifactId, String version) {
        MavenArtifact artifact = new MavenArtifact();
        artifact.setGroupId(groupId);
        artifact.setArtifactId(artifactId);
        artifact.setVersion(version);
        return mavenArtifactRepository.save(artifact);
    }

    public List<MavenArtifact> findArtifacts(String query) {
        return mavenArtifactRepository
                .findMavenArtifactsByGroupIdContainingIgnoreCaseOrArtifactIdContainingIgnoreCase(query, query);
    }
}