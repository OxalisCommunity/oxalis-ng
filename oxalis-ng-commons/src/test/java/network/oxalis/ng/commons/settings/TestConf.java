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

package network.oxalis.ng.commons.settings;

import network.oxalis.ng.api.settings.DefaultValue;
import network.oxalis.ng.api.settings.Nullable;
import network.oxalis.ng.api.settings.Path;
import network.oxalis.ng.api.settings.Title;

/**
 * @author erlend
 */
@Title("Testing")
public enum TestConf {

    @Path("test.nullable")
    @Nullable
    WITH_NULLABLE,

    @Path("test.default")
    @DefaultValue("Test")
    WITH_DEFAULT,

    @Path("test.value")
    WITH_VALUE,

}
