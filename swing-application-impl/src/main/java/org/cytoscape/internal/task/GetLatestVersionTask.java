package org.cytoscape.internal.task;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStreamReader;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.UnknownHostException;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
public class GetLatestVersionTask extends AbstractTask {

    // TODO: change the URL to new AWS instance for logging
    private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";
    // private static final String NEWS_URL = "https://www.cgl.ucsf.edu/home/scooter/version.json";

    private String latestVersion = null;
    private final CyServiceRegistrar registrar;
    private final String thisVersion;
    private static final String os = System.getProperty("os.name");
    private static final String os_version = System.getProperty("os.version");
    private static final String os_arch = System.getProperty("os.arch");
    private static final String java_version = System.getProperty("java.version");
    private final Properties props;

    public GetLatestVersionTask(final CyServiceRegistrar registrar, final String thisVersion) {
      this.registrar = registrar;
      this.thisVersion = thisVersion;
      final CyProperty<Properties> cyProps =
          registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
      props = cyProps.getProperties();
    }

    @Override
    public void run(TaskMonitor tm) throws Exception {
        tm.setTitle("Get Latest Cytoscape Version");
        String share = props.getProperty("installoptions.shareStatistics");
        if (share != null && !Boolean.parseBoolean(share))
          return;


        // Create the user agent string
        String user_agent = "Cytoscape v"+thisVersion+" Java "+java_version+" "+os+" "+os_version;

        // Don't throw an exception here (e.g. connection problem)! We don't want to block the UI
        // with a modal error dialog when Cytoscape is starting up, specially when this is simply checking
        // the latest version, which is not a critical task.
        try {
/*
 * Use this code when we switch to JSON
          CloseableHttpClient httpClient = HttpClients.custom().setUserAgent(user_agent).build();
          HttpGet httpGet = new HttpGet(NEWS_URL);
          CloseableHttpResponse response = httpClient.execute(httpGet);

          int statusCode = response.getStatusLine().getStatusCode();
          if (statusCode != 200 && statusCode != 202) {
            System.out.println("Status code not 200!");
            return;
          }
          HttpEntity entity = response.getEntity();

          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document doc = builder.parse(entity.getContent());
          NodeList metaTags = doc.getElementsByTagName("meta");

          for (int n = 0; n < metaTags.getLength(); n++) {
            if (metaTags.item(n).getNodeType() == Node.ELEMENT_NODE) {
              Element meta = (Element) metaTags.item(n);
              if (meta.getAttribute("name") == "latestVersion") {
                latestVersion = meta.getAttribute("content");
                break;
              }
            }
          }
 */

          Document doc = Jsoup.connect(NEWS_URL).userAgent(user_agent).get();
          Elements metaTags = doc.getElementsByTag("meta");

          for (Element tag : metaTags) {
            if ("latestVersion".equals(tag.attr("name"))) {
              latestVersion = tag.attr("content");
              break;
            }
          }
          System.out.println("latestVersion = "+latestVersion);
        } catch (UnknownHostException e) {
            Logger.getLogger(CyUserLog.NAME).warn("Cannot find host (please check your Internet connection).", e);
            tm.showMessage(Level.WARN, "Cannot find host (please check your Internet connection): " + e.getMessage());
        } catch (Exception e) {
            Logger.getLogger(CyUserLog.NAME).error("Unexpected error when getting latest Cytoscape version.", e);
            tm.showMessage(Level.ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
