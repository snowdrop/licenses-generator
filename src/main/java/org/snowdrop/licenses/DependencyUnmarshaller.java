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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DependencyUnmarshaller {

    private final JAXBContext context;

    private final Unmarshaller unmarshaller;

    public DependencyUnmarshaller() throws JAXBException {
        this.context = JAXBContext.newInstance(Dependency.class);
        this.unmarshaller = this.context.createUnmarshaller();
    }

    public Dependency unmarshal(XMLStreamReader reader) throws JAXBException {
        return unmarshaller.unmarshal(reader, Dependency.class)
                .getValue();
    }

    public boolean isSupportedElement(XMLStreamReader reader) {
        return reader.isStartElement() && "dependency".equals(reader.getLocalName());
    }

}
