package org.cytoscape.psi_mi.internal.model;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
