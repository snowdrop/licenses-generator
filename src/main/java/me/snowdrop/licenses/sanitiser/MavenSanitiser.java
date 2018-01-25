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
package me.snowdrop.licenses.sanitiser;

import me.snowdrop.licenses.maven.MavenProjectFactory;
import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import org.apache.maven.project.MavenProject;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 1/25/18
 */
public class MavenSanitiser implements LicenseSanitiser {

    private final MavenProjectFactory mavenProjectFactory;
    private final LicenseSanitiser next;

    public MavenSanitiser(MavenProjectFactory mavenProjectFactory, LicenseSanitiser next) {
        this.mavenProjectFactory = mavenProjectFactory;
        this.next = next;
    }
    
    @Override
    public DependencyElement fix(DependencyElement dependencyElement) {
        DependencyElement result;
        Set<LicenseElement> licenses = dependencyElement.getLicenses();
        if (licenses == null || licenses.isEmpty()) {
            Optional<MavenProject> mavenProject =
                    mavenProjectFactory.getMavenProject(dependencyElement.getArtifact(), false);
            Set<LicenseElement> licensesFromPom = mavenProject
                    .orElseThrow(() ->
                            new RuntimeException("Unable to find licenses neither through maven or previous sanitisers for " + dependencyElement))
                    .getLicenses()
                    .stream()
                    .map(LicenseElement::new)
                    .collect(Collectors.toSet());

            result = new DependencyElement(dependencyElement, licensesFromPom);
        } else {
            result = dependencyElement;
        }

        return next.fix(result);
    }
}
