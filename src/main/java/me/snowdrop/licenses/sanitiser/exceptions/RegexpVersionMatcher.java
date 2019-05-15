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

package me.snowdrop.licenses.sanitiser.exceptions;

import java.util.Objects;
import java.util.regex.Pattern;

class RegexpVersionMatcher implements VersionMatcher {
    private final Pattern regexp;

    RegexpVersionMatcher(String regexp) {
        this.regexp = Pattern.compile(Objects.requireNonNull(regexp, "version regexp must be set"));
    }

    @Override
    public boolean matches(String version) {
        if (version == null) {
            return false;
        }
        return this.regexp.matcher(version).matches();
    }
}
