package org.jboss.snowdrop.licenses.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.jboss.snowdrop.licenses.properties.GeneratorProperties;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactoryTest {

    private MavenProjectFactory projectFactory;

    private ArtifactFactory artifactFactory;

    @Before
    public void before() throws Exception {
        GeneratorProperties applicationProperties = new GeneratorProperties();
        SnowdropMavenEmbedder mavenEmbedder =
                new MavenEmbedderFactory(applicationProperties).getSnowdropMavenEmbedder();
        PlexusContainer container = mavenEmbedder.getPlexusContainer();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(applicationProperties, mavenEmbedder);
        artifactFactory = container.lookup(ArtifactFactory.class);
        projectFactory = new MavenProjectFactory(container, projectBuildingRequestFactory, artifactFactory);
    }

    @Test
    public void shouldGetMavenProjectForArtifact() throws MavenProjectFactoryException {
        Artifact junitArtifact = artifactFactory.createArtifact("junit", "junit", "4.12", null, "jar");
        Artifact hamcrestArtifact = artifactFactory.createArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar");

        MavenProject project = projectFactory.getMavenProject(junitArtifact);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
    }

    @Test
    public void shouldGetMavenProjectForFile() throws MavenProjectFactoryException {
        Artifact testArtifact = artifactFactory.createArtifact("test", "test","1.0", null, "jar");
        Artifact junitArtifact = artifactFactory.createArtifact("junit", "junit", "4.12", null, "jar");
        Artifact hamcrestArtifact = artifactFactory.createArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar");

        MavenProject project = projectFactory.getMavenProject("target/test-classes/test-pom.xml");

        assertThat(project.getArtifact()).isEqualTo(testArtifact);
        assertThat(project.getArtifacts()).containsOnly(junitArtifact, hamcrestArtifact);
    }

}