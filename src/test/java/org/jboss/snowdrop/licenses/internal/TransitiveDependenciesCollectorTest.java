package org.jboss.snowdrop.licenses.internal;

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

    private TransitiveDependenciesCollector collector;

    @Before
    public void before() throws Exception {
        ApplicationProperties applicationProperties = new ApplicationProperties();
        SnowdropMavenEmbedder embedder = new MavenEmbedderFactory(applicationProperties).getSnowdropMavenEmbedder();
        ProjectBuildingRequest request = new ProjectBuildingRequestFactory(applicationProperties, embedder)
                .getProjectBuildingRequest();
        factory = new MavenProjectFactory(embedder.getPlexusContainer(), request);
        collector = new TransitiveDependenciesCollector(applicationProperties, factory);
    }

    @Test
    public void shouldGetAllRequiredArtifacts() throws MavenProjectFactoryException {
        Dependency dependency = new DependencyFactory().getDependency("junit", "junit", "4.12");
        MavenProject project = factory.getMavenProject(dependency);
        Set<MavenProject> transitiveMavenProjects = collector.getTransitiveMavenProjects(project);
        assertThat(transitiveMavenProjects).hasSize(2);
    }

}