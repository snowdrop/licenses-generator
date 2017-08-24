# Overview
This project generates `licenses.xml` and `licenses.html` files based on a provided Maven GAV or `pom.xml`. It loads all transitive dependnencies from `dependencies` and `dependencyManagement` sections of the project's `pom.xml` and collects all their licenses. Then it aligns the license names and URLs to comply with the approved Red Hat license names and URLs. Finally, when generating `licenses.html`, it also downloads license contents for offline use.

# Standalone usage
This project can be pakaged as an uber-jar and used standalone. To create an uber-jar run the following command:
```
mvn clean package
```

Then generate `licenses.(xml|html)` based on GAV:
```
java -jar target/licenses-generator-${project.version}.jar -DgroupId={groupId} -DartifactId={artifactId}
-Dversion={version} -Ddestination={destination directory} [-Dtype={jar, pom, etc.}] [-DgeneratorProperties={path to a properties file}]
```

Or based on `pom.xml`:
java -jar target/licenses-generator-${project.version}.jar -Dpom={path to pom.xml} -Ddestination={destination directory} [-DgeneratorProperties={path to a properties file}]

# Usage in an application
You can add this project as a dependency and generate license files by using its API directly. Add the following dependency to your project's `pom.xml`:
```
<dependency>
  <groupId>org.jboss.snowdrop</groupId>
  <artifactId>licenses-generator</artifactId>
  <version>${project.version}</version>
</dependency>
```

Create an instance of `org.jboss.snowdrop.licenses.LicenseSummaryFactory` to get a summary of all the licenses for your requested GAV or `pom.xml`. Then use the generated summary with `org.jboss.snowdrop.licenses.LicenseFilesManager` to create `licenses.xml` and/or `licenses.html`.

# Configuration
Project can be configured with a `src/main/resources/generator.properties` file. However, it can be oveerriden when running in standalone mode by providing `-DgeneratorProperties={path to a properties file}` as an application argument. When using generator via API, you can provide `org.jboss.snowdrop.licenses.properties.GeneratorProperties` when creating an instance of `org.jboss.snowdrop.licenses.LicenseSummaryFactory`.

These are the available properties (you can also see them in `org.jboss.snowdrop.licenses.properties.PropertyKeys`):

Name|Description|Default value
---|---|---
repository.names | Comma separated list of repository names. Must be the same lenght as repository.urls | Maven Central
repository.urls | Comma separated list of repository URLs. Must be the same length as repository.names | http://repo1.maven.org/maven2
processPlugins | Whether plugins should be processed when resolving dependencies | false
includeOptional | Wether optional dependnecies should be processed when gathering licenses | false
excludedScopes | Comma separated list of dependency scopes which should be ignored | test,system,provided
excludedClassifiers | Comma separated list of dependency classifiers which should be ignored | tests

