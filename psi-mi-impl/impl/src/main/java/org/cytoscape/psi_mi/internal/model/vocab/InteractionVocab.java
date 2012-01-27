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
 * Controlled Vocabulary for Interactions.
 *
 * @author Ethan Cerami
 */
public class InteractionVocab extends CommonVocab {
	/**
	 * Interaction Short Name.
	 */
	public static final String INTERACTION_SHORT_NAME = "INTERACTION_SHORT_NAME";

	/**
	 * Interaction Full Name.
	 */
	public static final String INTERACTION_FULL_NAME = "INTERACTION_FULL_NAME";

	/**
	 * Experimental System Name.
	 */
	public static final String EXPERIMENTAL_SYSTEM_NAME = "EXPERIMENTAL_SYSTEM_NAME";

    /**
    * Interaction Type Name.
    */
    public static final String INTERACTION_TYPE_NAME = "INTERACTION_TYPE_NAME";

    /**
	 * Experimental System XRef Database.
	 */
	public static final String EXPERIMENTAL_SYSTEM_XREF_DB = "EXPERIMENTAL_SYSTEM_XREF_DB";

	/**
	 * Experimental System XRef Database.
	 */
	public static final String EXPERIMENTAL_SYSTEM_XREF_ID = "EXPERIMENTAL_SYSTEM_XREF_ID";

	/**
	 * Interaction Direction.
	 */
	public static final String DIRECTION = "DIRECTION";

	/**
	 * Interaction Owner.
	 */
	public static final String OWNER = "OWNER";

	/**
	 * Interaction PubMed ID
	 */
	public static final String PUB_MED_ID = "PUB_MED_ID";

	/**
	 * Bait Map.
	 */
	public static final String BAIT_MAP = "BAIT_MAP";

	/**
	 * Interaction Type
	 */
	public static final String INTERACTION_TYPE = "interaction";
}
