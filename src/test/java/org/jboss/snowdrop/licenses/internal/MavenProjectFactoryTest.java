package org.jboss.snowdrop.licenses.internal;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactoryTest {

    @Mock
    private ArtifactHandler mockArtifactHandler;

    private ProjectBuildingRequestFactory projectBuildingRequestFactory;

    private PlexusContainer container;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        ApplicationProperties applicationProperties = new ApplicationProperties();
        SnowdropMavenEmbedder mavenEmbedder =
                new MavenEmbedderFactory(applicationProperties).getSnowdropMavenEmbedder();
        container = mavenEmbedder.getPlexusContainer();
        projectBuildingRequestFactory = new ProjectBuildingRequestFactory(applicationProperties, mavenEmbedder);
    }

    @Test
    public void shouldGetMavenProjectForDependency() throws ComponentLookupException, MavenProjectFactoryException {
        when(mockArtifactHandler.getClassifier()).thenReturn(null);

        Artifact junitArtifact = new DefaultArtifact("junit", "junit", "4.12", null, "jar", null, mockArtifactHandler);
        Artifact hamcrestArtifact =
                new DefaultArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar", null, mockArtifactHandler);

        Dependency dependency = new DependencyFactory().getDependency(junitArtifact.getGroupId(),
                junitArtifact.getArtifactId(), junitArtifact.getVersion(), junitArtifact.getType(),
                junitArtifact.getScope(), junitArtifact.getClassifier(), junitArtifact.isOptional());

        MavenProjectFactory factory = new MavenProjectFactory(container, projectBuildingRequestFactory);
        MavenProject project = factory.getMavenProject(dependency);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
        assertThat(project.getArtifacts()).hasSize(1);
    }

    @Test
    public void shouldGetMavenProjectForArtifact() throws MavenProjectFactoryException {
        when(mockArtifactHandler.getClassifier()).thenReturn(null);

        Artifact junitArtifact = new DefaultArtifact("junit", "junit", "4.12", null, "jar", null, mockArtifactHandler);
        Artifact hamcrestArtifact =
                new DefaultArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar", null, mockArtifactHandler);

        MavenProjectFactory factory = new MavenProjectFactory(container, projectBuildingRequestFactory);
        MavenProject project = factory.getMavenProject(junitArtifact);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
        assertThat(project.getArtifacts()).hasSize(1);
    }

}