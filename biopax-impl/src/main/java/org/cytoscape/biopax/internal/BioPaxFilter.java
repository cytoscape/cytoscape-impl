package org.cytoscape.biopax.internal;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;


/**
 * BioPax Filer class.  Extends CyFileFilter for integration into the Cytoscape ImportHandler
 * framework.
 *
 * @author Ethan Cerami; (refactored by) Jason Montojo and Igor Rodchenkov
 */
public class BioPaxFilter extends BasicCyFileFilter {
	private static final String BIOPAX_NAMESPACE_STARTS_WITH= "http://www.biopax.org/release/biopax";

	private static final int DEFAULT_LINES_TO_CHECK = 20;

	/**
	 * Constructor.
	 */
	public BioPaxFilter(StreamUtil streamUtil) {
		super(
				new String[] { "xml", "owl", "rdf", "" }, 
				new String[] { "text/xml", "application/rdf+xml", "application/xml", "text/plain" }, 
				"BioPAX data", 
				DataCategory.NETWORK, 
				streamUtil);
	}

	/**
	 * Indicates which files the BioPaxFilter accepts.
	 * <p/>
	 * This method will return true only if:
	 * <UL>
	 * <LI>File ends in .xml or .owl;  and
	 * <LI>File headers includes the www.biopax.org namespace declaration.
	 * </UL>
	 *
	 * @param file File
	 * @return true or false.
	 */
	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (category != this.category) 
			return false;
		
		// file/stream header must contain the biopax declaration
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			int linesToCheck = DEFAULT_LINES_TO_CHECK;
			while (linesToCheck > 0) {
				String line = reader.readLine();
				if (line != null && line.contains(BIOPAX_NAMESPACE_STARTS_WITH)) {
					return true;
				}
				linesToCheck--;
			}
			return false;
		} catch (IOException e) {
		}

		return false;
	}


	@Override
	public boolean accepts(URI uri, DataCategory category) {		
		try {
			return super.accepts(uri, category) && accepts(streamUtil.getInputStream(uri.toURL()), category);
		} catch (IOException e) {
			return false;
		}
	}

}
