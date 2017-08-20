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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DependencyElement {

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final Set<LicenseElement> licenses;

    public DependencyElement(String groupId, String artifactId, String version, Set<LicenseElement> licenses) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.licenses = new HashSet<>(licenses);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public Set<LicenseElement> getLicenses() {
        return Collections.unmodifiableSet(licenses);
    }
}
