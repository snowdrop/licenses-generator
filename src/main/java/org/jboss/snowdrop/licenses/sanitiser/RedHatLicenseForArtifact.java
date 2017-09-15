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

import javax.json.JsonObject;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/14/17
 */
public class RedHatLicenseForArtifact {
    private String gav;
    private Set<LicenseElement> licenses;

    public RedHatLicenseForArtifact(JsonObject jsonObject) {
        this.gav = jsonObject.getString("gav");
        this.licenses = jsonObject.getJsonArray("licenses")
                .getValuesAs(JsonObject.class)
                .stream()
                .map(this::licenseElementFromJsonObject)
                .collect(Collectors.toSet());
    }

    public String getGav() {
        return gav;
    }

    public Set<LicenseElement> getLicenses() {
        return licenses;
    }

    private LicenseElement licenseElementFromJsonObject(JsonObject jsonObject) {
        LicenseElement licenseElement = new LicenseElement();
        licenseElement.setName(jsonObject.getString("name"));
        licenseElement.setUrl(jsonObject.getString("url"));
        return licenseElement;
    }
}
