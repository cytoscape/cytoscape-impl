
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

package org.cytoscape.tableimport.internal.reader.ontology;


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
