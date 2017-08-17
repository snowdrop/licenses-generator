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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PropertiesUnmarshaller {

    private final JAXBContext context;

    private final Unmarshaller unmarshaller;

    public PropertiesUnmarshaller() throws JAXBException {
        this(JAXBContext.newInstance(Property.class));
    }

    public PropertiesUnmarshaller(JAXBContext context) throws JAXBException {
        this(context, context.createUnmarshaller());
    }

    public PropertiesUnmarshaller(JAXBContext context, Unmarshaller unmarshaller) {
        this.context = context;
        this.unmarshaller = unmarshaller;
    }

    public List<Property> unmarshal(XMLStreamReader reader) throws JAXBException, XMLStreamException {
        List<Property> properties = new ArrayList<>();

        if (!reader.hasNext() || !reader.isStartElement() || !"properties".equals(reader.getLocalName())) {
            return properties;
        }

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.END_ELEMENT && "properties".equals(reader.getLocalName())) {
                break;
            }

            if (reader.isStartElement()) {
                String key = reader.getLocalName();
                if (reader.next() == XMLStreamConstants.CHARACTERS) {
                    String value = reader.getText();
                    properties.add(new Property(key, value));
                }
            }
        }

        return properties;
    }

    public boolean isSupportedElement(XMLStreamReader reader) {
        return reader.isStartElement() && "properties".equals(reader.getLocalName());
    }

}
