package org.snowdrop.licenses;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactoryTest {

    @Test
    public void shouldGetLicenseSummary() {
        LicenseSummaryFactory factory = new LicenseSummaryFactory();
        LicenseSummary dependencyContainer = factory.getLicenseSummary("junit", "junit", "4.12");
        assertThat(dependencyContainer.getDependencies()).containsOnly(
                new DependencyElement("org.hamcrest", "hamcrest-core", "1.3"));
    }

}