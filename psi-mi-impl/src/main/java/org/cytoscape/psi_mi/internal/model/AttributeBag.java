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

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.psi_mi.internal.schema.mi25.CvType;


/**
 * Encapsulates a Generic Bag of Attributes.
 *
 * @author Ethan Cerami
 */
public class AttributeBag {
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private ExternalReference[] externalRefs;
	private CvType cvType;
	private int interactionId;

	/**
	 * Gets Interaction ID;
	 * @return Interaction ID.
	 */
	public int getInteractionId() {
		return interactionId;
	}

	/**
	 * Sets Interaction ID.
	 * @param interactionId Interaction ID.
	 */
	public void setInteractionId(int interactionId) {
		this.interactionId = interactionId;
	}

	/**
	 * Gets CV Type.
	 * @return CV Type Object.
	 */
	public CvType getCvType() {
		return cvType;
	}

	/**
	 * Sets CV Type.
	 * @param cvType CV Type Object.
	 */
	public void setCvType(CvType cvType) {
		this.cvType = cvType;
	}

	/**
	 * Gets Attribute with specified key.
	 *
	 * @param key Attribute Key.
	 * @return Attribute object value.
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Gets all Attributes.
	 *
	 * @return HashMap of all attributes.
	 */
	public Map<String, Object> getAllAttributes() {
		return attributes;
	}

	/**
	 * Adds new Attribute.
	 *
	 * @param name  Attribute key.
	 * @param value Object value.
	 */
	public void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	/**
	 * Gets the External References.
	 *
	 * @return Array of External Reference objects.
	 */
	public ExternalReference[] getExternalRefs() {
		return externalRefs;
	}

	/**
	 * Sets the External References.
	 *
	 * @param externalRefs Array of External Reference objects.
	 */
	public void setExternalRefs(ExternalReference[] externalRefs) {
		this.externalRefs = externalRefs;
	}
}
