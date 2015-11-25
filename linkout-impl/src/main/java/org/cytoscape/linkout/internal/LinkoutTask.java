package org.cytoscape.linkout.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/*
 * #%L
 * Cytoscape Linkout Impl (linkout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkoutTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(LinkoutTask.class);

	private final String link;
	private final CyIdentifiable[] tableEntries;
	private final OpenBrowser browser;
	private final CyNetwork network;

	private static final String REGEX = "%.+?%";
	private static final Pattern regexPattern = Pattern.compile(REGEX);


	public LinkoutTask(String link, OpenBrowser browser, CyNetwork network, CyIdentifiable... tableEntries) {
		this.link = link;
		this.tableEntries = tableEntries;
		this.browser = browser;
		this.network = network;
	}

	@Override
	public void run(TaskMonitor tm) {
		String url = computeUrl();
		logger.debug("LinkOut opening url: " + url);
		if(!isValidUrl(url))
			throw new RuntimeException("URL has an incorrect format: " + url);
		if(!browser.openURL(url))
			throw new RuntimeException("Problem opening linkout URL: " + url);
	}

	public String computeUrl() {
		String url = link;
		// This absurdity is to support backwards compatibility with 2.x formatted links.
		if (tableEntries.length == 1) {
			url = substituteAttributes(url, tableEntries[0], "ID");
		} else if (tableEntries.length == 2) {
			url = substituteAttributes(url, tableEntries[0], "ID1");
			url = substituteAttributes(url, tableEntries[1], "ID2");
		} else if (tableEntries.length == 3) {
			url = substituteAttributes(url, tableEntries[0], "ID1");
			url = substituteAttributes(url, tableEntries[1], "ID2");
			url = substituteAttributes(url, tableEntries[2], "ID");
		}
		return url;
	}
	
	private static boolean isValidUrl(String url) {
		try {
			new URI(url); // OpenBrowser uses URI
			return true;
		} catch(URISyntaxException e) {
			return false;
		}
	}
	
	public boolean isValidUrl() {
		return isValidUrl(computeUrl());
	}
	
	private String substituteAttributes(String url, CyIdentifiable tableEntry, String id) {
		// Replace %ATTRIBUTE.NAME% mark with the value of the attribute final
		Matcher mat = regexPattern.matcher(url);

		while (mat.find()) {
			String attrName = url.substring(mat.start() + 1, mat.end() - 1);
			String replaceName = attrName;

			// handle the default case where ID, ID1, ID2 is now the "name" column
			if (attrName.equals(id))
				attrName = CyNetwork.NAME;

			Object raw = network.getRow(tableEntry).getRaw(attrName);
			if (raw == null)
				continue;

			String attrValue = raw.toString();
			String attrValueEscaped = null;
			try {
				attrValueEscaped = URLEncoder.encode(attrValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
			url = url.replace("%" + replaceName + "%", attrValueEscaped);
			mat = regexPattern.matcher(url);
		}

		return url;
	}
}
