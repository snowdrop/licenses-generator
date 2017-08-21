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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jboss.snowdrop.licenses.internal.ApplicationProperties;
import org.jboss.snowdrop.licenses.internal.MavenEmbedderFactory;
import org.jboss.snowdrop.licenses.internal.MavenProjectFactory;
import org.jboss.snowdrop.licenses.internal.MavenProjectFactoryException;
import org.jboss.snowdrop.licenses.internal.ProjectBuildingRequestFactory;
import org.jboss.snowdrop.licenses.internal.SnowdropMavenEmbedder;
import org.jboss.snowdrop.licenses.internal.TransitiveDependenciesCollector;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactory {

    private final MavenProjectFactory mavenProjectFactory;

    private final TransitiveDependenciesCollector transitiveDependenciesCollector;

    private final ArtifactFactory artifactFactory;

    public LicenseSummaryFactory() {
        ApplicationProperties applicationProperties = new ApplicationProperties();
        MavenEmbedderFactory mavenEmbedderFactory = new MavenEmbedderFactory(applicationProperties);
        SnowdropMavenEmbedder mavenEmbedder = mavenEmbedderFactory.getSnowdropMavenEmbedder();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(applicationProperties, mavenEmbedder);

        this.mavenProjectFactory =
                new MavenProjectFactory(mavenEmbedder.getPlexusContainer(), projectBuildingRequestFactory);
        this.transitiveDependenciesCollector =
                new TransitiveDependenciesCollector(applicationProperties, mavenProjectFactory);
        try {
            this.artifactFactory = mavenEmbedder.getPlexusContainer()
                    .lookup(ArtifactFactory.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version) {
        return getLicenseSummary(groupId, artifactId, version, "jar");
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version, String type) {
        Artifact artifact = artifactFactory.createArtifact(groupId, artifactId, version, "compile", type);
        MavenProject project;
        try {
            project = mavenProjectFactory.getMavenProject(artifact);
        } catch (MavenProjectFactoryException e) {
            throw new RuntimeException(e);
        }

        Set<DependencyElement> dependencyElements = transitiveDependenciesCollector.getTransitiveMavenProjects(project)
                .stream()
                .map(DependencyElement::new)
                .collect(Collectors.toSet());
        return new LicenseSummary(dependencyElements);
    }

}
