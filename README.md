1. Set correct parent in pom.xml.
2. Paste dependencies which should be included in licenses.xml generation.
3. If needed, download a local Maven repository.
4. Execute `mvn clean package [-Dmaven.repo.local={PATH_TO_A_LOCAL_REPO}]`
5. Verify that licenses.xml and licenses.html are correct (you might need to update files in `src/main/resources/templates` and repeat step 4).
6. Archive the licenses `zip -r licenses.zip src/main/resources/licenses`.
7. Copy the licenses.zip file to the staging server `scp licenses.zip <USER>@rcm-guest.app.eng.bos.redhat.com:/mnt/rcm-guest/staging/rhoar/<RELEASE_FOLDER>`
