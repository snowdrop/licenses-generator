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

import org.apache.maven.model.Dependency;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class DependencyFactory {

    public Dependency getDependency(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }

    public Dependency getDependency(String groupId, String artifactId, String version, String type) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        return dependency;
    }

    public Dependency getDependency(String groupId, String artifactId, String version, String type, String scope,
            String classifier, boolean optional) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        dependency.setScope(scope);
        dependency.setClassifier(classifier);
        dependency.setOptional(optional);
        return dependency;
    }

}
