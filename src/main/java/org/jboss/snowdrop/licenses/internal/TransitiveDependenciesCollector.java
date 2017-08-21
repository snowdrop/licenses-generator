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

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveDependenciesCollector {

    private final Logger logger = Logger.getLogger(TransitiveDependenciesCollector.class.getSimpleName());

    private final ApplicationProperties applicationProperties;

    private final MavenProjectFactory mavenProjectFactory;

    public TransitiveDependenciesCollector(ApplicationProperties applicationProperties,
            MavenProjectFactory mavenProjectFactory) {
        this.applicationProperties = applicationProperties;
        this.mavenProjectFactory = mavenProjectFactory;
    }

    public Set<MavenProject> getTransitiveMavenProjects(MavenProject root) {
        logger.info(String.format("Getting transitive dependencies for '%s'", root));
        Set<MavenProject> mavenProjects = new HashSet<>();
        mavenProjects.add(root);

        if (!root.getArtifact().isOptional() || applicationProperties.isIncludeOptional()) {
            recursivelyGetTransitiveMavenProjects(root, mavenProjects);
        }

        return mavenProjects;
    }

    private void recursivelyGetTransitiveMavenProjects(MavenProject root, Set<MavenProject> mavenProjects) {
        for (Dependency dependency : root.getDependencies()) {
            if (!shouldInclude(dependency)) {
                continue;
            }

            try {
                MavenProject project = mavenProjectFactory.getMavenProject(dependency);

                if (mavenProjects.add(project)) {
                    logger.fine(String.format("Added transitive dependency '%s'", project));
                    recursivelyGetTransitiveMavenProjects(project, mavenProjects);
                }
            } catch (MavenProjectFactoryException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    private boolean shouldInclude(Dependency dependency) {
        if (dependency.isOptional() && !applicationProperties.isIncludeOptional()) {
            return false;
        }
        if (applicationProperties.getExcludedScopes()
                .contains(dependency.getScope())) {
            return false;
        }
        if (applicationProperties.getExcludedClassifiers()
                .contains(dependency.getClassifier())) {
            return false;
        }

        return true;
    }

}
