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

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jboss.snowdrop.licenses.maven.MavenArtifact;
import org.jboss.snowdrop.licenses.maven.MavenEmbedderFactory;
import org.jboss.snowdrop.licenses.maven.MavenProjectFactory;
import org.jboss.snowdrop.licenses.maven.MavenProjectFactoryException;
import org.jboss.snowdrop.licenses.maven.ProjectBuildingRequestFactory;
import org.jboss.snowdrop.licenses.maven.SnowdropMavenEmbedder;
import org.jboss.snowdrop.licenses.maven.TransitiveMavenProjectsCollector;
import org.jboss.snowdrop.licenses.properties.GeneratorProperties;
import org.jboss.snowdrop.licenses.sanitiser.RedHatLicenseSanitiser;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class responsible for retrieving licenses information based on a provided GAV.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactory {

    private final MavenProjectFactory projectFactory;

    private final TransitiveMavenProjectsCollector projectsCollector;

    private final ArtifactFactory artifactFactory;

    private final RedHatLicenseSanitiser licenseSanitiser;

    /**
     * @throws RuntimeException if any of the initialisation steps fail.
     */
    public LicenseSummaryFactory() {
        this(new GeneratorProperties());
    }

    /**
     * @throws RuntimeException if any of the initialisation steps fail.
     */
    public LicenseSummaryFactory(GeneratorProperties properties) {
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
                new MavenProjectFactory(mavenEmbedder.getPlexusContainer(), projectBuildingRequestFactory, artifactFactory);
        this.projectsCollector =
                new TransitiveMavenProjectsCollector(properties, projectFactory, artifactFactory);
        this.licenseSanitiser =
                new RedHatLicenseSanitiser("rh-license-names.json", "rh-license-exceptions.json");
    }

    /**
     * Get licenses based on groupId:artifactId:type:version.
     * {@link RuntimeException} is thrown if main dependency cannot be loaded. For any other failed dependency only a
     * warning message is printed.
     *
     * @param mavenArtifact maven coordinates of the main maven artifact
     * @return license summary XML element containing all transitive dependencies and their licenses.
     * @throws RuntimeException if main dependency cannot be loaded.
     */
    public LicenseSummary getLicenseSummary(MavenArtifact mavenArtifact) {
        MavenProject project = null;
        try {
            project = projectFactory.getMavenProject(mavenArtifact, true);
        } catch (MavenProjectFactoryException e) {
            throw new RuntimeException(e);
        }

        return getLicenseSummary(project);
    }

    /**
     * Get licenses for a list of artifacts, non transitively.
     * @param artifacts list of artifacts to include in the license summary
     * @return a license summary for the listed artifacts
     * @throws RuntimeException if any of the artifacts in the list cannot be loaded.
     */
    public LicenseSummary getNonTransitiveLicenseSummary(Collection<MavenArtifact> artifacts) {
        Collection<MavenProject> projects = artifacts.parallelStream().map(a -> {
            try {
                return projectFactory.getMavenProject(a, false);
            } catch (MavenProjectFactoryException e) {
                throw new RuntimeException("Unable to create a maven project for artifact: " + a, e);
            }
        }).collect(Collectors.toList());
        return getLicenseSummary(projects);
    }

    /**
     * Get licenses based on a provided pom.xml.
     * {@link RuntimeException} is thrown if main dependency cannot be loaded. For any other failed dependency only a
     * warning message is printed.
     *
     * @param pomFilePath Path to the pom.xml.
     * @return license summary XML element containing all transitive dependencies and their licenses.
     * @throws RuntimeException if main dependency cannot be loaded.
     */
    public LicenseSummary getLicenseSummary(String pomFilePath) {
        MavenProject project;
        try {
            project = projectFactory.getMavenProject(pomFilePath);
        } catch (MavenProjectFactoryException e) {
            throw new RuntimeException(e);
        }

        return getLicenseSummary(project);
    }

    private LicenseSummary getLicenseSummary(MavenProject project) {
        Collection<MavenProject> mavenProjects = projectsCollector.getTransitiveMavenProjects(project);
        return getLicenseSummary(mavenProjects);
    }

    private LicenseSummary getLicenseSummary(Collection<MavenProject> projects) {
        List<DependencyElement> dependencyElements =
                projects.parallelStream()
                .map(DependencyElement::new)
                .map(this::fixDependencyLicenses)
                .sorted(Comparator.comparing(DependencyElement::getGroupId)
                        .thenComparing(DependencyElement::getArtifactId)
                        .thenComparing(DependencyElement::getVersion))
                .collect(Collectors.toList());

        return new LicenseSummary(dependencyElements);
    }

    private DependencyElement fixDependencyLicenses(DependencyElement dependencyElement) {
        resolveExceptionalLicenses(dependencyElement);
        Set<LicenseElement> fixedLicenses = dependencyElement.getLicenses()
                .stream()
                .map(licenseSanitiser::fix)
                .collect(Collectors.toSet());
        dependencyElement.setLicenses(fixedLicenses);
        return dependencyElement;
    }

    private void resolveExceptionalLicenses(DependencyElement dependencyElement) {
        Optional<Set<LicenseElement>> maybeLicenses = licenseSanitiser.getLicensesForArtifact(dependencyElement.toGav());
        maybeLicenses.ifPresent(dependencyElement::setLicenses);
    }

}
