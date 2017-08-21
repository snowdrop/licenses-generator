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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MavenProjectFactory {

    private final PlexusContainer plexusContainer;

    private final ProjectBuildingRequest projectBuildingRequest;

    public MavenProjectFactory(PlexusContainer plexusContainer, ProjectBuildingRequest projectBuildingRequest) {
        this.plexusContainer = plexusContainer;
        this.projectBuildingRequest = projectBuildingRequest;
    }

    public MavenProject getMavenProject(Dependency dependency) throws MavenProjectFactoryException {
        return getMavenProject(dependencyToArtifact(dependency));
    }

    public MavenProject getMavenProject(Artifact artifact) throws MavenProjectFactoryException {
        try {
            return getProjectBuilder().build(artifact, projectBuildingRequest)
                    .getProject();
        } catch (ProjectBuildingException e) {
            throw new MavenProjectFactoryException(e);
        }
    }

    private ProjectBuilder getProjectBuilder() {
        try {
            return plexusContainer.lookup(ProjectBuilder.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to lookup ProjectBuilder", e);
        }
    }

    private ArtifactFactory getArtifactFactory() {
        try {
            return plexusContainer.lookup(ArtifactFactory.class);
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to lookup ArtifactFactory", e);
        }
    }

    private Artifact dependencyToArtifact(Dependency dependency) {
        VersionRange versionRange = VersionRange.createFromVersion(dependency.getVersion());
        return getArtifactFactory().createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope(),
                dependency.isOptional());
    }
}
