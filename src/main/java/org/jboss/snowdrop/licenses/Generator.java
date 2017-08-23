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

package org.jboss.snowdrop.licenses;

import org.jboss.snowdrop.licenses.xml.LicenseSummary;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Generator {

    public static void main(String... args) throws Exception {
        LicenseSummaryFactory licenseSummaryFactory = new LicenseSummaryFactory();
        LicenseFilesManager licenseFilesManager = new LicenseFilesManager();
        // To test with *-redhat dependencies you need to change repository in application.properties
//        LicenseSummary licenseSummary =
//                licenseSummaryFactory.getLicenseSummary("org.jboss.snowdrop", "spring-boot-1.4-bom", "2.0.0.redhat-28");
        LicenseSummary licenseSummary = licenseSummaryFactory.getLicenseSummary("junit", "junit", "4.12");
        licenseFilesManager.createLicensesXml(licenseSummary, "target/licenses");
        licenseFilesManager.createLicensesHtml(licenseSummary, "target/licenses");
    }

}
