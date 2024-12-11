package org.example.springback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MavenArtifactRepository extends JpaRepository<MavenArtifact, Long> {

    List<MavenArtifact> findMavenArtifactsByGroupIdContainingIgnoreCaseOrArtifactIdContainingIgnoreCase(String groupId, String artifactId);
}
