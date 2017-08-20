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

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveDependenciesCollector {

    private final MavenProjectFactory mavenProjectFactory;

    public TransitiveDependenciesCollector(MavenProjectFactory mavenProjectFactory) {
        this.mavenProjectFactory = mavenProjectFactory;
    }

    public Set<MavenProject> getTransitiveMavenProjects(MavenProject root) {
        Set<MavenProject> mavenProjects = new HashSet<>();
        mavenProjects.add(root);

        if (!root.getArtifact()
                .isOptional()) {
            recursivelyGetTransitiveMavenProjects(root, mavenProjects);
        }

        return mavenProjects;
    }

    private void recursivelyGetTransitiveMavenProjects(MavenProject root, Set<MavenProject> mavenProjects) {
        for (Dependency dependency : root.getDependencies()) {
            if (!shouldInclude(dependency)) {
                continue;
            }

            MavenProject project = mavenProjectFactory.getMavenProject(dependency);

            if (mavenProjects.add(project)) {
                recursivelyGetTransitiveMavenProjects(project, mavenProjects);
            }
        }
    }

    // TODO make configurable
    private boolean shouldInclude(Dependency dependency) {
        return !dependency.isOptional() && !"test".equals(dependency.getScope()) && !"system".equals(
                dependency.getScope())
                && !"provided".equals(dependency.getScope()) && !"tests".equals(dependency.getClassifier());
    }

}
