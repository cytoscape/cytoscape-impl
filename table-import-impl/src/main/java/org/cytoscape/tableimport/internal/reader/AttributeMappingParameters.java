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

import org.cytoscape.tableimport.internal.util.AttributeTypes;

/**
 * Parameter object for text table <---> CyAttributes mapping.<br>
 * <p>
 *  This object will be used by all attribute readers.
 * </p>
 *
 * @since Cytoscape 2.4
 * @version 0.9
 * @author Keiichiro Ono
 */
public class AttributeMappingParameters extends AbstractMappingParameters {

	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private final int keyIndex;
	private boolean[] importFlag;
	
	public AttributeMappingParameters(InputStream is, String fileType) {
		super(is, fileType);

		this.keyIndex = -1;
	}
	
	public AttributeMappingParameters(
			final List<String> delimiters,
			final String listDelimiter,
			final int keyIndex,
			final String[] attrNames,
	        Byte[] attributeTypes,
	        Byte[] listAttributeTypes,
	        boolean[] importFlag
	) throws Exception {
		this(delimiters, listDelimiter, keyIndex, attrNames, attributeTypes, listAttributeTypes, importFlag, 
				0, null);
	}
	
	public AttributeMappingParameters(
			final List<String> delimiters,
            final String listDelimiter,
            final int keyIndex,
            final String[] attrNames,
            Byte[] attributeTypes,
            Byte[] listAttributeTypes,
            boolean[] importFlag,
            int startNumber,
            String commentChar
    ) throws Exception {
		super(delimiters, listDelimiter, attrNames, attributeTypes, listAttributeTypes, importFlag, startNumber,
				commentChar);
		
		if (attrNames == null)
			throw new Exception("attributeNames should not be null.");

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
	
	@Override
	public String[] getAttributeNames() {
		return attributeNames;
	}

	@Override
	public Byte[] getAttributeTypes() {
		return attributeTypes;
	}

	@Override
	public Byte[] getListAttributeTypes() {
		return listAttributeTypes;
	}

	public int getKeyIndex() {
		return keyIndex;
	}
}
