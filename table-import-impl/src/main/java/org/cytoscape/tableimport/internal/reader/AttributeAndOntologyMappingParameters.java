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

//import cytoscape.Cytoscape;

import java.util.List;

import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.util.AttributeDataType;


/**
 *
 */
public class AttributeAndOntologyMappingParameters extends AttributeMappingParameters {
	
	private final int ontologyIndex;
	private final String ontologyName;

	public AttributeAndOntologyMappingParameters(
			final ObjectType objectType,
			final List<String> delimiters,
			final String listDelimiter,
			final int keyIndex,
			final String mappingAttribute,
			final List<Integer> aliasIndex,
			final String[] attributeNames,
			final AttributeDataType[] attributeTypes,
			final boolean[] importFlag,
			final int ontologyIndex,
			final String ontologyName
	) throws Exception {
		super(delimiters, listDelimiter, keyIndex, attributeNames, attributeTypes, importFlag);
		
		this.ontologyName = ontologyName;
		this.ontologyIndex = ontologyIndex;
	}

	public int getOntologyIndex() {
		return ontologyIndex;
	}
	
	public String getOntologyName() {
		return ontologyName;
	}
}
