/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.snowdrop.licenses.sanitiser;

import org.jboss.snowdrop.licenses.xml.LicenseElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.jboss.snowdrop.licenses.utils.JsonUtils.loadJsonToSet;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RedHatLicenseSanitiser {

    private Set<RedHatLicense> redHatLicenses;
    private Map<String, Set<LicenseElement>> licensesForArtifact;

    public RedHatLicenseSanitiser(String redHatLicensesFile, String licenseExceptionsFile) {
        this.redHatLicenses = loadJsonToSet(redHatLicensesFile, RedHatLicense::new);
        this.licensesForArtifact = new HashMap<>();

        loadJsonToSet(licenseExceptionsFile, RedHatLicenseForArtifact::new)
                .forEach(artifact -> licensesForArtifact.put(artifact.getGav(), artifact.getLicenses()));
    }

    public Optional<Set<LicenseElement>> getLicensesForArtifact(String gav) {
        gav = stripRedhatSuffix(gav);
        Optional<Set<LicenseElement>> result = Optional.ofNullable(licensesForArtifact.get(gav));
        return result;
    }

    private String stripRedhatSuffix(String gav) {
        return gav.contains("redhat") ?
                gav.replaceFirst(".redhat.*$", "")
                : gav;
    }

    public LicenseElement fix(LicenseElement license) {
        if (license.getUrl() != null) {
            String url = license.getUrl()
                    .toLowerCase();
            Optional<RedHatLicense> optional = findRedHatLicense(l -> l.getUrlAliases()
                    .contains(url));
            if (optional.isPresent()) {
                return optional.get()
                        .toLicenseElement();
            }
        }

        if (license.getName() != null) {
            String name = license.getName()
                    .toLowerCase();
            Optional<RedHatLicense> optional = findRedHatLicense(l -> l.getAliases()
                    .contains(name));

            if (optional.isPresent()) {
                return optional.get()
                        .toLicenseElement();
            }
        }

        return license;
    }

    private Optional<RedHatLicense> findRedHatLicense(Predicate<RedHatLicense> predicate) {
        return redHatLicenses.stream()
                .filter(predicate)
                .findFirst();
    }


}
