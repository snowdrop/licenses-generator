package org.jboss.snowdrop.licenses.internal;

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenRequest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactoryTest {

    @Mock
    private ArtifactHandler mockArtifactHandler;

    private MavenEmbedder mavenEmbedder;

    private ProjectBuildingRequest projectBuildingRequest;

    private PlexusContainer container;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        mavenEmbedder = new MavenEmbedder(Thread.currentThread()
                .getContextClassLoader(), new MavenRequest());

        container = mavenEmbedder.getPlexusContainer();

        projectBuildingRequest = new DefaultProjectBuildingRequest();
        projectBuildingRequest.setLocalRepository(mavenEmbedder.getLocalRepository());
        projectBuildingRequest.setRemoteRepositories(Collections.singletonList(
                mavenEmbedder.createRepository("http://repo1.maven.org/maven2", "central")));
        projectBuildingRequest.setResolveDependencies(true);
        projectBuildingRequest.setInactiveProfileIds(Arrays.asList("restrict-doclint", "doclint-java8-disable"));
    }

    @Test
    public void shouldGetMavenProjectForDependency() throws ComponentLookupException {
        when(mockArtifactHandler.getClassifier()).thenReturn(null);

        Artifact junitArtifact = new DefaultArtifact("junit", "junit", "4.12", null, "jar", null, mockArtifactHandler);
        Artifact hamcrestArtifact =
                new DefaultArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar", null, mockArtifactHandler);

        Dependency dependency = new DependencyFactory().getDependency(junitArtifact.getGroupId(),
                junitArtifact.getArtifactId(), junitArtifact.getVersion(), junitArtifact.getType(),
                junitArtifact.getScope(), junitArtifact.getClassifier(), junitArtifact.isOptional());

        MavenProjectFactory factory = new MavenProjectFactory(container, projectBuildingRequest);
        MavenProject project = factory.getMavenProject(dependency);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
        assertThat(project.getArtifacts()).hasSize(1);
    }

    @Test
    public void shouldGetMavenProjectForArtifact() {
        when(mockArtifactHandler.getClassifier()).thenReturn(null);

        Artifact junitArtifact = new DefaultArtifact("junit", "junit", "4.12", null, "jar", null, mockArtifactHandler);
        Artifact hamcrestArtifact =
                new DefaultArtifact("org.hamcrest", "hamcrest-core", "1.3", null, "jar", null, mockArtifactHandler);

        MavenProjectFactory factory = new MavenProjectFactory(container, projectBuildingRequest);
        MavenProject project = factory.getMavenProject(junitArtifact);

        assertThat(project.getArtifact()).isEqualTo(junitArtifact);
        assertThat(project.getArtifacts()).containsOnly(hamcrestArtifact);

        assertThat(project.getLicenses()).hasSize(1);
        assertThat(project.getArtifacts()).hasSize(1);
    }

}