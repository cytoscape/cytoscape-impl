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

/**
 * Group of settings used by {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram} complex
 * parameter type.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class LongHistogramGroup extends SettingsGroup {

	/**
	 * Initializes a new instance of <code>LongHistogramGroup</code>.
	 * 
	 * @param aElement Node in XML settings file for this complex parameter settings group.
	 */
	public LongHistogramGroup(Element aElement) {
		super(aElement);
		try {
			useScatter = new BooleanSettings(aElement.getChild("usescatter"));
			general = new GeneralVisSettings(aElement.getChild(GeneralVisSettings.tag));
			axes = new AxesSettings(aElement.getChild(AxesSettings.tag));
			grid = new GridSettings(aElement.getChild(GridSettings.tag));
			bars = new BarsSettings(aElement.getChild(BarsSettings.tag));
			scatter = new ScatterSettings(aElement.getChild(ScatterSettings.tag));
			filter = new LongHistogramFilterSettings(aElement.getChild(LongHistogramFilterSettings.tag));
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
		LongHistogramGroup cloned = new LongHistogramGroup(getParamID());
		cloned.useScatter = (BooleanSettings) useScatter.clone();
		cloned.general = (GeneralVisSettings) general.clone();
		cloned.axes = (AxesSettings) axes.clone();
		cloned.grid = (GridSettings) grid.clone();
		cloned.bars = (BarsSettings) bars.clone();
		cloned.scatter = (ScatterSettings) scatter.clone();
		cloned.filter = (LongHistogramFilterSettings) filter.clone();
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
		el.setAttribute("type", "LongHistogram");
		el.addContent(useScatter.toXmlNode());
		el.addContent(general.toXmlNode());
		el.addContent(axes.toXmlNode());
		el.addContent(bars.toXmlNode());
		el.addContent(scatter.toXmlNode());
		// New elements must NOT be added above this line (for backward compatibility)
		el.addContent(grid.toXmlNode());
		el.addContent(filter.toXmlNode());
		attachDecoratorsTo(el);
		return el;
	}

	/**
	 * Gets the &quot;scatter&quot; property of this settings group.
	 * <p>
	 * If the value of this property is <code>true</code>, the
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram} this settings group applies to must be
	 * visualizes by a scatter plot. If the value is <code>false</code>, a bar chart must be used
	 * </p>
	 * 
	 * @return Value of the &quot;scatter&quot; boolean property.
	 */
	public boolean useScatter() {
		return useScatter.getValue();
	}

	/**
	 * &quot;Scatter&quot; boolean property - if the data must be visualized by a bar chart (<code>false</code>)
	 * or a scatter plot (<code>true</code>).
	 */
	public BooleanSettings useScatter;

	/**
	 * General visual settings for the chart.
	 */
	public GeneralVisSettings general;

	/**
	 * Axis-related setting for the chart.
	 */
	public AxesSettings axes;

	/**
	 * Gridline-related settings for the chart.
	 */
	public GridSettings grid;

	/**
	 * Visual settings specific for the bar chart, in case such is used ({@link #useScatter()}
	 * <code>== false</code>).
	 */
	public BarsSettings bars;

	/**
	 * Visual settings specific for the scatter plot, in case such is used ({@link #useScatter()}
	 * <code>== true</code>).
	 */
	public ScatterSettings scatter;

	/**
	 * Settings for the filter assigned to this complex parameter type, an
	 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.LongHistogramFilter} instance.
	 */
	public LongHistogramFilterSettings filter;

	/**
	 * Initializes a new instance of <code>LongHistogramGroup</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link #clone()} method.
	 * </p>
	 * 
	 * @param aParamID ID of the complex parameter this settings group applies to.
	 */
	private LongHistogramGroup(String aParamID) {
		super(aParamID);
	}
}
