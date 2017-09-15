package org.jboss.snowdrop.licenses;

import org.jboss.snowdrop.licenses.maven.MavenArtifact;
import org.jboss.snowdrop.licenses.properties.GeneratorProperties;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactoryTest {

    @Test
    public void shouldGetLicenseSummaryForCoordinates() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        MavenArtifact mavenArtifact = new MavenArtifact("junit", "junit", "4.12", "jar");
        LicenseSummary dependencyContainer = factory.getLicenseSummary(mavenArtifact);
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

    @Test
    public void shouldGetLicenseSummaryForFile() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        LicenseSummary dependencyContainer = factory.getLicenseSummary("target/test-classes/test-pom.xml");
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("junit", "junit", "4.11"),
                new DependencyElement("junit", "junit", "4.12"),
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

    @Test
    public void shouldGetLicenseSummaryForFileWithoutDependencyManagement() {
        GeneratorProperties properties = new GeneratorProperties("test_properties/no-dependency-management.properties");
        LicenseSummaryFactory factory = new LicenseSummaryFactory(properties);
        LicenseSummary dependencyContainer = factory.getLicenseSummary("target/test-classes/test-pom.xml");
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("junit", "junit", "4.12"),
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

    @Test
    public void shouldGetLicenseSummaryForFileWithException() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        LicenseSummary dependencyContainer = factory.getLicenseSummary(
                "target/test-classes/test-pom-with-exceptions.xml"
        );

        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("junit", "junit", "4.12"),
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"),
                new DependencyElement("org.apache.tomcat", "servlet-api", "6.0.41"));

        DependencyElement servletApi = dependencyContainer.getDependencies()
                .stream()
                .filter(d -> d.getArtifactId().equals("servlet-api"))
                .findAny().get();
        Set<LicenseElement> licenses = servletApi.getLicenses();
        assertThat(licenses).hasSize(2);

        String name = "apache";
        Optional<LicenseElement> apacheLicense = licenseByName(licenses, name);
        assertThat(apacheLicense).isNotEmpty();
        assertThat(apacheLicense.get().getName()).isEqualTo("Apache Software License, Version 2.0");
        assertThat(apacheLicense.get().getUrl()).isEqualTo("http://www.apache.org/licenses/LICENSE-2.0");
    }

    private Optional<LicenseElement> licenseByName(Set<LicenseElement> licenses, String name) {
        return licenses.stream().filter(l -> l.getName().toLowerCase().contains(name)).findFirst();
    }
}