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

package me.snowdrop.licenses.sanitiser;

import me.snowdrop.licenses.xml.LicenseElement;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RedHatLicense {

    private String name;

    private String url;

    private String textUrl;

    private Set<String> aliases;

    private Set<String> urlAliases;

    public RedHatLicense(JsonObject jsonObject) {
        this.name = jsonObject.getString("name");
        this.url = jsonObject.getString("url");
        this.textUrl = jsonObject.getString("textUrl", null);
        this.aliases = jsonObject.getJsonArray("aliases")
                .getValuesAs(JsonString.class)
                .stream()
                .map(JsonString::getString)
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());
        this.urlAliases = jsonObject.getJsonArray("urlAliases")
                .getValuesAs(JsonString.class)
                .stream()
                .map(JsonString::getString)
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public Set<String> getUrlAliases() {
        return urlAliases;
    }

    public LicenseElement toLicenseElement() {
        return new LicenseElement(name, url, textUrl);
    }

    public String getTextUrl() {
        return textUrl;
    }
}
