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

/**
 * Storage class for flag setting.
 * 
 * @author Yassen Assenov
 */
public final class BooleanSettings extends Settings {

	/**
	 * Initializes a new instance of <code>BooleanSettings</code>.
	 * 
	 * @param aElement Node in the XML settings file that identifies general settings.
	 */
	public BooleanSettings(Element aElement) {
		super();
		name = aElement.getName();
		value = aElement.getText().equals("true");
	}

	/**
	 * Produces an exact copy of the settings instance.
	 * 
	 * @return Copy of the settings instance.
	 * @see Object#clone()
	 */
	@Override
	public Object clone() {
		return new BooleanSettings(name, value);
	}

	/**
	 * Gets the value of this flag.
	 * 
	 * @return Current value of the flag in the form of a <code>boolean</code>.
	 */
	public boolean getValue() {
		return value;
	}

	/**
	 * Sets the value of this flag.
	 * 
	 * @param aValue New value of the flag in the form of a <code>boolean</code>.
	 */
	public void setValue(boolean aValue) {
		value = aValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable#toXmlNode()
	 */
	@Override
	public Element toXmlNode() {
		return new Element(name).setText(value ? "true" : "false");
	}

	/**
	 * Initializes a new instance of <code>BooleanSettings</code>.
	 * <p>
	 * This constructor is used by the {@link #clone()} method only.
	 * </p>
	 * 
	 * @param aName Name of the tag to be used for this flag.
	 * @param aValue Value of the flag.
	 */
	private BooleanSettings(String aName, boolean aValue) {
		super();
		name = aName;
		value = aValue;
	}

	/**
	 * Name of the tag for this flag.
	 */
	private String name;

	/**
	 * Value of the flag.
	 */
	private boolean value;
}
