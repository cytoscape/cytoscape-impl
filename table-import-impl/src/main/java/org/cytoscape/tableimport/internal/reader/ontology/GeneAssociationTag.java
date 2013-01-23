package org.cytoscape.tableimport.internal.reader.ontology;

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


/**
 * Reserved keywords for Gene Association files.<br>
 * <p>
 * For more information about GA format, please visit:<br>
 * http://www.geneontology.org/GO.annotation.shtml#file
 * </p>
 *
 * <strong>This is for GAF v2</strong>
 * 
 */
public enum GeneAssociationTag {
	DB("DB"),
	DB_OBJECT_ID("DB_Object_ID"),
	DB_OBJECT_SYMBOL("DB_Object_Symbol"),
	QUALIFIER("Qualifier"),
	GO_ID("GO ID"),
	DB_REFERENCE("DB:Reference"),
	EVIDENCE("Evidence"),
	WITH_OR_FROM("With (or) From"),
	ASPECT("Aspect"),
	DB_OBJECT_NAME("DB_Object_Name"),
	DB_OBJECT_SYNONYM("DB_Object_Synonym"),
	DB_OBJECT_TYPE("DB_Object_Type"),
	TAXON("Taxon"),
	DATE("Date"),
	ASSIGNED_BY("Assigned_by"),
	ANNOTATION_EXTENSION("Annotation Extension"),
	GENE_PRODUCT_FROM_ID("Gene Product Form ID");

	private final String tag;

	private GeneAssociationTag(String tag) {
		this.tag = tag;
	}

	
	@Override
	public String toString() {
		return tag;
	}

	/**
	 * Since this enum represents column names, we can find the index of the tag
	 * by using this method.
	 * <br>
	 * @return
	 */
	public int getPosition() {
		final GeneAssociationTag[] tags = values();

		for (int i = 0; i < tags.length; i++) {
			if (tags[i] == this)
				return i;
		}

		return 0;
	}
}
