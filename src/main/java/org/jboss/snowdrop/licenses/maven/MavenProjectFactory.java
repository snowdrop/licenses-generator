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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.io.File;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactory {

    private final PlexusContainer container;

    private final ProjectBuildingRequestFactory requestFactory;

    private final ArtifactFactory artifactFactory;

    public MavenProjectFactory(PlexusContainer container,
                               ProjectBuildingRequestFactory requestFactory,
                               ArtifactFactory artifactFactory) {
        this.container = container;
        this.requestFactory = requestFactory;
        this.artifactFactory = artifactFactory;
    }

    public MavenProject getMavenProject(String pomFilePath) throws MavenProjectFactoryException {
        ProjectBuildingRequest request = requestFactory.getProjectBuildingRequest();

        try {
            ProjectBuildingResult result = getProjectBuilder().build(new File(pomFilePath), request);
            return result.getProject();
        } catch (ProjectBuildingException e) {
            throw new MavenProjectFactoryException(e);
        }
    }

    public MavenProject getMavenProject(Artifact artifact) throws MavenProjectFactoryException {
        return getMavenProject(artifact, true);
    }

    public MavenProject getMavenProject(Artifact artifact, boolean resolveDependencies)
            throws MavenProjectFactoryException {
        ProjectBuildingRequest request = requestFactory.getProjectBuildingRequest();
        request.setResolveDependencies(resolveDependencies);

        try {
            ProjectBuildingResult result = getProjectBuilder().build(artifact, request);
            return result.getProject();
        } catch (ProjectBuildingException e) {
            throw new MavenProjectFactoryException(e);
        }
    }


    public MavenProject getMavenProject(MavenArtifact mavenArtifact, boolean resolveDependencies)
            throws MavenProjectFactoryException {
        Artifact artifact = artifactFactory.createArtifact(
                mavenArtifact.getGroupId(),
                mavenArtifact.getArtifactId(),
                mavenArtifact.getVersion(),
                "compile",
                mavenArtifact.getType());
        return getMavenProject(artifact, resolveDependencies);
    }

    private ProjectBuilder getProjectBuilder() {
        try {
            return container.lookup(ProjectBuilder.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to lookup ProjectBuilder", e);
        }
    }
}
