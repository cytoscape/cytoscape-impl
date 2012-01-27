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
package org.cytoscape.psi_mi.internal.model;

import java.util.List;


/**
 * Encapsulates a Single Interaction.
 *
 * @author Ethan Cerami
 */
public class Interaction extends AttributeBag {
	private List<Interactor> interactors;

	/**
	 * Gets All Interactors
	 *
	 * @return ArrayList of Interactors.
	 */
	public List<Interactor> getInteractors() {
		return interactors;
	}

	/**
	 * Sets All Interactors.
	 *
	 * @param interactors ArrayList of Interactors.
	 */
	public void setInteractors(List<Interactor> interactors) {
		this.interactors = interactors;
	}

	/**
	 * To String Method.
	 *
	 * @return Interaction Description.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer("Interaction: ");
		String description = this.getDescription();
		buffer.append(description);

		return buffer.toString();
	}

	/**
	 * Gets Interaction Description.
	 *
	 * @return Interaction Description.
	 */
	public String getDescription() {
		StringBuffer buffer = new StringBuffer();

		for (Interactor interactor : interactors) {
			String name = interactor.getName();
			buffer.append(" [" + name + "]");
		}

		ExternalReference[] xrefs = getExternalRefs();
		buffer.append(" [Interaction XREFs --> ");

		if ((xrefs == null) || (xrefs.length == 0)) {
			buffer.append("None");
		} else {
			for (int i = 0; i < xrefs.length; i++) {
				buffer.append(xrefs[i].toString() + " ");
			}
		}

		buffer.append("]");

		return buffer.toString();
	}
}
