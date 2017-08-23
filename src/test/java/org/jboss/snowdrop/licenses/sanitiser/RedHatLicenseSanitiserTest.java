package org.jboss.snowdrop.licenses.sanitiser;

import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RedHatLicenseSanitiserTest {

    private RedHatLicenseSanitiser licenseSanitiser;

    @Before
    public void before() {
        licenseSanitiser = new RedHatLicenseSanitiser("rh-license-names.json");
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

}