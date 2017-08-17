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

import com.jcabi.aether.Aether;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class DependencyUtils {

    // TODO make configurable
    private static final List<RemoteRepository> REPOSITORIES = Arrays.asList(
            new RemoteRepository("maven-central", "default", "http://repo1.maven.org/maven2/"),
            new RemoteRepository("indy", "default",
                    "http://indy.cloud.pnc.engineering.redhat.com/api/group/builds-untested+shared-imports+public"));

    private final Logger logger = Logger.getLogger(DependencyUtils.class.getSimpleName());

    public List<Dependency> replaceVersionsWithProperties(List<Dependency> dependencies, Properties properties) {
        List<Dependency> fixedDependencies = new ArrayList<>(dependencies.size());
        for (Dependency dependency : dependencies) {
            if (isPropertyString(dependency.getVersion())) {
                Dependency fixedDependency = replaceVersionWithProperty(dependency, properties);
                logger.fine(String.format("Fixed %s to %s", dependency, fixedDependency));
                fixedDependencies.add(fixedDependency);
            } else {
                logger.fine(String.format("Did not fix %s", dependency));
                fixedDependencies.add(dependency);
            }
        }
        return fixedDependencies;
    }

    public List<Dependency> getTransitiveDependencies(List<Dependency> dependencies) {
        // TODO refactor
        File local = new File("target/local-repository");
        Aether aether = new Aether(REPOSITORIES, local);

        return dependencies.stream()
                .flatMap(d -> {
                    try {
                        logger.fine(String.format("Getting transitive dependencies for %s", d));
                        return aether.resolve(d.toArtifact(), "runtime")
                                .stream();
                    } catch (DependencyResolutionException e) {
                        logger.warning(
                                String.format("Failed to get transitive dependencies for %s: %s", d, e.getMessage()));
                        return Stream.empty();
                    }
                })
                .map(Dependency::new)
                .collect(Collectors.toList());
    }

    private Dependency replaceVersionWithProperty(Dependency dependency, Properties properties) {
        String version = dependency.getVersion();
        String key = version.substring(2, version.length() - 1);
        String fixedVersion = properties.getProperty(key, version);

        return new Dependency(dependency.getGroupId(), dependency.getArtifactId(), fixedVersion, dependency.getType(),
                dependency.getClassifier());
    }

    private boolean isPropertyString(String stringToCheck) {
        return stringToCheck.startsWith("${") && stringToCheck.endsWith("}");
    }

}
