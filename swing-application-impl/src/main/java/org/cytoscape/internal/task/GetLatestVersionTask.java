package org.cytoscape.internal.task;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
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

    private String latestVersion;

    @Override
    public void run(TaskMonitor tm) throws Exception {
        tm.setTitle("Get Latest Cytoscape Version");

        // Don't throw an exception here (e.g. connection problem)! We don't want to block the UI
        // with a modal error dialog when Cytoscape is starting up, specially when this is simply checking
        // the latest version, which is not a critical task.
        try {
            Document doc = Jsoup.connect(NEWS_URL).get();
            Elements metaTags = doc.getElementsByTag("meta");

            for (Element tag : metaTags) {
                if ("latestVersion".equals(tag.attr("name"))) {
                    latestVersion = tag.attr("content");
                    break;
                }
            }
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
