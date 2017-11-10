package org.jboss.snowdrop.licenses.sanitiser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class AliasLicenseSanitiserTest {

    @Mock
    private LicenseSanitiser mockLicenseSanitiser;

    private AliasLicenseSanitiser aliasLicenseSanitiser;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        aliasLicenseSanitiser = new AliasLicenseSanitiser("rh-license-names.json", mockLicenseSanitiser);
    }

    @Test
    public void shouldFixLicenseNameWithAlias() {
        LicenseElement licenseElement = new LicenseElement("Test License Alias", "http://test-license.com");
        DependencyElement dependencyElement = new DependencyElement("", "", "", Collections.singleton(licenseElement));

        DependencyElement fixedDependencyElement = aliasLicenseSanitiser.fix(dependencyElement);

        assertThat(fixedDependencyElement).isEqualTo(dependencyElement);
        assertThat(fixedDependencyElement.getLicenses()).hasSize(1);

        Collection<LicenseElement> fixedLicenseElements = fixedDependencyElement.getLicenses();
        assertThat(fixedLicenseElements).containsOnly(
                new LicenseElement("Test License Name", "http://test-license.com"));

        verify(mockLicenseSanitiser, times(0)).fix(any());
    }

    @Test
    public void shouldFixLicenseUrlWithAlias() {
        LicenseElement licenseElement = new LicenseElement("Test License Name", "http://test-license-alias.com");
        DependencyElement dependencyElement = new DependencyElement("", "", "", Collections.singleton(licenseElement));

        DependencyElement fixedDependencyElement = aliasLicenseSanitiser.fix(dependencyElement);

        assertThat(fixedDependencyElement).isEqualTo(dependencyElement);
        assertThat(fixedDependencyElement.getLicenses()).hasSize(1);

        Collection<LicenseElement> fixedLicenseElements = fixedDependencyElement.getLicenses();
        assertThat(fixedLicenseElements).containsOnly(
                new LicenseElement("Test License Name", "http://test-license.com"));

        verify(mockLicenseSanitiser, times(0)).fix(any());
    }

    @Test
    public void shouldFixTwoLicenses() {
        List<LicenseElement> licenseElements = Arrays.asList(
                new LicenseElement("Test License Alias", "http://test-license-alias.com"),
                new LicenseElement("Test License Alias 2", "http://test-license-alias-2.com")
        );

        DependencyElement dependencyElement = new DependencyElement("", "", "", new HashSet<>(licenseElements));
        DependencyElement fixedDependencyElement = aliasLicenseSanitiser.fix(dependencyElement);

        assertThat(fixedDependencyElement).isEqualTo(dependencyElement);
        assertThat(fixedDependencyElement.getLicenses()).hasSize(2);

        Collection<LicenseElement> fixedLicenseElements = fixedDependencyElement.getLicenses();
        assertThat(fixedLicenseElements).containsOnly(
                new LicenseElement("Test License Name", "http://test-license.com"),
                new LicenseElement("Test License Name 2", "http://test-license-2.com"));

        verify(mockLicenseSanitiser, times(0)).fix(any());
    }

    @Test
    public void shouldFixOneLicenseAndDelegate() {
        when(mockLicenseSanitiser.fix(any(DependencyElement.class))).then(a -> a.getArgument(0));

        List<LicenseElement> licenseElements = Arrays.asList(
                new LicenseElement("Test License Alias", "http://test-license-alias.com"),
                new LicenseElement("Unknown name", "http://unknown.com")
        );

        DependencyElement dependencyElement = new DependencyElement("", "", "", new HashSet<>(licenseElements));
        DependencyElement fixedDependencyElement = aliasLicenseSanitiser.fix(dependencyElement);

        assertThat(fixedDependencyElement).isEqualTo(dependencyElement);
        assertThat(fixedDependencyElement.getLicenses()).hasSize(2);

        Collection<LicenseElement> fixedLicenseElements = fixedDependencyElement.getLicenses();
        assertThat(fixedLicenseElements).containsOnly(
                new LicenseElement("Test License Name", "http://test-license.com"),
                new LicenseElement("Unknown name", "http://unknown.com"));

        verify(mockLicenseSanitiser, times(1)).fix(any());
    }

    @Test
    public void shouldDelegateUnknownLicense() {
        LicenseElement licenseElement = new LicenseElement("Unknown name", "http://unknown.com");
        DependencyElement dependencyElement = new DependencyElement("", "", "", Collections.singleton(licenseElement));

        DependencyElement fixedDependencyElement = aliasLicenseSanitiser.fix(dependencyElement);

        assertThat(fixedDependencyElement).isNull();

        verify(mockLicenseSanitiser).fix(dependencyElement);
    }

}