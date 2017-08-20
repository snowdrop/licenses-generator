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

package org.snowdrop.licenses;

import org.apache.maven.model.License;

import java.util.Objects;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class LicenseElement {

    private final String name;

    private final String url;

    public LicenseElement(License license) {
        this.name = license.getName();
        this.url = license.getUrl();
    }

    public LicenseElement(String name, String url) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(url, "url cannot be null");
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', url='%s'}", LicenseElement.class.getSimpleName(), name, url);
    }
}
