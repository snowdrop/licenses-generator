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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ApplicationProperties {

    private final Configuration configuration;

    public ApplicationProperties() {
        try {
            configuration = new Configurations().properties("application.properties");
        } catch (ConfigurationException e) {
            throw new RuntimeException("Couldn't load application properties", e);
        }
    }

    public Map<String, String> getRepositories() {
        String name = configuration.getString("repository.name", "Maven Central");
        String url = configuration.getString("repository.url", "http://repo1.maven.org/maven2");
        return Collections.singletonMap(name, url);
    }

    public boolean isProcessPlugins() {
        return configuration.getBoolean("processPlugins", false);
    }

    public List<String> getExcludedScopes() {
        return Arrays.asList("test", "system", "provided"); // TODO
    }

    public List<String> getExcludedClassifiers() {
        return Collections.singletonList("tests"); // TODO
    }

    public boolean isIncludeOptional() {
        return configuration.getBoolean("includeOptional", false);
    }

}
