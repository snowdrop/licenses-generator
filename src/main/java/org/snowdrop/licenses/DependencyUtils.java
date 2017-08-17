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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class DependencyUtils {

    public List<Dependency> replaceVersionsWithProperties(List<Dependency> dependencies, Properties properties) {
        List<Dependency> fixedDependencies = new ArrayList<>(dependencies.size());
        for (Dependency dependency : dependencies) {
            if (isPropertyString(dependency.getVersion())) {
                fixedDependencies.add(replaceVersionWithProperty(dependency, properties));
            } else {
                fixedDependencies.add(dependency);
            }
        }
        return fixedDependencies;
    }

    private Dependency replaceVersionWithProperty(Dependency dependency, Properties properties) {
        String version = dependency.getVersion();
        String key = version.substring(2, version.length() - 1);
        String fixedVersion = properties.getProperty(key, version);

        return new Dependency(dependency.getGroupId(), dependency.getArtifactId(), fixedVersion, dependency.getScope(),
                dependency.getClassifier());
    }

    private boolean isPropertyString(String stringToCheck) {
        return stringToCheck.startsWith("${") && stringToCheck.endsWith("}");
    }

}
