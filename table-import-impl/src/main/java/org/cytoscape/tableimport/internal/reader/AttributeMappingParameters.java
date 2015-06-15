package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cytoscape.tableimport.internal.util.AttributeDataType;

/**
 * Parameter object for text table <---> CyAttributes mapping.
 * This object will be used by all attribute readers.
 */
public class AttributeMappingParameters extends AbstractMappingParameters {

	private final int keyIndex;
	
	public AttributeMappingParameters(InputStream is, String fileType) {
		super(is, fileType);

		this.keyIndex = -1;
	}
	
	public AttributeMappingParameters(
			final List<String> delimiters,
			final String listDelimiter,
			final int keyIndex,
			final String[] attrNames,
			AttributeDataType[] attributeTypes,
	        boolean[] importFlag
	) throws Exception {
		this(delimiters, listDelimiter, keyIndex, attrNames, attributeTypes, importFlag, 0, null);
	}
	
	public AttributeMappingParameters(
			final List<String> delimiters,
            final String listDelimiter,
            final int keyIndex,
            final String[] attrNames,
            AttributeDataType[] attributeTypes,
            boolean[] importFlag,
            int startNumber,
            String commentChar
    ) throws Exception {
		super(delimiters, listDelimiter, attrNames, attributeTypes, importFlag, startNumber, commentChar);
		
		if (attrNames == null)
			throw new Exception("attributeNames should not be null.");

		/*
		 * Error check: Key column number should be smaller than actual number
		 * of columns in the text table.
		 */
		if (attrNames.length < keyIndex)
			throw new IOException("Key is out of range.");
		
		this.keyIndex = keyIndex;
		
		/*
		 * If not specified, import everything as String attributes.
		 */
		if (attributeTypes == null) {
			this.attributeTypes = new AttributeDataType[attrNames.length];

			for (int i = 0; i < attrNames.length; i++) {
				this.attributeTypes[i] = AttributeDataType.TYPE_STRING;
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
	
	public int getKeyIndex() {
		return keyIndex;
	}
}
