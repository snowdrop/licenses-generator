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

package org.snowdrop.licenses;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DependencyContainerFactory {

    public DependencyContainer getDependencyContainer(String groupId, String artifactId, String version) {
        return getDependencyContainer(groupId, artifactId, version, "jar");
    }

    public DependencyContainer getDependencyContainer(String groupId, String artifactId, String version, String type) {
        Dependency dependency = new DependencyFactory().getDependency(groupId, artifactId, version, type);
        Set<DependencyElement> dependencyElements = getMavenProjects(dependency).stream()
                .map(DependencyElement::new)
                .collect(Collectors.toSet());
        return new DependencyContainer(dependencyElements);
    }

    private Set<MavenProject> getMavenProjects(Dependency dependency) {
        try {
            SnowdropMavenEmbedder mavenEmbedder = new MavenEmbedderFactory().getSnowdropMavenEmbedder();
            ProjectBuildingRequest projectBuildingRequest = new ProjectBuildingRequestFactory(mavenEmbedder)
                    .getProjectBuildingRequest();
            MavenProjectFactory mavenProjectFactory =
                    new MavenProjectFactory(mavenEmbedder.getPlexusContainer(), projectBuildingRequest);
            MavenProject root = mavenProjectFactory.getMavenProject(dependency);
            Set<Dependency> dependencies = new HashSet<>();
            dependencies.addAll(root.getDependencies());
            if (root.getDependencyManagement() != null) {
                dependencies.addAll(root.getDependencyManagement().getDependencies());
            }
            TransitiveDependenciesCollector transitiveDependenciesCollector =
                    new TransitiveDependenciesCollector(mavenProjectFactory);

            return dependencies.stream()
                    .map(mavenProjectFactory::getMavenProject)
                    .flatMap(p -> transitiveDependenciesCollector.getTransitiveMavenProjects(p)
                            .stream())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO throw normal exception
        }
    }

}
