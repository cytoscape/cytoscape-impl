package org.cytoscape.psi_mi.internal.plugin;

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

public class PsiMiCyFileFilter implements CyFileFilter {
	private static final String PSI_MI_XML_NAMESPACE = "net:sf:psidev:mi";

	private static final int DEFAULT_LINES_TO_CHECK = 20;

	private final StreamUtil streamUtil;
	private final Set<String> extensions;
	private final Set<String> contentTypes;
	private final String description;

	public PsiMiCyFileFilter(String description, StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
		
		extensions = new HashSet<String>();
		extensions.add("xml");
		
		contentTypes = new HashSet<String>();
		contentTypes.add("text/psi-mi");
		contentTypes.add("text/psi-mi+xml");
		
		this.description = description; 
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
		while (linesToCheck > 0) {
			String line = reader.readLine();
			if (line != null && line.contains(PSI_MI_XML_NAMESPACE)) {
				return true;
			}
			linesToCheck--;
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
