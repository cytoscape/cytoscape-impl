
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.tableimport.internal.reader;

import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Text table <---> CyAttribute & CyNetwork mapping parameters for network
 * table.
 *
 * @since Cytoscape 2.4
 * @version 0.9
 *
 * @author Keiichiro Ono
 *
 */
public class NetworkTableMappingParameters extends AbstractMappingParameters {
	private static final String DEF_INTERACTION = "pp";
	private final Integer source;
	private final Integer target;
	private final Integer interaction;
	private final String defInteraction;

	/**
	 * Creates a new NetworkTableMappingParameters object.
	 *
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param source  DOCUMENT ME!
	 * @param target  DOCUMENT ME!
	 * @param interaction  DOCUMENT ME!
	 * @param defInteraction  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public NetworkTableMappingParameters(List<String> delimiters, String listDelimiter,
	                                     String[] attributeNames, Byte[] attributeTypes,
	                                     Byte[] listAttributeTypes,
	                                     boolean[] importFlag,
	                                     Integer source, Integer target, Integer interaction,
	                                     final String defInteraction, 
	                                     int startNumber, String commentChar) throws Exception {
		
		
		super(delimiters, listDelimiter,attributeNames , attributeTypes, listAttributeTypes, importFlag, startNumber, commentChar);

		this.source = source;
		this.target = target;
		this.interaction = interaction;
		this.defInteraction = defInteraction;
	}

	public NetworkTableMappingParameters(InputStream is, String fileType) throws Exception {
		
		super(is, fileType);
		this.source = -1;
		this.target = -1;
		this.interaction = -1;
		this.defInteraction = "";
		
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getSourceIndex() {
		return source;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getTargetIndex() {
		return target;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getInteractionIndex() {
		return interaction;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getDefaultInteraction() {
		if (defInteraction == null) {
			return DEF_INTERACTION;
		} else {
			return defInteraction;
		}
	}


	

}
