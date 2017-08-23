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

import javax.json.Json;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RedHatLicenseSanitiser {

    private Set<RedHatLicense> redHatLicenses;

    public RedHatLicenseSanitiser(String redHatLicensesFile) {
        this.redHatLicenses = loadRedHatLicenses(redHatLicensesFile);
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

    private Set<RedHatLicense> loadRedHatLicenses(String redHatLicensesFile) {
        try (InputStream fileInputStream = getClass().getClassLoader()
                .getResourceAsStream(redHatLicensesFile)) {
            return Json.createReader(fileInputStream)
                    .readArray()
                    .stream()
                    .map(JsonValue::asJsonObject)
                    .map(RedHatLicense::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Red Hat licenses file", e);
        }
    }

}
