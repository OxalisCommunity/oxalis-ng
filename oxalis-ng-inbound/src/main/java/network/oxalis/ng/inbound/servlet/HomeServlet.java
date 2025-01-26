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

package network.oxalis.ng.inbound.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class HomeServlet extends HttpServlet {

    private final Config config;

    @Inject
    public HomeServlet(Config config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.print("<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "        <title>Oxalis - Next Generation Home</title>\n" +
                "        <style>" +
                "        .oxalis-container {color: black;}" +
                "        .oxalis-header {height: 120px;}" +
                "        .oxalis-content {align-items: center;width: 50%;background: rgba(244, 241, 239, .9);background-image: initial;background-position-x: initial;" +
                "        background-position-y: initial;background-size: initial;background-attachment: initial;background-origin: initial;background-clip: initial;" +
                "        background-color: rgba(244, 241, 239, 0.9);max-width: 540px;margin: 0 auto 3px;font-size: 21px;padding: 20px;}" +
		        "        </style>" +
                "    </head>\n" +
                "    <body>\n"+
                "    <div class=\"oxalis-header\">");
                if (config.hasPath("access.point.logo")) {
                    writer.print("<img src=\"" + config.getString("access.point.logo") + "\" alt=\"Destination Access Point built on Oxalis\" width=\"200\" height=\"200\">");
                }
        writer.print("<div class=\"oxalis-container\"><div class=\"oxalis-content\">");
        if (config.hasPath("access.point.name")) {
            writer.print("<h2>Welcome to '" + config.getString("access.point.name") + "' AP Home</h2><h6><i>Powered by Next Generation Oxalis (Oxalis-NG)</i></h6>");
        } else {
            writer.print("<h2>Welcome to the AP Home</h2><h6><i>Powered by Next Generation Oxalis (Oxalis-NG)</i></h6>");
        }
        writer.print("The AS4 endpoint is served at: <a href=\"as4\">here</a>");
        writer.print("<p>Important version, certificate and related information can be found at: <a href=\"as4\\status\">status</a></p>\n");
        writer.print("<p><b>NOTE:</b><i>Status information is only for debugging and internal support purpose so please consider blocking access to Status page.<i></p>\n");
        writer.print("</div></div>");
        writer.print("</body>");
        writer.print("</html>");
    }
}
