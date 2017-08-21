package org.jboss.snowdrop.licenses.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
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
        ApplicationProperties applicationProperties = new ApplicationProperties();
        SnowdropMavenEmbedder mavenEmbedder =
                new MavenEmbedderFactory(applicationProperties).getSnowdropMavenEmbedder();
        PlexusContainer container = mavenEmbedder.getPlexusContainer();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(applicationProperties, mavenEmbedder);
        projectFactory = new MavenProjectFactory(container, projectBuildingRequestFactory);
        artifactFactory = container.lookup(ArtifactFactory.class);
    }

    @Test
    public void shouldGetMavenProjectForArtifact() throws MavenProjectFactoryException {
        Artifact junitArtifact = artifactFactory.createArtifact("junit", "junit", "4.12", null, "jar");
        Artifact hamcrestArtifact = artifactFactory.createArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar");

        MavenProject project = projectFactory.getMavenProject(junitArtifact);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
        assertThat(project.getArtifacts()).hasSize(1);
    }

}