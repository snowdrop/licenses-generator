package org.snowdrop.licenses;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TransitiveDependenciesCollectorTest {

    private MavenProjectFactory factory;

    @Before
    public void before() throws Exception {
        SnowdropMavenEmbedder embedder = new MavenEmbedderFactory().getSnowdropMavenEmbedder();
        ProjectBuildingRequest request = new ProjectBuildingRequestFactory(embedder).getProjectBuildingRequest();
        factory = new MavenProjectFactory(embedder.getPlexusContainer(), request);
    }

    @Test
    public void shouldGetAllRequiredArtifacts() {
        Dependency dependency = new DependencyFactory().getDependency("junit", "junit", "4.12");
        MavenProject project = factory.getMavenProject(dependency);
        TransitiveDependenciesCollector collector = new TransitiveDependenciesCollector(factory);
        Set<MavenProject> transitiveMavenProjects = collector.getTransitiveMavenProjects(project);
        assertThat(transitiveMavenProjects).hasSize(2);
    }

}