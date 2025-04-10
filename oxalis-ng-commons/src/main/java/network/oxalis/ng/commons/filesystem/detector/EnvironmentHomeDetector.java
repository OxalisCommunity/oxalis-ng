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

package network.oxalis.ng.commons.filesystem.detector;

import lombok.extern.slf4j.Slf4j;
import network.oxalis.ng.api.filesystem.HomeDetector;
import network.oxalis.ng.api.util.Sort;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.util.Map;

/**
 * @author erlend
 */
@Slf4j
@Sort(3000)
@MetaInfServices
public class EnvironmentHomeDetector implements HomeDetector {

    protected static final String VARIABLE = "OXALIS_HOME";

    private Map<String, String> environment;

    public EnvironmentHomeDetector(Map<String, String> environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unused")
    public EnvironmentHomeDetector() {
        this(System.getenv());
    }

    @Override
    public File detect() {
        if (!environment.containsKey(VARIABLE))
            return null;

        String value = environment.get(VARIABLE);
        log.info("Using Oxalis folder specified as environment variable '{}' with value '{}'.",
                VARIABLE, value);
        return new File(value);
    }
}
