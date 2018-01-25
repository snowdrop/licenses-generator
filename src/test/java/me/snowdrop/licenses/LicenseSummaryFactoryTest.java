package me.snowdrop.licenses;

import me.snowdrop.licenses.maven.MavenProjectFactory;
import me.snowdrop.licenses.sanitiser.LicenseSanitiser;
import me.snowdrop.licenses.sanitiser.MavenSanitiser;
import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import me.snowdrop.licenses.xml.LicenseSummary;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactoryTest {

    @Mock
    private Artifact mockArtifact;
    @Mock
    private MavenProject mockMavenProject;

    @Mock
    private License mockLicense;
    @Mock
    private LicenseSanitiser mockLicenseSanitiser;
    @Mock
    private MavenProjectFactory projectFactoryMock;

    private LicenseSummaryFactory licenseSummaryFactory;
    private MavenSanitiser mavenSanitiser;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(mockLicenseSanitiser.fix(any())).then(a -> a.getArgument(0));

        mavenSanitiser = new MavenSanitiser(projectFactoryMock, mockLicenseSanitiser);
        licenseSummaryFactory = new LicenseSummaryFactory(mavenSanitiser);
    }

    @Test
    public void shouldGetLicenseSummary() {
        when(mockArtifact.getGroupId()).thenReturn("testGroupId");
        when(mockArtifact.getArtifactId()).thenReturn("testArtifactId");
        when(mockArtifact.getVersion()).thenReturn("testVersion");

        when(mockMavenProject.getGroupId()).thenReturn("testGroupId");
        when(mockMavenProject.getArtifactId()).thenReturn("testArtifactId");
        when(mockMavenProject.getVersion()).thenReturn("testVersion");
        when(mockMavenProject.getLicenses()).thenReturn(Collections.singletonList(mockLicense));

        when(projectFactoryMock.getMavenProject(any(), eq(false))).thenReturn(Optional.of(mockMavenProject));

        when(mockLicense.getName()).thenReturn("testLicenseName");
        when(mockLicense.getUrl()).thenReturn("testLicenseUrl");

        Collection<Artifact> mavenProjects = Collections.singleton(mockArtifact);
        LicenseSummary licenseSummary = licenseSummaryFactory.getLicenseSummary(mavenProjects);

        assertThat(licenseSummary).isNotNull();
        assertThat(licenseSummary.getDependencies()).hasSize(1);

        DependencyElement dependencyElement = licenseSummary.getDependencies()
                .get(0);
        assertThat(dependencyElement.getGroupId()).isEqualTo("testGroupId");
        assertThat(dependencyElement.getArtifactId()).isEqualTo("testArtifactId");
        assertThat(dependencyElement.getVersion()).isEqualTo("testVersion");
        assertThat(dependencyElement.getLicenses()).hasSize(1);

        LicenseElement licenseElement = dependencyElement.getLicenses()
                .iterator()
                .next();
        assertThat(licenseElement.getName()).isEqualTo("testLicenseName");
        assertThat(licenseElement.getUrl()).isEqualTo("testLicenseUrl");

        verify(mockLicenseSanitiser).fix(dependencyElement);
    }

}