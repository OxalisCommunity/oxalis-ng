/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.as4.inbound;

import network.oxalis.api.lang.OxalisLoadingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides access to selected Maven injected properties in the oxalis-as4-version.properties file.
 */
public class OxalisAS4Version {

    private static Properties properties;

    static {
        try (InputStream inputStream = OxalisAS4Version.class.getResourceAsStream("/oxalis-as4-version.properties")) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            throw new OxalisLoadingException(e.getMessage(), e);
        }
    }

    /**
     * The Oxalis version, taken from the POM
     */
    public static String getVersion() {
        return properties.getProperty("oxalis.version");
    }
}
