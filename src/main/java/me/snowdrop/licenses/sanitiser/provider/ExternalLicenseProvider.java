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
package me.snowdrop.licenses.sanitiser.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import me.snowdrop.licenses.LicensesGeneratorException;
import me.snowdrop.licenses.xml.LicenseElement;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 10/20/17
 */
public class ExternalLicenseProvider {

    private static final Logger logger = Logger.getLogger(ExternalLicenseProvider.class.getSimpleName());
    private final ResteasyClient client;
    private final String licenseServiceUrl;

    public ExternalLicenseProvider(String licenseServiceUrl) {
        this.licenseServiceUrl = licenseServiceUrl;

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
        clientBuilder = clientBuilder.connectionPoolSize(20);
        client = clientBuilder.build();
    }

    public Set<LicenseElement> getLicenses(String gav) {
        Response response = client
                .target(licenseServiceUrl)
                .queryParam("gav", gav)
                .request()
                .get();
        try {
            if (response.getStatus() != 200) {
                logger.info("Unable to get license information for " + gav);
            } else {
                String content = response.readEntity(String.class);

                Set<LicenseElement> licenses =
                        parseLicenses(content)
                                .stream()
                                .flatMap(dto -> dto.getLicenses().stream())
                                .map(ExternalLicenseDto::toLicenseElement)
                                .collect(Collectors.toSet());

                return licenses;
            }
        } catch (LicensesGeneratorException e) {
            throw new RuntimeException("Error getting license for gav: " + gav, e);
        } finally {
            try {
                response.close();
            } catch (Exception ignored) {}
        }
        return Collections.emptySet();
    }

    private List<ExternalLicensesDto> parseLicenses(String content) throws LicensesGeneratorException {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();
        CollectionType licenseList =
                typeFactory.constructCollectionType(List.class, ExternalLicensesDto.class);
        try {
            return mapper.readValue(content, licenseList);
        } catch (IOException e) {
            throw new LicensesGeneratorException("Unable to parse EAP licenses: " + content, e);
        }
    }
}
