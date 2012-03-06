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

import org.cytoscape.model.CyNetwork;

//import cytoscape.data.CyAttributesUtils;

//import cytoscape.data.synonyms.Aliases;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.*;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.*;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cytoscape.tableimport.internal.util.AttributeTypes;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.model.CyTable;

/**
 * Parameter object for text table <---> CyAttributes mapping.<br>
 * <p>
 *  This object will be used by all attribute readers.
 * </p>
 *
 * @since Cytoscape 2.4
 * @version 0.9
 * @author Keiichiro Ono
 *
 */
public class AttributeMappingParameters extends AbstractMappingParameters {

	
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private final int keyIndex;
	private boolean[] importFlag;
	private boolean caseSensitive;
	
	public AttributeMappingParameters(InputStream is, String fileType) {
		super(is, fileType);

		this.keyIndex = -1;
		}
	
	public AttributeMappingParameters( final List<String> delimiters,
	                                  final String listDelimiter, final int keyIndex, final String[] attrNames,
	                                  Byte[] attributeTypes, Byte[] listAttributeTypes,
	                                  boolean[] importFlag, boolean caseSensitive) throws Exception {
		this( delimiters, listDelimiter, keyIndex, attrNames, attributeTypes, listAttributeTypes, importFlag, 
				caseSensitive, 0, null);
	}
	
	public AttributeMappingParameters(final List<String> delimiters,
            final String listDelimiter, final int keyIndex,
            final String[] attrNames,
            Byte[] attributeTypes, Byte[] listAttributeTypes,
            boolean[] importFlag, Boolean caseSensitive, int startNumber, String commentChar)
throws Exception {
		
		super(delimiters, listDelimiter, attrNames, attributeTypes, importFlag, startNumber, commentChar);
		
		this.caseSensitive = caseSensitive;
		
		
		if (attrNames == null) {
			throw new Exception("attributeNames should not be null.");
		}

		/*
		 * Error check: Key column number should be smaller than actual number
		 * of columns in the text table.
		 */
		if (attrNames.length < keyIndex) {
			throw new IOException("Key is out of range.");
		}
		
		this.listAttributeTypes = listAttributeTypes;

		this.keyIndex = keyIndex;
		this.attributeNames = attrNames;
		
		/*
		 * If not specified, import everything as String attributes.
		 */
		if (attributeTypes == null) {
			this.attributeTypes = new Byte[attrNames.length];

			for (int i = 0; i < attrNames.length; i++) {
				this.attributeTypes[i] = AttributeTypes.TYPE_STRING;
			}
		} else {
			this.attributeTypes = attributeTypes;
		}

		/*
		 * If not specified, import everything.
		 */
		if (importFlag == null) {
			this.importFlag = new boolean[attrNames.length];

			for (int i = 0; i < this.importFlag.length; i++) {
				this.importFlag[i] = true;
			}
		} else {
			this.importFlag = importFlag;
		}

		
	}
	
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String[] getAttributeNames() {
		// TODO Auto-generated method stub
		return attributeNames;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Byte[] getAttributeTypes() {
		return attributeTypes;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Byte[] getListAttributeTypes() {
		return listAttributeTypes;
	}

	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getKeyIndex() {
		// TODO Auto-generated method stub
		return keyIndex;
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Boolean getCaseSensitive() {
		return caseSensitive;
	}
	
}
