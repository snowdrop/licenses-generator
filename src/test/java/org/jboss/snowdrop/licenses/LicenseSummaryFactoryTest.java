package org.jboss.snowdrop.licenses;

import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactoryTest {

    @Test
    public void shouldGetLicenseSummaryForCoordinates() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        LicenseSummary dependencyContainer = factory.getLicenseSummary("junit", "junit", "4.12");
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

    @Test
    public void shouldGetLicenseSummaryForFile() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        LicenseSummary dependencyContainer = factory.getLicenseSummary("target/test-classes/test-pom.xml");
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("junit", "junit", "4.12"),
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

}