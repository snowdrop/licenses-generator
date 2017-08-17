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

import org.sonatype.aether.resolution.DependencyResolutionException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Generator {

    public static void main(String... args) throws DependencyResolutionException, JAXBException, XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        StreamSource streamSource = new StreamSource("src/test/resources/test-pom.xml");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(streamSource);
        PropertiesUnmarshaller propertiesUnmarshaller = new PropertiesUnmarshaller();
        DependencyUnmarshaller dependencyUnmarshaller = new DependencyUnmarshaller();

        List<Dependency> dependencies = new ArrayList<>();
        Properties properties = new Properties();

        while (xmlStreamReader.hasNext()) {
            if (propertiesUnmarshaller.isSupportedElement(xmlStreamReader)) {
                properties = propertiesUnmarshaller.unmarshal(xmlStreamReader);
            } else if (dependencyUnmarshaller.isSupportedElement(xmlStreamReader)) {
                dependencies.add(dependencyUnmarshaller.unmarshal(xmlStreamReader));
            }
            xmlStreamReader.next();
        }

        DependencyUtils dependencyUtils = new DependencyUtils();
        List<Dependency> fixedDependencies = dependencyUtils.replaceVersionsWithProperties(dependencies, properties);
        List<Dependency> transitiveDependencies = dependencyUtils.getTransitiveDependencies(fixedDependencies);
        System.out.println(transitiveDependencies);
    }

}
