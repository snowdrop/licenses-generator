package org.snowdrop.licenses;

import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PropertiesUnmarshallerTest {

    private static final String TEST_PROPERTIES = "<properties>"
            + "<testProperty1>testValue1</testProperty1>"
            + "<testProperty2>testValue2</testProperty2>"
            + "</properties>";

    private PropertiesUnmarshaller unmarshaller;

    private XMLInputFactory xmlInputFactory;

    @Before
    public void before() throws JAXBException {
        unmarshaller = new PropertiesUnmarshaller();
        xmlInputFactory = XMLInputFactory.newFactory();
    }

    @Test
    public void shouldUnmarshallProperties() throws XMLStreamException, JAXBException {
        StringReader stringReader = new StringReader(TEST_PROPERTIES);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        Properties properties = unmarshaller.unmarshal(xmlStreamReader);

        assertThat(properties).containsExactly(new SimpleEntry<>("testProperty1", "testValue1"),
                new SimpleEntry<>("testProperty2", "testValue2"));
    }

    @Test
    public void shouldSupportPropertiesElement() throws XMLStreamException {
        StringReader stringReader = new StringReader("<properties></properties>");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        assertThat(unmarshaller.isSupportedElement(xmlStreamReader)).isTrue();
    }

    @Test
    public void shouldNotSupportRandomElement() throws XMLStreamException {
        StringReader stringReader = new StringReader("<random></random>");
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        assertThat(unmarshaller.isSupportedElement(xmlStreamReader)).isFalse();
    }

}