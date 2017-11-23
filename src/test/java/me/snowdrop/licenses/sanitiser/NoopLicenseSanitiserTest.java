package me.snowdrop.licenses.sanitiser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import org.junit.Test;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NoopLicenseSanitiserTest {

    @Test
    public void shouldDoNothing() {
        NoopLicenseSanitiser sanitiser = new NoopLicenseSanitiser();
        DependencyElement dependencyElement = new DependencyElement("testGroupId", "testArtifactId", "testVersion",
                Collections.singleton(new LicenseElement("testLicenseName", "testLicenseUrl")));

        DependencyElement sanitisedDependencyElement = sanitiser.fix(dependencyElement);

        assertThat(sanitisedDependencyElement).isEqualTo(dependencyElement);
        assertThat(sanitisedDependencyElement.getLicenses()).isEqualTo(dependencyElement.getLicenses());
    }
}