package de.mpg.mpi_inf.bioinf.netanalyzer.dec;

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

import javax.swing.JDialog;

import org.jfree.chart.JFreeChart;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ComplexParamVisualizer;

/**
 * Base class for all decorators in NetworkAnalyzer.
 * 
 * @author Yassen Assenov
 */
public abstract class Decorator implements Cloneable, XMLSerializable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract Object clone();

	/**
	 * Adds a decoration to the specified chart, or updates the current one.
	 * 
	 * @param aOwner Analysis dialog instance which uses this decorator.
	 * @param aChart Chart to be decorated.
	 * @param aVisualizer Visualizer that that created the chart.
	 * @param aVerbose Flag indicating if the decorator must be run in verbose mode - asking the
	 *        user user to specify parameters and/or informing the user about the results.
	 */
	public abstract void decorate(JDialog aOwner, JFreeChart aChart, ComplexParamVisualizer aVisualizer, boolean aVerbose);

	/**
	 * Removes the decoration added to the specified chart.
	 * <p>
	 * If the chart was not decorated by this decorator, calling this method has no effect.
	 * </p>
	 * 
	 * @param aChart Chart to be cleared of the decoration previously added.
	 */
	public abstract void undecorate(JFreeChart aChart);

	/**
	 * Gets the label of the decorator's button.
	 * <p>
	 * The return value of this method typically depends on the state of the decorator, as returned
	 * by the {@link #isActive()} method.
	 * </p>
	 * 
	 * @return Label for the button in the form of a <code>String</code> instance.
	 */
	public abstract String getButtonLabel();

	/**
	 * Gets the tooltip for the decorator's button.
	 * <p>
	 * The return value of this method typically depends on the state of the decorator, as returned
	 * by the {@link #isActive()} method.
	 * </p>
	 * 
	 * @return Tooltip for the button in the form of a <code>String</code> instance;
	 *         <code>null</code> if no tooltip is to be displayed.
	 */
	public abstract String getButtonToolTip();

	/**
	 * Checks if the decorator is currently active.
	 * 
	 * @return <code>true</code> if the decorator is active; <code>false</code> otherwise.
	 */
	public abstract boolean isActive();
}
