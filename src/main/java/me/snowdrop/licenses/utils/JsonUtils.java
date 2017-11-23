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
package me.snowdrop.licenses.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 9/14/17
 */
public interface JsonUtils {

    static <T> Set<T> loadJsonToSet(String resourceLocation, Function<JsonObject, T> mapper) {
        try (InputStream fileInputStream = JsonUtils.class.getClassLoader()
                .getResourceAsStream(resourceLocation)) {
            return Json.createReader(fileInputStream)
                    .readArray()
                    .stream()
                    .map(JsonValue::asJsonObject)
                    .map(mapper)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the resource: " + resourceLocation, e);
        }
    }
}
