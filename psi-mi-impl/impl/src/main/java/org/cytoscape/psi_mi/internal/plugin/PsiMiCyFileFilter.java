package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Thi sis for 2.5
public class PsiMiCyFileFilter implements CyFileFilter {
	private static final String PSI_MI_XML_NAMESPACE = "net:sf:psidev:mi";
	
	private static final String PSI_MI_254_KEY1 = "psi.hupo.org/mi";
	private static final String PSI_MI_254_KEY2 = "psidev.sourceforge.net/mi";
	

	private static final int DEFAULT_LINES_TO_CHECK = 20;

	public enum PSIMIVersion {
		PXIMI10, PSIMI25;
	}
	
	private final StreamUtil streamUtil;
	private final Set<String> extensions;
	private final Set<String> contentTypes;
	private final String description;
	
	private final PSIMIVersion version;

	public PsiMiCyFileFilter(String description, StreamUtil streamUtil, final PSIMIVersion version) {
		this.streamUtil = streamUtil;
		
		extensions = new HashSet<String>();
		extensions.add("xml");
		
		contentTypes = new HashSet<String>();
		contentTypes.add("text/psi-mi");
		contentTypes.add("text/psi-mi+xml");
		
		this.description = description;
		this.version = version;
	}
	
	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		try {
			return accepts(getInputStream(uri.toURL()), category);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error while checking header", e);
			return false;
		}
	}

	private InputStream getInputStream(URL url) throws IOException {
		return streamUtil.getInputStream(url);
	}

	private boolean checkHeader(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		int linesToCheck = DEFAULT_LINES_TO_CHECK;
		while (linesToCheck-- > 0) {
			String line = reader.readLine();
			if(line == null)
				continue;
		
			if (version == PSIMIVersion.PSIMI25) {
				if ((line.contains(PSI_MI_XML_NAMESPACE) || line.contains(PSI_MI_254_KEY1) || line
						.contains(PSI_MI_254_KEY2)) && line.contains("level=\"2\""))
					return true;
			} else if(version == PSIMIVersion.PXIMI10) {
				if (line.contains(PSI_MI_XML_NAMESPACE) && line.contains("level=\"1\""))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		try {
			return checkHeader(stream);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error while checking header", e);
			return false;
		}
	}

	@Override
	public Set<String> getExtensions() {
		return extensions;
	}

	@Override
	public Set<String> getContentTypes() {
		return contentTypes;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public DataCategory getDataCategory() {
		return DataCategory.NETWORK;
	}
}
