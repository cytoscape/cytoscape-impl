package de.mpg.mpi_inf.bioinf.netanalyzer.data;

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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable;
import de.mpg.mpi_inf.bioinf.netanalyzer.dec.Decorator;

/**
 * Storage class for all available chart decorators.
 * 
 * @author Yassen Assenov
 */
public abstract class Decorators {

	/**
	 * Tag name used in XML settings file to identify decorators definition.
	 */
	public static final String tag = "decorators";

	/**
	 * Gets the decorators for the specified complex parameter.
	 * 
	 * @param aParamID ID of complex parameter to get the decorators for.
	 * @return Array of <code>Decorator</code> instances for the given complex parameter;
	 *         <code>null</code> if no decorators were registered for it.
	 */
	public static Decorator[] get(String aParamID) {
		Decorator[] decs = decorators.get(aParamID);
		if (decs != null) {
			final int decCount = decs.length;
			Decorator[] decsCopy = new Decorator[decCount];
			for (int i = 0; i < decCount; ++i) {
				decsCopy[i] = (Decorator) decs[i].clone();
			}
			return decsCopy;
		}
		return null; 
	}

	/**
	 * Registers the decorators defined in XML file for the specified complex parameter.
	 * <p>
	 * The children of the given XML node define all decorators for the specified complex parameter.
	 * </p>
	 * 
	 * @param aParamID ID of complex parameter to receive decorators.
	 * @param aElement Node in XML settings file defining the decorators.
	 */
	public static void set(String aParamID, Element aElement) {
		List<?> children = aElement.getChildren();
		final int size = children.size();
		Decorator[] decs = new Decorator[size];
		Iterator<?> it = children.iterator();
		for (int i = 0; i < size; ++i) {
			try {
				Element el = (Element) it.next();
				Class<?> decClass = Plugin.getDecoratorClass(el.getName());
				Constructor<?> constr = decClass.getConstructor(XMLSerializable.constructorParams);
				decs[i] = (Decorator) constr.newInstance(new Object[] { el });
			} catch (SecurityException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new InnerException(ex);
			}
		}
		set(aParamID, decs);
	}

	/**
	 * Registers the given decorators for the specified complex parameter.
	 * <p>
	 * Registering an array of decorators overrides all decorators, if any, previously registered
	 * for this complex parameter.
	 * </p>
	 * 
	 * @param aParamID ID of complex parameter to receive decorators.
	 * @param aDecs Array of decorators for the given complex parameter. Setting this parameter to
	 *        <code>null</code> effectively erases all registered decorators.
	 */
	private static void set(String aParamID, Decorator[] aDecs) {
		decorators.put(aParamID, aDecs);
	}

	/**
	 * Map of all registered decorators.
	 */
	private static Map<String, Decorator[]> decorators = new HashMap<String, Decorator[]>();
}
