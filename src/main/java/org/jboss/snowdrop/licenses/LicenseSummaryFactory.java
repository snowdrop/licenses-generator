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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.project.MavenProject;
import org.jboss.snowdrop.licenses.sanitiser.LicenseSanitiser;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;

/**
 * Class responsible for retrieving licenses information based on a provided GAV.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicenseSummaryFactory {

    private final LicenseSanitiser licenseSanitiser;

    public LicenseSummaryFactory(LicenseSanitiser licenseSanitiser) {
        this.licenseSanitiser = licenseSanitiser;
    }

    public LicenseSummary getLicenseSummary(Collection<MavenProject> mavenProjects) {
        List<DependencyElement> dependencyElements =
                mavenProjects.parallelStream()
                        .map(DependencyElement::new)
                        .map(licenseSanitiser::fix)
                        .sorted(Comparator.comparing(DependencyElement::getGroupId)
                                .thenComparing(DependencyElement::getArtifactId)
                                .thenComparing(DependencyElement::getVersion))
                        .collect(Collectors.toList());

        return new LicenseSummary(dependencyElements);
    }

}
