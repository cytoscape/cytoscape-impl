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


/**
 * Encapsulates data for a single Interactor.
 *
 * @author Ethan Cerami
 */
public class Interactor extends AttributeBag {
	private String name;
	private String description;

	/**
	 * Gets Interactor Name.
	 *
	 * @return Interactor Name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets Interactor Name.
	 *
	 * @param name Interactor Name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets Interactor Description.
	 *
	 * @return Interactor Description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets Interactor Description.
	 *
	 * @param description Interactor Descriptor.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Provides Interactor Name.
	 *
	 * @return Interactor String Name.
	 */
	public String toString() {
		return "Interactor:  " + this.getName();
	}
}
