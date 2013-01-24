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
