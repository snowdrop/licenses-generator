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
import org.jboss.snowdrop.licenses.internal.RedHatLicenseSanitiser;
import org.jboss.snowdrop.licenses.internal.SnowdropMavenEmbedder;
import org.jboss.snowdrop.licenses.internal.TransitiveMavenProjectsCollector;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactory {

    private final MavenProjectFactory projectFactory;

    private final TransitiveMavenProjectsCollector projectsCollector;

    private final ArtifactFactory artifactFactory;

    private final RedHatLicenseSanitiser licenseSanitiser;

    public LicenseSummaryFactory() {
        ApplicationProperties properties = new ApplicationProperties();
        MavenEmbedderFactory mavenEmbedderFactory = new MavenEmbedderFactory(properties);
        SnowdropMavenEmbedder mavenEmbedder = mavenEmbedderFactory.getSnowdropMavenEmbedder();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(properties, mavenEmbedder);

        try {
            this.artifactFactory = mavenEmbedder.getPlexusContainer()
                    .lookup(ArtifactFactory.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
        this.projectFactory =
                new MavenProjectFactory(mavenEmbedder.getPlexusContainer(), projectBuildingRequestFactory);
        this.projectsCollector =
                new TransitiveMavenProjectsCollector(properties, projectFactory, artifactFactory);
        this.licenseSanitiser = new RedHatLicenseSanitiser("rh-license-names.json");
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version) {
        return getLicenseSummary(groupId, artifactId, version, "jar");
    }

    public LicenseSummary getLicenseSummary(String groupId, String artifactId, String version, String type) {
        Artifact artifact = artifactFactory.createArtifact(groupId, artifactId, version, "compile", type);
        MavenProject project;
        try {
            project = projectFactory.getMavenProject(artifact);
        } catch (MavenProjectFactoryException e) {
            throw new RuntimeException(e);
        }

        List<DependencyElement> dependencyElements = projectsCollector.getTransitiveMavenProjects(project)
                .stream()
                .map(DependencyElement::new)
                .map(this::findDependencyLicenses)
                .sorted(Comparator.comparing(DependencyElement::getGroupId)
                        .thenComparing(DependencyElement::getArtifactId)
                        .thenComparing(DependencyElement::getVersion))
                .collect(Collectors.toList());

        return new LicenseSummary(dependencyElements);
    }

    private DependencyElement findDependencyLicenses(DependencyElement dependencyElement) {
        Set<LicenseElement> fixedLicenses = dependencyElement.getLicenses()
                .stream()
                .map(licenseSanitiser::fix)
                .collect(Collectors.toSet());
        dependencyElement.setLicenses(fixedLicenses);
        return dependencyElement;
    }

}
