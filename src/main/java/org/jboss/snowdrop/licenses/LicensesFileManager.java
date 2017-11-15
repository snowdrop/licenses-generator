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

import org.apache.commons.io.FileUtils;
import org.jboss.snowdrop.licenses.xml.DependencyElement;
import org.jboss.snowdrop.licenses.xml.LicenseElement;
import org.jboss.snowdrop.licenses.xml.LicenseSummary;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class responsible for persisting licenses information to XML and HTML files.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LicensesFileManager {

    private final Logger logger = Logger.getLogger(LicensesFileManager.class.getSimpleName());

    /**
     * Create a licenses.xml file.
     *
     * @param licenseSummary license summary XML element, which should be written to a licenses.xml file.
     * @param directoryPath  directory where new file should be stored.
     * @throws LicensesGeneratorException
     */
    public void createLicensesXml(LicenseSummary licenseSummary, String directoryPath)
            throws LicensesGeneratorException {
        File file = new File(directoryPath, "licenses.xml");
        try {
            FileUtils.writeStringToFile(file, licenseSummary.toXmlString());
        } catch (JAXBException | IOException e) {
            throw new LicensesGeneratorException("Failed to create licenses.xml", e);
        }
    }

    /**
     * Create a licenses.html file and download copy of each license for offline use.
     *
     * @param licenseSummary license summary XML element, which should be written to a licenses.xml file.
     * @param directoryPath  directory where new file should be stored.
     * @throws LicensesGeneratorException
     */
    public void createLicensesHtml(LicenseSummary licenseSummary, String directoryPath)
            throws LicensesGeneratorException {
        Map<String, String> licenseFiles = downloadLicenseFiles(licenseSummary.getDependencies(), directoryPath);

        File file = new File(directoryPath, "licenses.html");
        JtwigTemplate template = JtwigTemplate.classpathTemplate("licenses.twig");
        JtwigModel model = JtwigModel.newModel()
                .with("dependencies", licenseSummary.getDependencies())
                .with("licenseFiles", licenseFiles);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            template.render(model, fileOutputStream);
        } catch (IOException e) {
            throw new LicensesGeneratorException("Failed to create licenses.html", e);
        }
    }

    private Map<String, String> downloadLicenseFiles(List<DependencyElement> dependencies, String directoryPath) {
        return dependencies.stream()
                .map(DependencyElement::getLicenses)
                .flatMap(Set::stream)
                .map(l -> downloadLicenseFile(l, directoryPath))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<Map.Entry<String, String>> downloadLicenseFile(LicenseElement license, String directoryPath) {
        try {
            String fileName = getLocalLicenseFileName(license);
            File file = new File(directoryPath, fileName);
            if (!file.exists()) {
                URL url = new URL(license.getTextUrl());
                FileUtils.copyURLToFile(url, file);
            }
            return Optional.of(new AbstractMap.SimpleEntry<>(license.getName(), fileName));
        } catch (IOException e) {
            logger.warning(String.format("Failed to download license '%s' from '%s': %s", license.getName(),
                    license.getTextUrl(), e.getMessage()));
            return Optional.empty();
        }
    }

    private String getLocalLicenseFileName(LicenseElement licenseElement) {
        String fileName = licenseElement.getName()
                .replaceAll("[^A-Za-z0-9 ]", "");
        return "content/" + fileName.replace(" ", "+");
    }

}
