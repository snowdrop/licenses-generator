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

package me.snowdrop.licenses;

import me.snowdrop.licenses.maven.MavenEmbedderFactory;
import me.snowdrop.licenses.maven.MavenProjectFactory;
import me.snowdrop.licenses.maven.ProjectBuildingRequestFactory;
import me.snowdrop.licenses.maven.SnowdropMavenEmbedder;
import me.snowdrop.licenses.properties.GeneratorProperties;
import me.snowdrop.licenses.sanitiser.AliasLicenseSanitiser;
import me.snowdrop.licenses.sanitiser.ExceptionLicenseSanitiser;
import me.snowdrop.licenses.sanitiser.LicenseSanitiser;
import me.snowdrop.licenses.sanitiser.LicenseServiceSanitiser;
import me.snowdrop.licenses.sanitiser.MavenSanitiser;
import me.snowdrop.licenses.sanitiser.NoopLicenseSanitiser;
import me.snowdrop.licenses.utils.Gav;
import me.snowdrop.licenses.xml.LicenseSummary;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicensesGenerator {

    private static final String LICENSE_NAMES_FILE = "rh-license-names.json";

    private static final String LICENSE_EXCEPTIONS_FILE = "rh-license-exceptions.json";

    private final ArtifactFactory artifactFactory;

    private final MavenProjectFactory mavenProjectFactory;

    private final LicenseSummaryFactory licenseSummaryFactory;

    private final LicensesFileManager licensesFileManager;

    private final Optional<String> licenseServiceUrl;

    public LicensesGenerator() throws LicensesGeneratorException {
        this(new GeneratorProperties());
    }

    public LicensesGenerator(GeneratorProperties generatorProperties) throws LicensesGeneratorException {
        MavenEmbedderFactory mavenEmbedderFactory = new MavenEmbedderFactory();
        SnowdropMavenEmbedder mavenEmbedder = mavenEmbedderFactory.getSnowdropMavenEmbedder();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(generatorProperties, mavenEmbedder);
        PlexusContainer container = mavenEmbedder.getPlexusContainer();
        try {
            ProjectBuilder projectBuilder = container.lookup(ProjectBuilder.class);
            this.mavenProjectFactory = new MavenProjectFactory(projectBuilder, projectBuildingRequestFactory);
            this.artifactFactory = container.lookup(ArtifactFactory.class);
        } catch (ComponentLookupException e) {
            throw new LicensesGeneratorException(e.getMessage(), e);
        }

        this.licenseServiceUrl = generatorProperties.getLicenseServiceUrl();
        this.licenseSummaryFactory = createLicenseSummaryFactory();
        this.licensesFileManager = new LicensesFileManager();
    }

    public void generateLicensesForPom(String pomPath, String resultPath) throws LicensesGeneratorException {
        List<MavenProject> mavenProjects = mavenProjectFactory.getMavenProjects(new File(pomPath), true);
        Set<Artifact> artifacts = mavenProjects.stream()
                .map(MavenProject::getArtifacts)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        generateLicensesForArtifacts(artifacts, resultPath);
    }

    public void generateLicensesForGavs(Collection<Gav> gavs, String resultPath) throws LicensesGeneratorException {
        Set<Artifact> artifacts = gavs.parallelStream()
                .map(this::gavToArtifact)
                .collect(Collectors.toSet());

        generateLicensesForArtifacts(artifacts, resultPath);
    }

    private void generateLicensesForArtifacts(Collection<Artifact> artifacts, String resultPath)
            throws LicensesGeneratorException {
        LicenseSummary licenseSummary = licenseSummaryFactory.getLicenseSummary(artifacts);
        licensesFileManager.createLicensesXml(licenseSummary, resultPath);
        licensesFileManager.createLicensesHtml(licenseSummary, resultPath);
    }

    protected Artifact gavToArtifact(Gav gav) {
        return artifactFactory.createArtifact(gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), null,
                gav.getType());
    }


    protected LicenseSummaryFactory createLicenseSummaryFactory() {
        LicenseSanitiser noopLicenseSanitiser = new NoopLicenseSanitiser();
        LicenseSanitiser aliasLicenseSanitiser = new AliasLicenseSanitiser(LICENSE_NAMES_FILE, noopLicenseSanitiser);
        LicenseSanitiser mavenSanitiser = new MavenSanitiser(mavenProjectFactory, aliasLicenseSanitiser);

        Optional<LicenseSanitiser> maybeExternalLicenseSanitiser =
                licenseServiceUrl.<LicenseSanitiser>map(
                        url -> new LicenseServiceSanitiser(url, mavenSanitiser)
                );

        LicenseSanitiser secondSanitiser = maybeExternalLicenseSanitiser.orElse(mavenSanitiser);

        LicenseSanitiser exceptionLicenseSanitiser =
                new ExceptionLicenseSanitiser(LICENSE_EXCEPTIONS_FILE, secondSanitiser);

        return new LicenseSummaryFactory(exceptionLicenseSanitiser);
    }
}
