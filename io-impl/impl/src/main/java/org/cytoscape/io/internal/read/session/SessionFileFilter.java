/*
  File: SessionFileFilter.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.APPS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.VERSION_EXT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.MarkSupportedInputStream;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionFileFilter extends BasicCyFileFilter {

	private static final String DEFAULT_VERSION = "2.0.0";
	private static final Logger logger = LoggerFactory.getLogger(SessionFileFilter.class);
	
	private String requiredVersion;

	public SessionFileFilter(Set<String> extensions, Set<String> contentTypes, String description,
			DataCategory category, String requiredVersion, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.requiredVersion = requiredVersion;
	}

	public SessionFileFilter(String[] extensions, String[] contentTypes, String description, DataCategory category,
			String requiredVersion, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
		this.requiredVersion = requiredVersion;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (super.accepts(uri, category)) {
			String version = extractVersion(uri);
			return accepts(version);
		}

		return false;
	}

	protected String extractVersion(URI uri) {
		String version = "";
		ZipInputStream zis = null;

		try {
			// Extract list of entries until it finds the version file.
			zis = new ZipInputStream(uri.toURL().openStream());
			ZipEntry zen = null;
			String entryName = null;

			while ((zen = zis.getNextEntry()) != null) {
				entryName = zen.getName();
				InputStream tmpIs = null;

				try {
					tmpIs = new MarkSupportedInputStream(zis);
					
					if (!entryName.contains(APPS_FOLDER) && entryName.endsWith(VERSION_EXT)) {
						version = parseVersion(entryName);
						logger.debug("CYS version: " + version);
						break;
					}
				} catch (Exception e) {
					logger.warn("Failed reading session entry: " + entryName, e);
				} finally {
					if (tmpIs != null) {
						try {
							tmpIs.close();
						} catch (IOException e) {
						}
					}
					tmpIs = null;
				}

				try {
					zis.closeEntry();
				} catch (IOException e) {
				}
			}
		} catch (Exception ex) {
			logger.error("Failed reading session file: " + uri.getPath(), ex);
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
				}
			}
			zis = null;
		}
		
		return version;
	}
	
	protected String parseVersion(String entryName) {
		return entryName.replaceAll("[^/]*/", "").replace(VERSION_EXT, "");
	}

	protected boolean accepts(String version) {
		boolean accepts = true;
		
		if (version == null || version.trim().isEmpty())
			version = DEFAULT_VERSION;
		
		int majorVer = 0;
		int minorVer = 0;
		int revision = 0;
		
		try {
			String[] verArr = version.split("\\.");
			majorVer = Integer.parseInt(verArr[0]);
			
			if (verArr.length > 1)
				minorVer = Integer.parseInt(verArr[1]);
			if (verArr.length > 2)
				revision = Integer.parseInt(verArr[2]);
		} catch (Exception ex) {
			logger.warn("Cannot parse the session file's version \"" + version + "\"", ex);
		}
		
		if (requiredVersion != null) {
			String[] reqArr = requiredVersion.split("\\.");
			
			int reqMajorVer = 0;
			int reqMinorVer = 0;
			int reqRevision = 0;
			
			try {
				reqMajorVer = Integer.parseInt(reqArr[0]);
				
				if (reqMajorVer != majorVer) {
					// A different major version means that the CYS format is incompatible with the reader!
					accepts = false;
				} else {
					if (reqArr.length > 1)
						reqMinorVer = Integer.parseInt(reqArr[1]);
					
					if (reqMinorVer > minorVer) {
						accepts = false;
					} else if (reqMinorVer == minorVer) {
						if (reqArr.length > 2)
							reqRevision = Integer.parseInt(reqArr[2]);
						
						if (reqRevision > revision)
							accepts = false;
					}
				}
			} catch (Exception ex) {
				logger.warn("Cannot parse the required version \"" + requiredVersion + "\"", ex);
			}
		}

		return accepts;
	}
}
