package org.snowdrop.licenses;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DependencyUtilsTest {

    @Test
    public void shouldReplaceDependencyVersionWithPropertyValue() {
        Dependency dependency =
                new Dependency("testGroup", "testArtifact", "${test.version}", "testScope", "testClassifier");
        List<Dependency> dependencies = Collections.singletonList(dependency);
        Properties properties = new Properties();
        properties.put("test.version", "1");

        DependencyUtils utils = new DependencyUtils();
        List<Dependency> fixedDependencies = utils.replaceVersionsWithProperties(dependencies, properties);

        Dependency expectedDependency = new Dependency("testGroup", "testArtifact", "1", "testScope", "testClassifier");

        assertThat(fixedDependencies).containsExactly(expectedDependency);
    }

    @Test
    public void shouldNotReplaceDependencyVersionWithUnknownPropertyValue() {
        Dependency dependency = new Dependency("testGroup", "testArtifact", "${test.version}", "testScope",
                "testClassifier");
        List<Dependency> dependencies = Collections.singletonList(dependency);
        Properties properties = new Properties();
        properties.put("unknown.version", "1");

        DependencyUtils utils = new DependencyUtils();
        List<Dependency> fixedDependencies = utils.replaceVersionsWithProperties(dependencies, properties);

        assertThat(fixedDependencies).containsExactly(dependency);
    }

}