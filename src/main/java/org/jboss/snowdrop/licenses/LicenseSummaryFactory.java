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

package org.jboss.snowdrop.licenses;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.jboss.snowdrop.licenses.internal.ApplicationProperties;
import org.jboss.snowdrop.licenses.internal.DependencyFactory;
import org.jboss.snowdrop.licenses.internal.MavenEmbedderFactory;
import org.jboss.snowdrop.licenses.internal.MavenProjectFactory;
import org.jboss.snowdrop.licenses.internal.MavenProjectFactoryException;
import org.jboss.snowdrop.licenses.internal.ProjectBuildingRequestFactory;
import org.jboss.snowdrop.licenses.internal.SnowdropMavenEmbedder;
import org.jboss.snowdrop.licenses.internal.TransitiveDependenciesCollector;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactory {

    private final Logger logger = Logger.getLogger(LicenseSummaryFactory.class.getSimpleName());

    private final ApplicationProperties applicationProperties;

    private final MavenProjectFactory mavenProjectFactory;

    private final TransitiveDependenciesCollector transitiveDependenciesCollector;

    public LicenseSummaryFactory() {
        this.applicationProperties = new ApplicationProperties();
        MavenEmbedderFactory mavenEmbedderFactory = new MavenEmbedderFactory(applicationProperties);
        SnowdropMavenEmbedder mavenEmbedder = mavenEmbedderFactory.getSnowdropMavenEmbedder();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(applicationProperties, mavenEmbedder);
        this.mavenProjectFactory = new MavenProjectFactory(mavenEmbedder.getPlexusContainer(),
                projectBuildingRequestFactory.getProjectBuildingRequest());
        this.transitiveDependenciesCollector =
                new TransitiveDependenciesCollector(applicationProperties, mavenProjectFactory);
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version) {
        return getLicenseSummary(groupId, artifactId, version, "jar");
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version, String type) {
        Dependency dependency = new DependencyFactory().getDependency(groupId, artifactId, version, type);
        Set<DependencyElement> dependencyElements = getMavenProjects(dependency).stream()
                .map(DependencyElement::new)
                .collect(Collectors.toSet());
        return new LicenseSummary(dependencyElements);
    }

    private Set<MavenProject> getMavenProjects(Dependency dependency) {
        Set<Dependency> dependencies = new HashSet<>();
        MavenProject root;
        try {
            root = mavenProjectFactory.getMavenProject(dependency);
        } catch (MavenProjectFactoryException e) {
            logger.warning(e.getMessage());
            return Collections.emptySet();
        }

        dependencies.addAll(root.getDependencies());

        if (root.getDependencyManagement() != null) {
            dependencies.addAll(root.getDependencyManagement()
                    .getDependencies());
        }

        return dependencies.stream()
                .map(d -> {
                    try {
                        return mavenProjectFactory.getMavenProject(d);
                    } catch (MavenProjectFactoryException e) {
                        logger.warning(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(p -> transitiveDependenciesCollector.getTransitiveMavenProjects(p)
                        .stream())
                .collect(Collectors.toSet());
    }

}
