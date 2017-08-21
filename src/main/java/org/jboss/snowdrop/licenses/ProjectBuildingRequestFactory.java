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

package org.jboss.snowdrop.licenses;

import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;

import java.util.Collections;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class ProjectBuildingRequestFactory {

    private final SnowdropMavenEmbedder mavenEmbedder;

    public ProjectBuildingRequestFactory(SnowdropMavenEmbedder mavenEmbedder) {
        this.mavenEmbedder = mavenEmbedder;
    }

    public ProjectBuildingRequest getProjectBuildingRequest() throws Exception {
        DefaultProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest();
        projectBuildingRequest.setLocalRepository(mavenEmbedder.getLocalRepository());
        projectBuildingRequest.setRemoteRepositories(Collections.singletonList(
                mavenEmbedder.createRepository(
//                        "http://indy.cloud.pnc.engineering.redhat
// .com/api/group/builds-untested+shared-imports+public",
                        "http://repo1.maven.org/maven2",
                        "indy")));
        projectBuildingRequest.setResolveDependencies(true);

        projectBuildingRequest.setRepositorySession(mavenEmbedder.buildRepositorySystemSession());
        projectBuildingRequest.setSystemProperties(System.getProperties());

        projectBuildingRequest.setProcessPlugins(false);

        return projectBuildingRequest;
    }

}
