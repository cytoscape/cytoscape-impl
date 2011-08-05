/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

import org.jdom.Element;

/**
 * Interface identifying the ability of a class to store its state in an XML tree using the JDOM
 * library.
 * <p>
 * A class implementing this interface is recommended to have a constructor with parameters
 * {@link #constructorParams}.
 * </p>
 * 
 * @author Yassen Assenov
 */
public interface XMLSerializable {

	/**
	 * Parameters of a constructor of class that implements this interface. This identifies a
	 * constructor with a single parameter of type <code>Element</code>.
	 * <p>
	 * Every class that implements this interface should by able to create instances given an XML
	 * tree as returned by the {@link #toXmlNode()} method. This field is used when classes that
	 * implement this interface are initialized through reflection.
	 * </p>
	 */
	public static final Class<?>[] constructorParams = new Class<?>[] { Element.class };

	/**
	 * Saves the state of the class instance to an XML tree.
	 * <p>
	 * The resulting tree can be used to create identical copies of this instance.
	 * </p>
	 * 
	 * @return Root of the created XML tree.
	 */
	public Element toXmlNode();
}
