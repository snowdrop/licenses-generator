package org.snowdrop.licenses;

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenRequest;
import org.apache.maven.model.License;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicensesCollectorTest {

    private MavenProjectFactory factory;

    private PlexusContainer container;

    private ProjectBuildingRequest projectBuildingRequest;

    @Before
    public void before() throws Exception {
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread()
                .getContextClassLoader(), new MavenRequest());

        container = mavenEmbedder.getPlexusContainer();

        projectBuildingRequest = new DefaultProjectBuildingRequest();
        projectBuildingRequest.setLocalRepository(mavenEmbedder.getLocalRepository());
        projectBuildingRequest.setRemoteRepositories(Collections.singletonList(
                mavenEmbedder.createRepository("http://repo1.maven.org/maven2", "central")));
        projectBuildingRequest.setResolveDependencies(true);
        projectBuildingRequest.setInactiveProfileIds(Arrays.asList("restrict-doclint", "doclint-java8-disable"));

        factory = new MavenProjectFactory(container, projectBuildingRequest);
    }

    @Test
    public void shouldGetAllLicenses() throws Exception {
        ProjectBuilder builder = container.lookup(ProjectBuilder.class);
        MavenProject project = builder.build(new File("src/test/resources/test-pom.xml"), projectBuildingRequest)
                .getProject();

        LicensesCollector collector = new LicensesCollector(factory);
        Map<Dependency, List<License>> licenses = collector.getLicenses(project);

        assertThat(licenses).containsOnlyKeys(new Dependency("test", "test", "1.0"),
                new Dependency("junit", "junit", "4.11"), new Dependency("junit", "junit", "4.12"),
                new Dependency("org.hamcrest", "hamcrest-core", "1.3"));
    }

}