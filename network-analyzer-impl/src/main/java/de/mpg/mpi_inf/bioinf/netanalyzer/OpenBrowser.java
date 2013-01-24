package de.mpg.mpi_inf.bioinf.netanalyzer;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 The Cytoscape Consortium
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

import java.awt.Desktop;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenBrowser {
	private static final Logger logger = LoggerFactory.getLogger(OpenBrowser.class);
	private OpenBrowser() {}
	public static void openURL(String urlString) {
		try {
			URL url = new URL(urlString);
			Desktop.getDesktop().browse(url.toURI());
		} catch (Exception e) {
			logger.warn("failed to open url: " + urlString, e);
		}
	}
}
