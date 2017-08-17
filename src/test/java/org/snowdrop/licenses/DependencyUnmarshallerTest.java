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

    private DependencyUnmarshaller unmarshaller;

    private XMLInputFactory xmlInputFactory;

    @Before
    public void before() throws JAXBException {
        unmarshaller = new DependencyUnmarshaller();
        xmlInputFactory = XMLInputFactory.newFactory();
    }

    @Test
    public void shouldUnmarshallDependencyWithExtraFields() throws XMLStreamException, JAXBException {
        String dependencyString = "<dependency>"
                + "<groupId>testGroup</groupId>"
                + "<artifactId>testArtifact</artifactId>"
                + "<version>testVersion</version>"
                + "<type>testType</type>"
                + "<scope>testScope</scope>"
                + "<optional>true</optional>"
                + "<classifier>testClassifier</classifier>"
                + "<systemPath>testSystemPath</systemPath>"
                + "<exclusions>"
                + "<exclusion>"
                + "<groupId>testExclusionGroup</groupId>"
                + "<artifactId>testExclusionArtifact</artifactId>"
                + "</exclusion>"
                + "</exclusions>"
                + "</dependency>";

        StringReader stringReader = new StringReader(dependencyString);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        Dependency dependency = unmarshaller.unmarshal(xmlStreamReader);

        assertThat(dependency.getGroupId()).isEqualTo("testGroup");
        assertThat(dependency.getArtifactId()).isEqualTo("testArtifact");
        assertThat(dependency.getVersion()).isEqualTo("testVersion");
        assertThat(dependency.getType()).isEqualTo("testType");
        assertThat(dependency.getClassifier()).isEqualTo("testClassifier");
    }

    @Test
    public void shouldUnmarshallDependencyWithMissingFields() throws XMLStreamException, JAXBException {
        String dependencyString = "<dependency>"
                + "<groupId>testGroup</groupId>"
                + "<artifactId>testArtifact</artifactId>"
                + "</dependency>";

        StringReader stringReader = new StringReader(dependencyString);
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

        xmlStreamReader.next();
        Dependency dependency = unmarshaller.unmarshal(xmlStreamReader);

        assertThat(dependency.getGroupId()).isEqualTo("testGroup");
        assertThat(dependency.getArtifactId()).isEqualTo("testArtifact");
        assertThat(dependency.getVersion()).isNull();
        assertThat(dependency.getType()).isNull();
        assertThat(dependency.getClassifier()).isNull();
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