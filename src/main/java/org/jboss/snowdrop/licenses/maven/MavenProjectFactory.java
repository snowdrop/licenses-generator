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

package org.jboss.snowdrop.licenses.maven;

import java.io.File;
import java.util.Optional;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactory {

    private final ProjectBuilder projectBuilder;

    private final ProjectBuildingRequestFactory projectBuildingRequestFactory;

    public MavenProjectFactory(ProjectBuilder projectBuilder,
            ProjectBuildingRequestFactory projectBuildingRequestFactory) {
        this.projectBuilder = projectBuilder;
        this.projectBuildingRequestFactory = projectBuildingRequestFactory;
    }

    public Optional<MavenProject> getMavenProject(Artifact artifact, boolean resolveDependencies) {
        ProjectBuildingRequest request = projectBuildingRequestFactory.getProjectBuildingRequest();
        request.setResolveDependencies(resolveDependencies);

        try {
            ProjectBuildingResult result = projectBuilder.build(artifact, request);
            return Optional.ofNullable(result.getProject());
        } catch (ProjectBuildingException e) {
            e.printStackTrace(); // TODO add logging
            return Optional.empty();
        }
    }

    public Optional<MavenProject> getMavenProject(File pom, boolean resolveDependencies) {
        ProjectBuildingRequest request = projectBuildingRequestFactory.getProjectBuildingRequest();
        request.setResolveDependencies(resolveDependencies);

        try {
            ProjectBuildingResult result = projectBuilder.build(pom, request);
            return Optional.ofNullable(result.getProject());
        } catch (ProjectBuildingException e) {
            e.printStackTrace(); // TODO add logging
            return Optional.empty();
        }
    }

}
