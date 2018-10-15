package me.snowdrop.licenses;

import me.snowdrop.licenses.maven.MavenProjectFactoryException;
import me.snowdrop.licenses.properties.GeneratorProperties;
import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import me.snowdrop.licenses.xml.LicenseSummary;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.License;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 11/15/17
 */
public class ExternalLicensesTest {
    private static final String artifactWithLicenseInEap = "eap-known:artifact:1.0-redhat-1";
    private static final String artifactWithLicenseInFile = "eap-unknown:artifact:2.0.0-redhat-1";
    private static final String artifactWithLicenseInFileInCommunityVersion = "eap-unknown:artifact:1.0-redhat-1";
    public static final String APACHE_SOFTWARE_LICENSE_VERSION_2_0_NAME = "Apache Software License, Version 2.0";
    public static final String APACHE_SOFTWARE_LICENSE_VERSION_2_0_JSON = "{ \n" +
            "  \"name\": \"" + APACHE_SOFTWARE_LICENSE_VERSION_2_0_NAME + "\",\n" +
            "  \"url\": \"http://example.com/apache-license-url\",\n" +
            "  \"license_text_url\": \"http://example.com/apache-license/content.txt\"\n" +
            "}";
    public static final String ECLIPSE_LICENSE_NAME = "Eclipse Public License 1.0";
    public static final String ECLIPSE_LICENSE_LINK = "http://www.eclipse.org/legal/epl-v10.html";

    private static String licenseServiceUrl;
    private static LicenseServiceMock licenseServiceMock;

    private LicenseSummaryFactory summaryFactory;

    @BeforeClass
    public static void setUp() {
        licenseServiceMock = new LicenseServiceMock();
        licenseServiceMock.addLicenses(artifactWithLicenseInEap, APACHE_SOFTWARE_LICENSE_VERSION_2_0_JSON);
        licenseServiceUrl = licenseServiceMock.start("/licensecheck");
    }

    @Before
    public void before() throws LicensesGeneratorException {
        GeneratorProperties properties = mock(GeneratorProperties.class);

        when(properties.getLicenseServiceUrl()).thenReturn(Optional.of(licenseServiceUrl));
        when(properties.getAliasesFilePath()).thenReturn("rh-license-names.json");
        when(properties.getExceptionsFilePath()).thenReturn("rh-license-exceptions.json");

        LicensesGenerator generator = new LicensesGenerator(properties);

        summaryFactory = generator.createLicenseSummaryFactory();
    }

    @AfterClass
    public static void tearDown() {
        licenseServiceMock.stop();
    }

    @Test
    public void shouldGetFromService() throws MavenProjectFactoryException {
        Set<LicenseElement> licenses = getLicensesForGav(artifactWithLicenseInEap);
        assertThat(licenses).hasSize(1);

        LicenseElement actual = licenses.iterator().next();
        assertThat(actual.getName()).isEqualTo(APACHE_SOFTWARE_LICENSE_VERSION_2_0_NAME);
        assertThat(actual.getUrl()).isEqualTo("http://example.com/apache-license-url");
        assertThat(actual.getTextUrl()).isEqualTo("http://example.com/apache-license/content.txt");

    }

    @Test
    public void shouldGetFromFileIfNotAvailableViaService() throws MavenProjectFactoryException {
        Set<LicenseElement> licenseElements = getLicensesForGav(artifactWithLicenseInFile);
        assertThat(licenseElements).containsExactlyInAnyOrder(
                new LicenseElement("Test License A", "http://test-license-a.com", "http://test-license-a.com")
        );
    }

    @Test
    @Ignore("Not implemented, a nice-to have feature")
    public void shouldGetFromFileForCommunityVersionIfNotAvailableViaService() throws MavenProjectFactoryException {
        Set<LicenseElement> licenseElements = getLicensesForGav(artifactWithLicenseInFileInCommunityVersion);
        assertThat(licenseElements).containsExactlyInAnyOrder(
                new LicenseElement("Test License B", "http://test-license-b.com", "http://test-license-b.com")
        );
    }

    @Test
    public void shouldGetFromMavenIfNotAvailableInFileOrService() throws MavenProjectFactoryException {
        Set<LicenseElement> licenses = getLicensesForGav("junit:junit:4.12");
        assertThat(licenses).containsExactlyInAnyOrder(
                new LicenseElement(ECLIPSE_LICENSE_NAME, ECLIPSE_LICENSE_LINK, ECLIPSE_LICENSE_LINK)
        );
    }

    private Set<LicenseElement> getLicensesForGav(String gav) throws MavenProjectFactoryException {
        LicenseSummary licenseSummary = summaryFactory.getLicenseSummary(singleton(artifact(gav)));
        assertThat(licenseSummary.getDependencies()).hasSize(1);

        DependencyElement element = licenseSummary.getDependencies().get(0);
        return element.getLicenses();
    }

    private Artifact artifact(String gavAsString) throws MavenProjectFactoryException {
        String gav[] = gavAsString.split(":");
        return new DefaultArtifact(gav[0], gav[1], gav[2],
                "compile", "jar", "", new DefaultArtifactHandler());
    }

    private License mavenLicense(String name, String url) {
        License license = new License();
        license.setName(name);
        license.setUrl(url);
        return license;
    }
}
