1. Set correct parent in pom.xml.
2. Paste dependencies which should be included in licenses.xml generation.
3. Execute `mvn clean package`
4. Verify that licenses.xml and licenses.html are correct (you might need to update files in `src/main/resources/templates` or pom.xml and repeat step 3).
5. Archive the licenses `zip -r licenses.zip src/main/resources/licenses`.
6. Copy the licenses.zip file to the staging server `scp licenses.zip <USER>@rcm-guest.app.eng.bos.redhat.com:/mnt/rcm-guest/staging/rhoar/<RELEASE_FOLDER>`
