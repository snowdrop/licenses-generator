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

package me.snowdrop.licenses;

import me.snowdrop.licenses.xml.DependencyElement;
import me.snowdrop.licenses.xml.LicenseElement;
import me.snowdrop.licenses.xml.LicenseSummary;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final int DOWNLOAD_TIMEOUT = 60_000;
    private static final int CONNECTION_TIMEOUT = 20_000;

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
                downloadTo(license, file);
            }
            return Optional.of(new AbstractMap.SimpleEntry<>(license.getName(), fileName));
        } catch (IOException e) {
            logger.warning(String.format("Failed to download license '%s' from '%s': %s", license.getName(),
                    license.getTextUrl(), e.getMessage()));
            return Optional.empty();
        }
    }

    private void downloadTo(LicenseElement license, File file) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);

        HttpGet httpget = new HttpGet(license.getTextUrl());
        HttpResponse response = httpClient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try (OutputStream stream = new FileOutputStream(file)) {
                entity.writeTo(stream);
            }
        }
    }

    private String getLocalLicenseFileName(LicenseElement licenseElement) {
        String fileName = licenseElement.getName()
                .replaceAll("[^A-Za-z0-9 ]", "");
        return "content/" + fileName.replace(" ", "+");
    }

}
