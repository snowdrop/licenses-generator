package me.snowdrop.licenses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import me.snowdrop.licenses.sanitiser.LicenseSanitiser;
import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import me.snowdrop.licenses.xml.LicenseSummary;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactoryTest {

    @Mock
    private MavenProject mockMavenProject;

    @Mock
    private License mockLicense;

    @Mock
    private LicenseSanitiser mockLicenseSanitiser;

    private LicenseSummaryFactory licenseSummaryFactory;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(mockLicenseSanitiser.fix(any())).then(a -> a.getArgument(0));

        licenseSummaryFactory = new LicenseSummaryFactory(mockLicenseSanitiser);
    }

    @Test
    public void shouldGetLicenseSummary() {
        when(mockMavenProject.getGroupId()).thenReturn("testGroupId");
        when(mockMavenProject.getArtifactId()).thenReturn("testArtifactId");
        when(mockMavenProject.getVersion()).thenReturn("testVersion");
        when(mockMavenProject.getLicenses()).thenReturn(Collections.singletonList(mockLicense));
        when(mockLicense.getName()).thenReturn("testLicenseName");
        when(mockLicense.getUrl()).thenReturn("testLicenseUrl");

        Collection<MavenProject> mavenProjects = Collections.singleton(mockMavenProject);
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