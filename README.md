1. Set correct parent in pom.xml.
2. Paste dependencies which should be included to the licenses.xml generation.
3. Execute `mvn clean package`
4. Verify that licenses.xml and licenses.html are correct (you might need to update files in `src/main/resources/templates` or pom.xml and repeat step 3).
5. Go to the resources directory `cd src/main/resources/`.
6. Archive the licenses `zip -r rhoar-spring-boot-<RELEASE_VERSION>-license.zip licenses`.
7. Copy the license archive file to the staging server `scp rhoar-spring-boot-<RELEASE_VERSION>-license.zip <USER>@rcm-guest.app.eng.bos.redhat.com:/mnt/rcm-guest/staging/rhoar/spring-boot/<RELEASE_FOLDER>`
8. Delete licenses archive `rm rhoar-spring-boot-<RELEASE_VERSION>-license.zip`
