/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.snowdrop.licenses.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.jboss.snowdrop.licenses.properties.GeneratorProperties;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveMavenProjectsCollector {

    private final Logger logger = Logger.getLogger(TransitiveMavenProjectsCollector.class.getSimpleName());

    private final GeneratorProperties properties;

    private final MavenProjectFactory projectFactory;

    private final ArtifactFactory artifactFactory;

    public TransitiveMavenProjectsCollector(GeneratorProperties properties, MavenProjectFactory projectFactory,
            ArtifactFactory artifactFactory) {
        this.properties = properties;
        this.projectFactory = projectFactory;
        this.artifactFactory = artifactFactory;
    }

    public Collection<MavenProject> getTransitiveMavenProjects(MavenProject rootProject) {
        Map<Artifact, MavenProject> mavenProjects = new HashMap<>();
        Set<Artifact> failedArtifacts = new HashSet<>();
        Set<Dependency> rootDependencies = new HashSet<>();
        rootDependencies.addAll(rootProject.getDependencies());
        if (properties.isIncludeDependencyManagement() && rootProject.getDependencyManagement() != null) {
            rootDependencies.addAll(rootProject.getDependencyManagement()
                    .getDependencies());
        }

        rootDependencies.stream()
                .map(this::dependencyToArtifact)
                .map(a -> getMavenProjectOrNull(a, failedArtifacts))
                .filter(Objects::nonNull)
                .peek(p -> mavenProjects.put(p.getArtifact(), p))
                .peek(p -> logger.info("Getting transitive dependencies for " + p))
                .forEach(p -> this.recursivelyGetTransitiveMavenProjects(p, mavenProjects, failedArtifacts));

        return mavenProjects.values();
    }

    private void recursivelyGetTransitiveMavenProjects(MavenProject root, Map<Artifact, MavenProject> mavenProjects,
            Set<Artifact> failedArtifacts) {
        root.getDependencies()
                .stream()
                .map(this::dependencyToArtifact)
                .filter(a -> this.shouldInclude(a, mavenProjects, failedArtifacts))
                .map(a -> getMavenProjectOrNull(a, failedArtifacts))
                .filter(Objects::nonNull)
                .peek(p -> mavenProjects.put(p.getArtifact(), p))
                .forEach(p -> recursivelyGetTransitiveMavenProjects(p, mavenProjects, failedArtifacts));
    }

    private MavenProject getMavenProjectOrNull(Artifact artifact, Set<Artifact> failedArtifacts) {
        try {
            return projectFactory.getMavenProject(artifact);
        } catch (MavenProjectFactoryException e) {
            failedArtifacts.add(artifact);
            logger.warning(String.format("Failed to resolve maven project: %s", e.getMessage()));
            return null;
        }
    }

    private Artifact dependencyToArtifact(Dependency dependency) {
        VersionRange versionRange = VersionRange.createFromVersion(dependency.getVersion());
        return artifactFactory.createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope(),
                dependency.isOptional());
    }

    private boolean shouldInclude(Artifact artifact, Map<Artifact, MavenProject> mavenProjects,
            Set<Artifact> failedArtifacts) {
        if (artifact.isOptional() && !properties.isIncludeOptional()) {
            return false;
        }
        if (properties.getExcludedScopes()
                .contains(artifact.getScope())) {
            return false;
        }
        if (properties.getExcludedClassifiers()
                .contains(artifact.getClassifier())) {
            return false;
        }
        return !mavenProjects.containsKey(artifact) && !failedArtifacts.contains(artifact);
    }

}
