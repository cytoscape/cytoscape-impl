package org.cytoscape.tableimport.internal.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;
import org.cytoscape.tableimport.internal.util.TypeUtil;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
			final String name,
			final List<String> delimiters,
			final String[] listDelimiters,
			final int keyIndex,
			final String[] attrNames,
			final AttributeDataType[] dataTypes,
	        final SourceColumnSemantic[] types,
	        final String[] namespaces
	) throws Exception {
		this(name, delimiters, listDelimiters, keyIndex, attrNames, dataTypes, types, namespaces, 0, null);
	}
	
	public AttributeMappingParameters(
			final String name,
			final List<String> delimiters,
            final String[] listDelimiters,
            final int keyIndex,
            final String[] attrNames,
            final AttributeDataType[] dataTypes,
            final SourceColumnSemantic[] types,
            final String[] namespaces,
            final int startNumber,
            final String commentChar
    ) throws Exception {
		super(name, delimiters, listDelimiters, attrNames, dataTypes, types, namespaces, startNumber, commentChar);
		
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
		if (dataTypes == null) {
			this.dataTypes = new AttributeDataType[attrNames.length];
			Arrays.fill(this.dataTypes, AttributeDataType.TYPE_STRING);
		} else {
			this.dataTypes = dataTypes;
		}

		/*
		 * If not specified, import everything.
		 */
		if (types == null) {
			this.types = new SourceColumnSemantic[attrNames.length];
			Arrays.fill(this.types, SourceColumnSemantic.ATTR);
		} else {
			this.types = types;
		}
		
		/*
		 * If namespaces were not specified, use the preferred ones
		 */
		if (namespaces == null)
			this.namespaces = TypeUtil.getPreferredNamespaces(this.types);
		else
			this.namespaces = namespaces;
	}
	
	public int getKeyIndex() {
		return keyIndex;
	}
}
