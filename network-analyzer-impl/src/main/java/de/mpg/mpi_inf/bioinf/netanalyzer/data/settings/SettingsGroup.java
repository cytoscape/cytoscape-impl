package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import org.jdom.Element;
import org.w3c.dom.DOMException;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Decorators;
import de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator;

/**
 * Base class for the group of settings for a complex parameter.
 * 
 * @author Yassen Assenov
 */
public abstract class SettingsGroup
	implements XMLSerializable {

	/**
	 * Tag name used in XML settings file to identify complex parameter settings group.
	 */
	public static final String tag = "complexparam";

	/**
	 * Gets the complex parameter ID for this settings group.
	 * 
	 * @return ID of the complex parameter this settings group is applied to.
	 */
	public String getParamID() {
		return paramID;
	}

	/**
	 * Initializes the fields of a <code>SettingsGroup</code> instance.
	 * 
	 * @param aElement Node in the XML settings file that identifies complex parameter settings
	 *        group.
	 * @throws DOMException When the given element is not an element node with the expected
	 *         attributes.
	 */
	protected SettingsGroup(Element aElement) {
		paramID = aElement.getAttributeValue("name");
		if (paramID == null) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "");
		}
		Element decorators = aElement.getChild("decorators");
		if (decorators != null) {
			Decorators.set(paramID, decorators);
		}
	}

	/**
	 * Initializes the fields of a <code>SettingsGroup</code> instance.
	 * 
	 * @param aParamID ID of the complex parameter this settings group is applied to.
	 */
	protected SettingsGroup(String aParamID) {
		paramID = aParamID;
	}

	/**
	 * Attaches nodes describing the decorators for the complex parameter this setting group is
	 * applied to.
	 * <p>
	 * If no decorators are defined for this complex parameter, calling this method has no effect.
	 * </p>
	 * 
	 * @param aNode Root XML node for this setting group, to which the decorators are to be
	 *        attached.
	 */
	protected void attachDecoratorsTo(Element aNode) {
		Decorator[] decs = Decorators.get(paramID);
		if (decs != null) {
			Element decsEl = new Element("decorators");
			for (int i = 0; i < decs.length; ++i) {
				decsEl.addContent(decs[i].toXmlNode());
			}
			aNode.addContent(decsEl);
		}
	}

	/**
	 * ID of the complex parameter this settings group is applied to.
	 */
	private String paramID;
}
