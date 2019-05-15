package me.snowdrop.licenses;

import me.snowdrop.licenses.maven.MavenProjectFactory;
import me.snowdrop.licenses.utils.Gav;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GavFinder {
    private final MavenProjectFactory mavenProjectFactory;

    GavFinder(MavenProjectFactory mavenProjectFactory) {
        this.mavenProjectFactory = mavenProjectFactory;
    }

    public Collection<Gav> inMavenProject(Path pomPath) {
        return getArtifactsForMavenProject(pomPath)
                .map(a -> new Gav(a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getType()))
                .collect(Collectors.toSet());
    }

    Stream<Artifact> getArtifactsForMavenProject(Path pomPath) {
        return mavenProjectFactory.getMavenProjects(pomPath.toFile(), true)
                .stream()
                .map(MavenProject::getArtifacts)
                .flatMap(Set::stream);
    }
}
