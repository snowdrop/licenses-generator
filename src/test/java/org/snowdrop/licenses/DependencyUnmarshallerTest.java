package org.snowdrop.licenses;


import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DependencyUnmarshallerTest {

    private static final String TEST_DEPENDENCY = "<dependency>"
            + "<groupId>testGroup</groupId>"
            + "<artifactId>testArtifact</artifactId>"
            + "<version>testVersion</version>"
            + "<scope>testScope</scope>"
            + "<classifier>testClassifier</classifier>"
            + "</dependency>";

    private DependencyUnmarshaller unmarshaller;

    private XMLInputFactory xmlInputFactory;

    @Before
    public void before() throws JAXBException {
        unmarshaller = new DependencyUnmarshaller();
        xmlInputFactory = XMLInputFactory.newFactory();
    }

    @Test
    public void shouldUnmarshallDependency() throws XMLStreamException, JAXBException {
        StringReader stringReader = new StringReader(TEST_DEPENDENCY);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        Dependency dependency = unmarshaller.unmarshal(xmlStreamReader);

        assertThat(dependency.getGroupId()).isEqualTo("testGroup");
        assertThat(dependency.getArtifactId()).isEqualTo("testArtifact");
        assertThat(dependency.getVersion()).isEqualTo("testVersion");
        assertThat(dependency.getScope()).isEqualTo("testScope");
        assertThat(dependency.getClassifier()).isEqualTo("testClassifier");
    }

    @Test
    public void shouldSupportDependencyElement() throws XMLStreamException {
        StringReader stringReader = new StringReader("<dependency></dependency>");
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