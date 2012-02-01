// $Id: TestExternalLinkUtil.java,v 1.11 2006/06/15 22:07:49 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami, Benjamin Gross.
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal;

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
	private static final String BIOPAX_XML_NAMESPACE = "www.biopax.org";

	private static final int DEFAULT_LINES_TO_CHECK = 20;

	/**
	 * Constructor.
	 */
	public BioPaxFilter(StreamUtil streamUtil) {
		super(
				new String[] { "xml", "owl", "rdf" }, 
				new String[] { "text/xml", "application/rdf+xml", "application/xml" }, 
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
				if (line != null && line.contains(BIOPAX_XML_NAMESPACE)) {
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
