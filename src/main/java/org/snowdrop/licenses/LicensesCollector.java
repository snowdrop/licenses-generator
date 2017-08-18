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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicensesCollector {

    private final MavenProjectFactory factory;

    public LicensesCollector(MavenProjectFactory factory) {
        this.factory = factory;
    }

    public Map<Dependency, List<License>> getLicenses(MavenProject project) {
        Map<Dependency, List<License>> licenses = new HashMap<>();
        licenses.put(new Dependency(project.getArtifact()), project.getLicenses());

        for (Artifact artifact : project.getArtifacts()) {
            MavenProject artifactProject = factory.getMavenProject(artifact);
            licenses.putAll(getLicenses(artifactProject));
        }

        if (project.getDependencyManagement() != null) {
            for (org.apache.maven.model.Dependency dependency : project.getDependencyManagement().getDependencies()) {
                Dependency localDependency = new Dependency(dependency.getGroupId(), dependency.getArtifactId(),
                        dependency.getVersion(), dependency.getType(), dependency.getScope(), dependency.getClassifier());
                MavenProject dependencyProject = factory.getMavenProject(localDependency);
                licenses.putAll(getLicenses(dependencyProject));
            }
        }

        return licenses;
    }
}
