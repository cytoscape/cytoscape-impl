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
