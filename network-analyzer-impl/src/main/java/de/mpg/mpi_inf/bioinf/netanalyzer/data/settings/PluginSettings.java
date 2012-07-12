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

import java.awt.Color;

import org.jdom.Element;
import org.w3c.dom.DOMException;

/**
 * Plugin's general settings.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 */
public class PluginSettings extends Settings {

	/**
	 * Initializes a new instance of <code>PluginSettings</code>.
	 * 
	 * @param aElement
	 *            Node in the XML settings file that identifies plugin general settings.
	 * @throws DOMException
	 *             When the given element is not an element node with the expected name ({@link #tag}) or when
	 *             the subtree rooted at <code>aElement</code> does not have the expected structure.
	 */
	public PluginSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Checks if expandable chart panels must be used.
	 * 
	 * @return <code>true</code> if {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.ChartExpandablePanel} must be
	 *         used as building blocks for {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.AnalysisResultPanel};
	 *         <code>false</code> otherwise.
	 */
	public boolean getExpandable() {
		return expandable;
	}

	/**
	 * Gets the help section about function fitting in NetworkAnalyzer.
	 * 
	 * @return Document name for the &quot;Fitting a Function&quot; help section. Currently this section is
	 *         named &quot;Fitting a Power Law&quot; since this is the only function NetworkAnalyzer can fit
	 *         on parameter data. Concatenating the returned <code>String</code> to the help base URL gives
	 *         the URL of the help on function fitting.
	 */
	public String getHelpFitting() {
		return helpFitting;
	}

	/**
	 * Gets the help section about interpretation of network's status.
	 * 
	 * @return Document name for the &quot;Network Interpretation&quot; help section. Concatenating this
	 *         <code>String</code> to the help base URL gives the URL of the help on network interpretations.
	 */
	public String getHelpInterpret() {
		return helpInterpret;
	}

	/**
	 * Gets the help section about network parameters.
	 * 
	 * @return Document name for the &quot;Network Parameters&quot; help section. Concatenating this
	 *         <code>String</code> to the help base URL gives the URL of the help on network parameters.
	 */
	public String getHelpParams() {
		return helpParams;
	}

	/**
	 * Gets the help section about removing duplicated edges.
	 * 
	 * @return Document name for the &quot;Remove Duplicated Edges&quot; help section. Concatenating this
	 *         <code>String</code> to the help base URL gives the URL of the help on network parameters.
	 */
	public String getHelpRemDuplicates() {
		return helpRemDuplicates;
	}

	/**
	 * Gets the help section about removing self-loops.
	 * 
	 * @return Document name for the &quot;Remove Self-Loops&quot; help section. Concatenating this
	 *         <code>String</code> to the help base URL gives the URL of the help on network parameters.
	 */
	public String getHelpRemSelfloops() {
		return helpRemSelfloops;
	}

	/**
	 * Gets the help section about plugin's settings.
	 * 
	 * @return Document name for the &quot;NetworkAnalyzer Settings&quot; help section. Concatenating this
	 *         <code>String</code> to the help base URL gives the URL of the help on network parameters.
	 */
	public String getHelpSettings() {
		return helpSettings;
	}

	/**
	 * Gets the base URL for the help documents of this plugin.
	 * <p>
	 * The help documents are in HTML format and are open automatically in a browser's window.
	 * </p>
	 * 
	 * @return <code>String</code> containing the URL for the help documents.
	 */
	public String getHelpUrlString() {
		return helpUrlString;
	}

	/**
	 * Checks if node attributes are computed and stored.
	 * 
	 * @return <code>true</code> if node attributes must be computed by the analyzers and stored in the
	 *         hashmap provided by <code>Cytoscape.data.CyAttributes</code> and <code>false</code> otherwise.
	 */
	public boolean getUseNodeAttributes() {
		return useNodeAttributes;
	}

	/**
	 * Checks if edge attributes are computed and stored.
	 * 
	 * @return <code>true</code> if edge attributes must be computed by the analyzers and stored in the
	 *         hashmap provided by <code>Cytoscape.data.CyAttributes</code> and <code>false</code> otherwise.
	 */
	public boolean getUseEdgeAttributes() {
		return useEdgeAttributes;
	}

	/**
	 * Gets the color of the background.
	 * 
	 * @return Color the background is filled with for visualizing network parameters.
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Gets the bright color.
	 * 
	 * @return Bright color to be used for visualizing network parameters.
	 */
	public Color getBrightColor() {
		return brightColor;
	}

	/**
	 * Gets the middle color.
	 * 
	 * @return Middle color to be used for visualizing network parameters.
	 */
	public Color getMiddleColor() {
		return middleColor;
	}

	/**
	 * Gets the dark color.
	 * 
	 * @return Dark color to be used for visualizing network parameters.
	 */
	public Color getDarkColor() {
		return darkColor;
	}

	/**
	 * Sets the value of the &quot;expandable&quot; flag.
	 * <p>
	 * This flag identifies if expandable chart panels must be used in the dialog presenting network analysis
	 * results.
	 * 
	 * @param aExpandable
	 *            New value of the &quot;expandable&quot; flag.
	 * @see #getExpandable()
	 */
	public void setExpandable(boolean aExpandable) {
		expandable = aExpandable;
	}

	/**
	 * Sets the base URL for the help documents.
	 * 
	 * @param aHelpUrlString
	 *            New URL of the help document.
	 * @see #getHelpUrlString()
	 */
	public void setHelpUrlString(String aHelpUrlString) {
		helpUrlString = aHelpUrlString;
	}

	/**
	 * Sets the value of the &quot;useNodeAttributes&quot; flag.
	 * <p>
	 * This flag identifies if node attributes are computed and used.
	 * </p>
	 * 
	 * @param aNodeAttribute
	 *            New value of the &quot;useNodeAttributes&quot; flag.
	 * @see #getUseNodeAttributes()
	 */
	public void setUseNodeAttributes(boolean aNodeAttribute) {
		useNodeAttributes = aNodeAttribute;
	}

	/**
	 * Sets the value of the &quot;useEdgeAttributes&quot; flag.
	 * <p>
	 * This flag identifies if edge attributes are computed and used.
	 * </p>
	 * 
	 * @param anEdgeAttribute
	 *            New value of the &quot;useEdgeAttributes&quot; flag.
	 * @see #getUseEdgeAttributes()
	 */
	public void setUseEdgeAttributes(boolean anEdgeAttribute) {
		useEdgeAttributes = anEdgeAttribute;
	}

	/**
	 * Sets the background color to be used for visualizing network parameters.
	 * 
	 * @param aBackgroundColor
	 *            New value of the background color.
	 * @see #getBackgroundColor()
	 */
	public void setBackgroundColor(Color aBackgroundColor) {
		backgroundColor = aBackgroundColor;
	}

	/**
	 * Sets the value of the bright color to be used for visualizing network parameters.
	 * 
	 * @param aBrightColor
	 *            New value of the bright color.
	 * @see #getBrightColor()
	 */
	public void setBrightColor(Color aBrightColor) {
		brightColor = aBrightColor;
	}

	/**
	 * Sets the value of the middle color to be used for visualizing network parameters.
	 * 
	 * @param aMiddleColor
	 *            New value of the middle color.
	 * @see #getMiddleColor()
	 */
	public void setMiddleColor(Color aMiddleColor) {
		middleColor = aMiddleColor;
	}

	/**
	 * Sets the value of the dark color to be used for visualizing network parameters.
	 * 
	 * @param aDarkColor
	 *            New value of the dark color.
	 * @see #getDarkColor()
	 */
	public void setDarkColor(Color aDarkColor) {
		darkColor = aDarkColor;
	}

	/**
	 * Tag name used in XML settings file to identify plugin's general settings.
	 */
	public static final String tag = "plugin";

	/**
	 * Name of the tag identifying the &quot;expandable&quot; flag.
	 */
	static final String expandableTag = "expandable";

	/**
	 * Name of the tag identifying the base URL for the online help.
	 */
	static final String helpUrlStringTag = "helpurl";

	/**
	 * Name of the tag identifying the &quot;Fitting a Function&quot; help section.
	 */
	static final String helpFittingTag = "helpfitting";

	/**
	 * Name of the tag identifying the &quot;Network Interpretation&quot; help section.
	 */
	static final String helpInterpretTag = "helpinterpret";

	/**
	 * Name of the tag identifying the &quot;Network Parameters&quot; help section.
	 */
	static final String helpParamsTag = "helpparams";

	/**
	 * Name of the tag identifying the &quot;Remove Duplicated Edges&quot; help section.
	 */
	static final String helpRemDuplicatesTag = "helpremduplicates";

	/**
	 * Name of the tag identifying the &quot;Remove Self-Loops&quot; help section.
	 */
	static final String helpRemSelfloopsTag = "helpremselfloops";

	/**
	 * Name of the tag identifying the &quot;Network Settings&quot; help section.
	 */
	static final String helpSettingsTag = "helpsettings";

	/**
	 * Name of the tag identifying the the &quot;useNodeAttributes&quot; flag.
	 */
	static final String useNodeAttributesTag = "nodeattributes";

	/**
	 * Name of the tag identifying the the &quot;useEdgeAttributes&quot; flag.
	 */
	static final String useEdgeAttributesTag = "edgeattributes";

	/**
	 * Name of the tag identifying the background color.
	 */
	static final String backgroundColorTag = "background";

	/**
	 * Name of the tag identifying the background color.
	 */
	static final String brightColorTag = "brightcolor";

	/**
	 * Name of the tag identifying the background color.
	 */
	static final String middleColorTag = "middlecolor";

	/**
	 * Name of the tag identifying the background color.
	 */
	static final String darkColorTag = "darkcolor";

	/**
	 * Flag indicating if expandable panels must be used.
	 */
	boolean expandable;

	/**
	 * Help section (document) about function fitting.
	 */
	String helpFitting;

	/**
	 * Help section (document) about interpretation of network's status.
	 */
	String helpInterpret;

	/**
	 * Help section (document) about network parameters.
	 */
	String helpParams;

	/**
	 * Help section (document) about removing duplicated edges.
	 */
	String helpRemDuplicates;

	/**
	 * Help section (document) about removing self-loops.
	 */
	String helpRemSelfloops;

	/**
	 * Help section (document) about NetworkAnalyzer's settings.
	 */
	String helpSettings;

	/**
	 * Base URL for online help.
	 */
	String helpUrlString;

	/**
	 * Flag indicating if node attributes are computed.
	 */
	boolean useNodeAttributes;

	/**
	 * Flag indicating if edge attributes are computed.
	 */
	boolean useEdgeAttributes;

	/**
	 * Color to be used for the background when visualizing network parameters.
	 */
	Color backgroundColor = new Color(245, 245, 245);


	/**
	 * Bright color to be used for visualizing of network parameters.
	 */
	Color brightColor;

	/**
	 * Middle color to be used for visualizing of network parameters.
	 */
	Color middleColor;

	/**
	 * Dark color to be used for visualizing of network parameters.
	 */
	Color darkColor;
}
