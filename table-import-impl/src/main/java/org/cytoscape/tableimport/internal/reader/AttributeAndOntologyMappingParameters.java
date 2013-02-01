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

import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;

//import org.biojava.ontology.Ontology;

import java.util.List;


/**
 *
 */
public class AttributeAndOntologyMappingParameters extends AttributeMappingParameters {
	private final int ontologyIndex;
	private final String ontologyName;

	/**
	 * Creates a new AttributeAndOntologyMappingParameters object.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param ontologyIndex  DOCUMENT ME!
	 * @param ontologyName  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public AttributeAndOntologyMappingParameters(ObjectType objectType, List<String> delimiters,
	                                             String listDelimiter, int keyIndex,
	                                             String mappingAttribute, List<Integer> aliasIndex,
	                                             String[] attributeNames, Byte[] attributeTypes,
	                                             Byte[] listAttributeTypes, boolean[] importFlag,
	                                             int ontologyIndex, final String ontologyName)
	    throws Exception {
		this(objectType, delimiters, listDelimiter, keyIndex, mappingAttribute,
			 aliasIndex, attributeNames, attributeTypes, listAttributeTypes,
			 importFlag, ontologyIndex, ontologyName, true);
	}

	/**
	 * Creates a new AttributeAndOntologyMappingParameters object.
	 * This constructor takes an additional parameter to allow case sensitivity
	 * to be specified.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param ontologyIndex  DOCUMENT ME!
	 * @param ontologyName  DOCUMENT ME!
	 * @param caseSensitive  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public AttributeAndOntologyMappingParameters(ObjectType objectType, List<String> delimiters,
	                                             String listDelimiter, int keyIndex,
	                                             String mappingAttribute, List<Integer> aliasIndex,
	                                             String[] attributeNames, Byte[] attributeTypes,
	                                             Byte[] listAttributeTypes, boolean[] importFlag,
	                                             int ontologyIndex, final String ontologyName,
												 boolean caseSensitive)
	    throws Exception {
		super( delimiters, listDelimiter, keyIndex,
		      attributeNames, attributeTypes, listAttributeTypes, importFlag, caseSensitive);
		this.ontologyName = ontologyName;
		this.ontologyIndex = ontologyIndex;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getOntologyIndex() {
		return ontologyIndex;
	}

}
