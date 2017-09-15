package org.jboss.snowdrop.licenses.sanitiser;

import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RedHatLicenseSanitiserTest {

    private RedHatLicenseSanitiser licenseSanitiser;

    @Before
    public void before() {
        licenseSanitiser = new RedHatLicenseSanitiser("rh-license-names.json", "rh-license-exceptions.json");
    }

    @Test
    public void shouldFixWrongLicenseBaseOnAlias() {
        LicenseElement license = new LicenseElement("Test License Alias", "http://wrong-url.com");
        LicenseElement fixedLicense = licenseSanitiser.fix(license);
        assertThat(fixedLicense.getName()).isEqualTo("Test License Name");
        assertThat(fixedLicense.getUrl()).isEqualTo("http://test-license.com");
    }

    @Test
    public void shouldFixWrongLicenseBaseOnUrlAlias() {
        LicenseElement license = new LicenseElement("Unknonw Test License", "http://test-license-alias.com");
        LicenseElement fixedLicense = licenseSanitiser.fix(license);
        assertThat(fixedLicense.getName()).isEqualTo("Test License Name");
        assertThat(fixedLicense.getUrl()).isEqualTo("http://test-license.com");
    }

    @Test
    public void shouldLeaveCorrectLicenseAsIs() {
        LicenseElement license = new LicenseElement("Test License Name", "http://test-license.com");
        LicenseElement fixedLicense = licenseSanitiser.fix(license);
        assertThat(fixedLicense.getName()).isEqualTo("Test License Name");
        assertThat(fixedLicense.getUrl()).isEqualTo("http://test-license.com");
    }

    @Test
    public void shouldLeaveUnknownLicenseAsIs() {
        LicenseElement license = new LicenseElement("Unknown Test License", "http://unknown-license.com");
        LicenseElement fixedLicense = licenseSanitiser.fix(license);
        assertThat(fixedLicense.getName()).isEqualTo("Unknown Test License");
        assertThat(fixedLicense.getUrl()).isEqualTo("http://unknown-license.com");
    }

    @Test
    public void shouldProvideLicenseForExceptionalArtifact() {
        Optional<Set<LicenseElement>> maybeLicenses = licenseSanitiser.getLicensesForArtifact("org.apache.tomcat:servlet-api:6.0.41");
        assertThat(maybeLicenses).isNotEmpty();
        Set<LicenseElement> licenseElements = maybeLicenses.get();
        assertThat(licenseElements).containsExactlyInAnyOrder(
                new LicenseElement("Apache License, Version 2.0", "http://www.apache.org/licenses/LICENSE-2.0.txt"),
                new LicenseElement("Common Development And Distribution License (CDDL) Version 1.0", "http://www.opensource.org/licenses/cddl1.txt"));
    }

    @Test
    public void shouldNotProvideLicenseForUsualArtifact() {
        Optional<Set<LicenseElement>> maybeLicenses = licenseSanitiser.getLicensesForArtifact("junit:junit:4.12");
        assertThat(maybeLicenses).isEmpty();
    }

}