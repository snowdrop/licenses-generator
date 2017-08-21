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

package org.jboss.snowdrop.licenses.internal;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ProjectBuildingRequestFactory {

    private final ApplicationProperties applicationProperties;

    private final SnowdropMavenEmbedder mavenEmbedder;

    public ProjectBuildingRequestFactory(ApplicationProperties applicationProperties,
            SnowdropMavenEmbedder mavenEmbedder) {
        this.applicationProperties = applicationProperties;
        this.mavenEmbedder = mavenEmbedder;
    }

    public ProjectBuildingRequest getProjectBuildingRequest() {
        try {
            DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
            projectBuildingRequest.setLocalRepository(mavenEmbedder.getLocalRepository());
            projectBuildingRequest.setRemoteRepositories(getRepositories());
            projectBuildingRequest.setResolveDependencies(true);
            projectBuildingRequest.setRepositorySession(mavenEmbedder.buildRepositorySystemSession());
            projectBuildingRequest.setSystemProperties(System.getProperties());
            projectBuildingRequest.setProcessPlugins(applicationProperties.isProcessPlugins());

            return projectBuildingRequest;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create project building request", e);
        }
    }

    private List<ArtifactRepository> getRepositories() {
        return applicationProperties.getRepositories()
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return mavenEmbedder.createRepository(entry.getValue(), entry.getKey());
                    } catch (ComponentLookupException e) {
                        throw new RuntimeException("Failed to initialise repository", e);
                    }
                })
                .collect(Collectors.toList());
    }

}
