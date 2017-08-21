package org.jboss.snowdrop.licenses.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveDependenciesCollectorTest {

    private MavenProjectFactory projectFactory;

    private TransitiveMavenProjectsCollector projectsCollector;

    private ArtifactFactory artifactFactory;

    @Before
    public void before() throws Exception {
        ApplicationProperties applicationProperties = new ApplicationProperties();
        SnowdropMavenEmbedder embedder = new MavenEmbedderFactory(applicationProperties).getSnowdropMavenEmbedder();
        ProjectBuildingRequestFactory projectBuildingRequestFactory =
                new ProjectBuildingRequestFactory(applicationProperties, embedder);
        artifactFactory = embedder.getPlexusContainer()
                .lookup(ArtifactFactory.class);
        projectFactory = new MavenProjectFactory(embedder.getPlexusContainer(), projectBuildingRequestFactory);
        projectsCollector = new TransitiveMavenProjectsCollector(applicationProperties, projectFactory, artifactFactory);
    }

    @Test
    public void shouldGetAllRequiredArtifacts() throws MavenProjectFactoryException {
        Artifact artifact = artifactFactory.createArtifact("junit", "junit", "4.12", "compile", "jar");
        MavenProject project = projectFactory.getMavenProject(artifact);
        Set<MavenProject> transitiveMavenProjects = projectsCollector.getTransitiveMavenProjects(project);
        assertThat(transitiveMavenProjects).hasSize(1);
    }

}