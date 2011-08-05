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
import org.w3c.dom.DOMException;

/**
 * Group of settings used by {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D} complex parameter type.
 * 
 * @author Yassen Assenov
 */
public class Points2DGroup extends SettingsGroup {

	/**
	 * Initializes a new instance of <code>Points2DGroup</code>.
	 * 
	 * @param aElement Node in XML settings file for this complex parameter settings group.
	 */
	public Points2DGroup(Element aElement) {
		super(aElement);
		try {
			general = new GeneralVisSettings(aElement.getChild(GeneralVisSettings.tag));
			axes = new AxesSettings(aElement.getChild(AxesSettings.tag));
			grid = new GridSettings(aElement.getChild(GridSettings.tag));
			scatter = new ScatterSettings(aElement.getChild(ScatterSettings.tag));
			final Element fe = aElement.getChild(IntHistogramFilterSettings.tag);
			filter = (fe != null) ? new Points2DFilterSettings(fe) : null;
		} catch (NullPointerException ex) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "");
		}
	}

	/**
	 * Produces an exact copy of this settings group instance.
	 * 
	 * @return Copy of the settings group instance.
	 * @see Object#clone()
	 */
	@Override
	public Object clone() {
		Points2DGroup cloned = new Points2DGroup(getParamID());
		cloned.general = (GeneralVisSettings) general.clone();
		cloned.axes = (AxesSettings) axes.clone();
		cloned.grid = (GridSettings) grid.clone();
		cloned.scatter = (ScatterSettings) scatter.clone();
		cloned.filter = (filter != null) ? (Points2DFilterSettings) filter.clone() : null;
		return cloned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.XMLSerializable#toXmlNode()
	 */
	public Element toXmlNode() {
		Element el = new Element(SettingsGroup.tag);
		el.setAttribute("name", getParamID());
		el.setAttribute("type", "Points2D");
		el.addContent(general.toXmlNode());
		el.addContent(axes.toXmlNode());
		el.addContent(scatter.toXmlNode());
		// New elements must NOT be added above this line (for backward compatibility)
		el.addContent(filter.toXmlNode());
		el.addContent(grid.toXmlNode());
		attachDecoratorsTo(el);
		return el;
	}

	/**
	 * General visual settings for the plot.
	 */
	public GeneralVisSettings general;

	/**
	 * Axis-related setting for the plot.
	 */
	public AxesSettings axes;

	/**
	 * Gridline-related settings for the chart.
	 */
	public GridSettings grid;

	/**
	 * Visual settings specific for the scatter plot used.
	 */
	public ScatterSettings scatter;

	/**
	 * Settings for the filter assigned to this complex parameter type, an
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.Points2DFilter} instance.
	 */
	public Points2DFilterSettings filter;

	/**
	 * Initializes a new instance of <code>Points2DGroup</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link #clone()} method.
	 * </p>
	 * 
	 * @param aParamID ID of the complex parameter this settings group applies to.
	 */
	private Points2DGroup(String aParamID) {
		super(aParamID);
	}
}
