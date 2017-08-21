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

package org.jboss.snowdrop.licenses.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveMavenProjectsCollector {

    private final Logger logger = Logger.getLogger(TransitiveMavenProjectsCollector.class.getSimpleName());

    private final ApplicationProperties properties;

    private final MavenProjectFactory projectFactory;

    private final ArtifactFactory artifactFactory;

    public TransitiveMavenProjectsCollector(ApplicationProperties properties, MavenProjectFactory projectFactory,
            ArtifactFactory artifactFactory) {
        this.properties = properties;
        this.projectFactory = projectFactory;
        this.artifactFactory = artifactFactory;
    }

    public Set<MavenProject> getTransitiveMavenProjects(MavenProject rootProject) {
        Set<MavenProject> mavenProjects = new HashSet<>();
        Set<Dependency> rootDependencies = new HashSet<>();
        rootDependencies.addAll(rootProject.getDependencies());
        if (rootProject.getDependencyManagement() != null) {
            rootDependencies.addAll(rootProject.getDependencyManagement()
                    .getDependencies());
        }

        rootDependencies.stream()
                .map(this::dependencyToArtifact)
                .map(this::getMavenProjectOrNull)
                .filter(Objects::nonNull)
                .peek(mavenProjects::add)
                .forEach(p -> this.recursivelyGetTransitiveMavenProjects(p, mavenProjects));

        return mavenProjects;
    }

    private void recursivelyGetTransitiveMavenProjects(MavenProject root, Set<MavenProject> mavenProjects) {
        root.getDependencies()
                .stream()
                .map(this::dependencyToArtifact)
                .filter(a -> this.shouldInclude(a, mavenProjects))
                .map(this::getMavenProjectOrNull)
                .filter(Objects::nonNull)
                .peek(mavenProjects::add)
                .forEach(p -> recursivelyGetTransitiveMavenProjects(p, mavenProjects));
    }

    private MavenProject getMavenProjectOrNull(Artifact artifact) {
        try {
            return projectFactory.getMavenProject(artifact);
        } catch (MavenProjectFactoryException e) {
            logger.warning(e.getMessage());
            return null;
        }
    }

    private Artifact dependencyToArtifact(Dependency dependency) {
        VersionRange versionRange = VersionRange.createFromVersion(dependency.getVersion());
        return artifactFactory.createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope(),
                dependency.isOptional());
    }

    private boolean shouldInclude(Artifact artifact, Set<MavenProject> mavenProjects) {
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
        try {
            MavenProject project = projectFactory.getMavenProject(artifact, false);
            return !mavenProjects.contains(project);
        } catch (MavenProjectFactoryException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

}
