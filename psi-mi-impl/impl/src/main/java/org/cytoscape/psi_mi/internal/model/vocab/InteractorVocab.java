/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.psi_mi.internal.model.vocab;


/**
 * Controlled Vocabularly for Interactors.
 *
 * @author Ethan Cerami
 */
public class InteractorVocab extends CommonVocab {
	/**
	 * Gene Name.
	 */
	public static final String GENE_NAME = "GENE_NAME";

	/**
	 * Go Term.
	 */
	public static final String GO_TERM = "GO_TERM";

	/**
	 * Organism (Species Name).
	 */
	public static final String ORGANISM_SPECIES_NAME = "ORGANISM_SPECIES";

	/**
	 * Organism (Common Name).
	 */
	public static final String ORGANISM_COMMON_NAME = "ORGANISM_COMMON_NAME";

	/**
	 * Organism (NCBI Taxonomy ID).
	 */
	public static final String ORGANISM_NCBI_TAXONOMY_ID = "ORGANISM_NCBI_TAXON_ID";

	/**
	 * Local ID.
	 */
	public static final String LOCAL_ID = "LOCAL_ID";

	/**
	 * GenBank Flat File.
	 */
	public static final String GEN_BANK_FLAT_FILE = "GEN_BANK_FLAT_FILE";

	/**
	 * Bio Java Sequence.
	 */
	public static final String BIO_JAVA_SEQUENCE_LIST = "BIO_JAVA_SEQUENCE_LIST";

	/**
	 * Full Name.
	 */
	public static final String FULL_NAME = "FULL_NAME";

	/**
	 * Sequence Data.
	 */
	public static final String SEQUENCE_DATA = "SEQUENCE_DATA";

	/**
	 * XML Document is Result Set Format.
	 */
	public static final String XML_RESULT_SET = "XML_RESULT_SET";
}
