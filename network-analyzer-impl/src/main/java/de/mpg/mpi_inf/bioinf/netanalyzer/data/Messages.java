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

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Storage class for human-readable messages.
 * 
 * @author Yassen Assenov
 * @author Mario Albrecht
 * @author Sven-Eric Schelhorn
 * @author Nadezhda Doncheva
 */
public abstract class Messages {

	/**
	 * Simple parameter names in the form of a hash map, the keys being the IDs and the values - the textual
	 * <code>String</code>s in human readable form.
	 */
	private static final Map<String, String> simpleParams;

	/**
	 * Node attributes names for parameters computed by NetworkAnalyzer for directed network interpretations.
	 */
	private static final Set<String> dirNodeAttributes;

	/**
	 * Node attributes names for parameters computed by NetworkAnalyzer for undirected network
	 * interpretations.
	 */
	private static final Set<String> undirNodeAttributes;

	/**
	 * Node attribute names in the form of a hash map, the keys being the IDs and the values - the textual
	 * <code>String</code>s in human readable form.
	 */
	private static final Map<String, String> nodeAttributes;

	/**
	 * Edge attribute names in the form of a hash map, the keys being the IDs and the values - the textual
	 * <code>String</code>s in human readable form.
	 */
	private static final Map<String, String> edgeAttributes;

	static {
		simpleParams = new HashMap<String, String>(16);
		simpleParams.put("time", "Analysis time (sec)");
		simpleParams.put("nodeCount", "Number of nodes");
		simpleParams.put("edgeCount", "Number of edges");
		simpleParams.put("density", "Network density");
		simpleParams.put("heterogeneity", "Network heterogeneity");
		simpleParams.put("centralization", "Network centralization");
		simpleParams.put("avNeighbors", "Avg. number of neighbors");
		simpleParams.put("ncc", "Connected components");
		simpleParams.put("connPairs", "Shortest paths");
		simpleParams.put("diameter", "Network diameter");
		simpleParams.put("radius", "Network radius");
		simpleParams.put("avSpl", "Characteristic path length");
		simpleParams.put("cc", "Clustering coefficient");
		simpleParams.put("nsl", "Number of self-loops");
		simpleParams.put("mnp", "Multi-edge node pairs");
		simpleParams.put("usn", "Isolated nodes");

		nodeAttributes = new HashMap<String, String>(32);
		nodeAttributes.put("spl", "Eccentricity");
		nodeAttributes.put("cco", "ClusteringCoefficient");
		nodeAttributes.put("tco", "TopologicalCoefficient");
		nodeAttributes.put("apl", "AverageShortestPathLength");
		nodeAttributes.put("clc", "ClosenessCentrality");
		nodeAttributes.put("isn", "IsSingleNode");
		nodeAttributes.put("nco", "NeighborhoodConnectivity");
		nodeAttributes.put("nde", "NumberOfDirectedEdges");
		nodeAttributes.put("nue", "NumberOfUndirectedEdges");
		nodeAttributes.put("slo", "SelfLoops");
		nodeAttributes.put("deg", "Degree");
		nodeAttributes.put("pmn", "PartnerOfMultiEdgedNodePairs");
		nodeAttributes.put("din", "Indegree");
		nodeAttributes.put("dou", "Outdegree");
		nodeAttributes.put("dal", "EdgeCount");
		nodeAttributes.put("nbt", "BetweennessCentrality");
		nodeAttributes.put("rad", "Radiality");
		nodeAttributes.put("stress", "Stress");

		dirNodeAttributes = new HashSet<String>(16);
		dirNodeAttributes.add("Eccentricity");
		dirNodeAttributes.add("AverageShortestPathLength");
		dirNodeAttributes.add("ClosenessCentrality");
		dirNodeAttributes.add("ClusteringCoefficient");
		dirNodeAttributes.add("Indegree");
		dirNodeAttributes.add("Outdegree");
		dirNodeAttributes.add("EdgeCount");
		dirNodeAttributes.add("IsSingleNode");
		dirNodeAttributes.add("SelfLoops");
		dirNodeAttributes.add("PartnerOfMultiEdgedNodePairs");
		dirNodeAttributes.add("NeighborhoodConnectivity");
		dirNodeAttributes.add("BetweennessCentrality");
		dirNodeAttributes.add("Stress");

		undirNodeAttributes = new HashSet<String>(16);
		undirNodeAttributes.add("Degree");
		undirNodeAttributes.add("NeighborhoodConnectivity");
		undirNodeAttributes.add("ClusteringCoefficient");
		undirNodeAttributes.add("TopologicalCoefficient");
		undirNodeAttributes.add("Eccentricity");
		undirNodeAttributes.add("AverageShortestPathLength");
		undirNodeAttributes.add("ClosenessCentrality");
		undirNodeAttributes.add("BetweennessCentrality");
		undirNodeAttributes.add("Stress");
		undirNodeAttributes.add("Radiality");
		undirNodeAttributes.add("SelfLoops");
		undirNodeAttributes.add("IsSingleNode");
		undirNodeAttributes.add("NumberOfUndirectedEdges");
		undirNodeAttributes.add("NumberOfDirectedEdges");
		undirNodeAttributes.add("PartnerOfMultiEdgedNodePairs");

		edgeAttributes = new HashMap<String, String>(2);
		edgeAttributes.put("ebt", "EdgeBetweenness");
		edgeAttributes.put("dpe", "NumberOfUnderlyingEdges");
	}

	// Dialog titles

	public static String DT_ABOUT = "About NetworkAnalyzer";

	public static String DT_ANALYZING = "Analyzing network - please wait...";

	public static String DT_ANALYSIS = "Network Analysis of ";

	public static String DT_ANALYSISNEEDED = "NetworkAnalyzer - Analysis";

	public static String DT_BATCHANALYSIS = "Batch Analysis";

	public static String DT_BATCHRESULTS = "Batch Analysis - Results";

	public static String DT_BATCHSETTINGS = "Batch Analysis - Select Directories";

	public static String DT_CLOSEWARN = "Warning - Unsaved Data";

	public static String DT_COMPNETWORKS = "Compare Networks";

	public static String DT_CONNCOMP = "Connected Components";

	public static String DT_DIRECTED = " (directed)";

	public static String DT_FILEEXISTS = "Warning - File Exists";

	public static String DT_FILTERDATA = "Filter Displayed Data";

	public static String DT_FIT = "NetworkAnalyzer - Fitting Function";

	public static String DT_FITTED = "NetworkAnalyzer - Fitted Function";

	public static String DT_GUIERROR = "NetworkAnalyzer - Error";

	public static String DT_INFO = "NetworkAnalyzer - Information";

	public static String DT_IOERROR = "NetworkAnalyzer - Error";

	public static String DT_INTERPRETATION = "NetworkAnalyzer - Network Interpretation";

	public static String DT_MAPPARAM = "NetworkAnalyzer - Visualize Parameters";

	public static String DT_PLOTPARAM = "NetworkAnalyzer - Plot Parameters";

	public static String DT_REMDUPEDGES = "Remove Duplicated Edges";

	public static String DT_REMOVEFILTER = "Restore Range";

	public static String DT_REMSELFLOOPS = "Remove Self-Loops";

	public static String DT_SAVECHART = "Save Chart to File";

	public static String DT_SECERROR = "NetworkAnalyzer - Error";

	public static String DT_SETTINGS = "NetworkAnalyzer Settings";

	public static String DT_UNDIRECTED = " (undirected)";

	public static String DT_WRONGDATA = "NetworkAnalyzer - Error";

	// Short informative messages to the user

	public static String SM_AMBIGUOUSFTYPE = constructLabel("File type not specified.",
			"When giving file name, please also select one of the supported file types.");

	public static String SM_ANALYSISC = "Analysis cancelled.";

	public static String SM_ATTRIBUTESNOTSAVED = "  ERROR: Could not save node attributes to a file.";

	public static String SM_BADINPUT = constructLabel("Selected input directory is not acceptable.",
			"Please make sure you have selected an existing non-empty directory.");

	public static String SM_BADOUTPUT = constructLabel("Selected output directory is not acceptable.",
			"Please make sure you have selected an existing empty directory<br>"
					+ "for which NetworkAnalyzer has write permissions.");

	public static String SM_CHOOSEINTERPR = "Please choose an interpretation for the edges.";

	public static String SM_CLOSEWARN = "<html>You have not saved the network statistics. " +
			"They will be lost when you close this window." +
			"<br /><br />Are you sure you want to close the window?<br /><br />" +
			"(Note: Statistics will be saved as normal table data, not the NetworkAnalyzer property file)</html>";

	public static String SM_CONNECTED = " is connected, i.e. has a single connected component.";

	public static String SM_CREATEVIEW = constructLabel("No nodes are selected.",
			"Please create a network view and select nodes.");

	public static String SM_DEFFAILED = "An I/O error occurred while saving the settings as default.";

	public static String SM_DONE = "done";

	public static String SM_FILEEXISTS = "<html>The specified file already exists.<br>Overwrite?";

	public static String SM_FITLINE = "<html>A line in the form y = a + b x was fitted.</html>";

	public static String SM_FITLINEERROR = "Could not fit line to the points.";

	public static String SM_FITLINENODATA = "There are not enough data points to fit a line.";

	public static String SM_FITNONPOSITIVE = "<html>Some data points have non-positive coordinates.<br>Only points with positive coordinates are included in the fit.</html>";

	public static String SM_FITPL = "<html>A power law of the form y = ax<sup>b</sup> was fitted.</html>";

	public static String SM_FITPLERROR = "Could not fit power law to the points.";

	public static String SM_FITPLNODATA = "There are not enough data points to fit a power law.";

	public static String SM_GUIERROR = "An error occurred while initializing the window.";

	public static String SM_IERROR = "An error occurred while opening or reading from the file.";

	public static String SM_NOINPUTFILES = "No network files found in the specified input directory.";

	public static String SM_INTERNALERROR = "Internal error occurred during computation.";

	public static String SM_LOADING = "Loading ";

	public static String SM_LOADNET = "Please load a network first.";

	public static String SM_LOADPARAMETERS = constructLabel(
			"Storing node and edge parameters as attributes is disabled.",
			"You need to compute node or edge attributes.<br>"
					+ "Do you want to enable storing parameters and run NetworkAnalyzer on the selected network?");

	public static String SM_LOADSETTINGSFAIL1 = "NetworkAnalyzer: Loading settings from ";

	public static String SM_LOADSETTINGSFAIL2 = " failed.";

	public static String SM_LOGERROR = "NetworkAnalyzer - Internal Error";

	public static String SM_NETWORKEMPTY = "Network contains no nodes.";

	public static String SM_NETMODIFICATION = "<html>Please note that this option effectively modifies the selected network(s)<br>"
			+ "and the operations performed cannot be undone.</html>";

	public static String SM_NETWORKFILEINVALID = "Network file is invalid.";

	public static String SM_NETWORKNOTOPENED = "Could not load network from file.";

	public static String SM_OERROR = "An error occurred while creating or writing to the file.";

	public static String SM_OUTPUTIOERROR = "Could not save network statistics file.";

	public static String SM_OUTPUTNOTCREATED = "Could not write to output directory.";

	public static String SM_READERROR = "\n  ERROR: Could not create network from network file!\n";

	public static String SM_REMDUPEDGES = " duplicated edge(s) removed from ";

	public static String SM_REMOVEFILTER = "Do you want to restore the whole range for this topological parameter?";

	public static String SM_REMSELFLOOPS = " self-loop(s) removed from ";

	public static String SM_RESULTSSAVED = "  Results saved to network statistics file.";

	public static String SM_RUNNETWORKANALYZER = constructLabel(
			"No node or edge attributes are computed for this network.",
			"You need to run NetworkAnalyzer to compute node or edge attributes.<br>"
					+ "Do you want to run NetworkAnalyzer on the selected network?");

	public static String SM_SAVEERROR = "  ERROR: Could not save results to network statistics file.";

	public static String SM_SAVESETERROR = constructLabel(
			"NetworkAnalyzer cannot save plugin's settings due to security restrictions.",
			"Changes in the settings will be lost after closing Cytoscape.");

	public static String SM_SECERROR1 = "NetworkAnalyzer could not be initialized due to security restrictions.";

	public static String SM_SECERROR2 = "The operation was stopped due to security restrictions.";

	public static String SM_SELECTNET = "Please select a network from the list of loaded networks.";

	public static String SM_SELECTNODES = constructLabel("No nodes are selected.",
			"Please select nodes from the network of interest.");

	public static String SM_SELECTONENET = "Please select a single network from the list of loaded networks.";

	public static String SM_VISUALIZEERROR = "Parameters cannot be visualized because the network was modified or deleted.";

	public static String SM_UNKNOWNERROR = "Unknown error occurred.";

	public static String SM_UNLOADING = "Unloading ";

	public static final String SM_WRONGDATAFILE = "The file specified is not recognized as Network Statistics file.";

	// Menu items added in Cytoscape

	public static String AC_ABOUT = "About NetworkAnalyzer";

	public static String AC_ANALYZE = "Analyze Network";

	public static String AC_ANALYZE_SUBSET = "Analyze Subset of Nodes";

	public static String AC_BATCH_ANALYSIS = "Batch Analysis";

	public static String AC_COMPARE = "Compare Two Networks";

	public static String AC_CONNCOMP = "Extract Connected Components...";

	public static String AC_LOAD = "Load Network Statistics";

	/**
	 * Name of Submenu in Cytoscape's menubar, where network analysis actions are added.
	 */
	public static String AC_MENU_ANALYSIS = "Network Analysis";

	/**
	 * Name of Submenu in Cytoscape's menubar, where network modification actions are added.
	 */
	public static String AC_MENU_MODIFICATION = "Subnetwork Creation";

	public static String AC_PLOTPARAM = "Plot Parameters";

	public static String AC_SETTINGS = "NetworkAnalyzer Settings";

	public static String AC_REMDUPEDGES = "Remove Duplicated Edges";

	public static String AC_REMSELFLOOPS = "Remove Self-Loops";

	public static String AC_MAPPARAM = "Generate Visual Style from Statistics...";

	// Labels of items in dialogs

	public static String DI_ANALYZINGINTERP1 = "  Analyzing interpretation ";

	public static String DI_ANALYZINGINTERP2 = " of ";

	public static String DI_APPLY = "Apply";

	public static String DI_APPLYVS = "Apply visual styles to ";

	public static String DI_ATTRIBUTE1 = "Table Column 1";

	public static String DI_ATTRIBUTE2 = "Table Column 2";

	public static String DI_AUTOANALYSIS1 = "Performing automatic analysis of ";

	public static String DI_AUTOANALYSIS2 = " networks.";

	public static String DI_AXES = "Axes";

	public static String DI_BATCHREPORT = " networks were analyzed. The results are shown below.";

	public static String DI_CANCEL = "Cancel";

	public static String DI_CHARTSETTINGS = "Chart Settings";

	public static String DI_CCOF = "Connected Components of ";

	public static String DI_CDIFF = "Compute Differences";

	public static String DI_CLOSE = "Close";

	public static String DI_CNETWORKS = "Please select two networks from the list below:";

	public static String DI_CINTERSECTION = "Compute Intersection";

	public static String DI_CONNCOMP = "List of connected components";

	public static String DI_COMP = "Component";

	public static String DI_CORR = "Correlation = ";

	public static String DI_CUNION = "Compute Union";

	public static String DI_EXPORTCHART = "Export Chart";

	public static String DI_EXPORTDATA = "Export Data";

	public static String DI_EXTR = "Extract";

	public static String DI_EXTRCOMP = "Extract Component";

	public static String DI_EXTRCOMPLONG = "Extract the selected connected component into a new network named:";

	public static String DI_FILTERDATA = "Change Range";

	public static String DI_FITLINE = "Fit Line";

	public static String DI_FITPL = "Fit Power Law";

	public static String DI_GENERAL = "General";

	public static String DI_GRID = "Gridlines";

	public static String DI_HEIGHT = "Height:";

	public static String DI_HELP = "Help";

	public static String DI_HISTOGRAM = "Histogram";

	public static String DI_IGNOREEDGEDIR = "Ignore edge direction";

	public static String DI_IMAGESIZE = "Image Size";

	public static String DI_INPUTDIR = "Input Directory";

	public static String DI_INTERPR = "Interpretation";

	public static String DI_INTERPRS = "Network Interpretations";

	public static String DI_INTERPR_ALL = "Apply all possible interpretations.";

	public static String DI_INTERPR_DIRECTED = "Consider networks as directed.";

	public static String DI_INTERPR_UNDIRECTED = "Consider networks as undirected.";

	public static String DI_LOWTOBRIGHT = "Low values to bright colors";

	public static String DI_LOWTODARK = "Low values to dark colors";

	public static String DI_LOWTOLARGE = "Low values to large sizes";

	public static String DI_LOWTOSMALL = "Low values to small sizes";

	public static String DI_MAPEDGECOLOR = "Map edge color to:";

	public static String DI_MAPEDGESIZE = "Map edge size to:";

	public static String DI_MAPNODECOLOR = "Map node color to:";

	public static String DI_MAPNODESIZE = "Map node size to:";

	public static String DI_MAPTYPE = "Mapping type:";

	public static String DI_NETFILE = "Network";

	public static String DI_NETSTATSFILE = "Network Statistics File";

	public static String DI_NODEATTR_SAVE = "<html><i>Node parameters stored as node attributes will be written to tab-delimited files. You can disable<br />"
			+ "this option in the NetworkAnalyzer Settings dialog.</i></html>";

	public static String DI_NODEATTR_SAVENOT = "<html><i>Node parameters are not stored as node attributes and will not be saved to files. You can enable<br />"
			+ "this option in the NetworkAnalyzer Settings dialog.</i></html>";

	public static String DI_OK = "OK";

	public static String DI_OUTPUTDIR = "Output Directory";

	public static String DI_PIXELS = "pixels";

	public static String DI_PLOT1 = "Plot node attributes of ";

	public static String DI_PLOT2 = " against each other.";

	public static String DI_REMDUPEDGES = "Remove duplicated edges from the following networks:";

	public static String DI_REMOVE = "Remove";

	public static String DI_REMOVEFILTER = "Restore Range";

	public static String DI_REMOVELINE = "Remove Fitted Line";

	public static String DI_REMOVEPL = "Remove Power Law";

	public static String DI_REMOVESL = "Remove self-loops from the following networks:";

	public static String DI_RESULTS = "Show Results";

	public static String DI_RSQUARED = "R-squared = ";

	public static String DI_SAVE = "Save";

	public static String DI_SAVEDEFAULT = "Save as Default";

	public static String DI_SAVENUMBEREDGES = "Create an edge attribute with number of duplicated edges";

	public static String DI_SAVESTATISTICS = "Save Statistics";

	public static String DI_SCATTER = "Scatter Plot";

	public static String DI_SELECTCOLOR = "Select Color";

	public static String DI_SELECTDIR = "Select Directory";

	public static String DI_SENDREPORT = "Send Report";

	public static String DI_SHOWHIST = "Display as Histogram";

	public static String DI_SHOWSCAT = "Display as Scatter Plot";

	public static String DI_SIMPLEPARAMS = "Simple Parameters";

	public static String DI_STARTANALYSIS = "Start Analysis >";

	public static String DI_UNDEF = "Undefined";

	public static String DI_VIEWENLARGED = "Enlarge Chart";

	public static String DI_VISUALIZEPARAMETER = "Visualize Parameters";

	public static String DI_WIDTH = "Width:";

	// Messages related to the network interpretation

	public static String NI_COMBPAIRED = "Combine paired edges.";

	public static String NI_DIRPAIRED = "The network contains only directed edges and they are paired.";

	public static String NI_DIRUNPAIRED = "The network contains only directed edges and they are not paired.";

	public static String NI_FORCETU = "It will be treated as undirected.";

	public static String NI_IGNOREUSL = "Ignore undirected self-loops.";

	public static String NI_LOOPSBOTH = "It also contains both directed and undirected self-loops.";

	public static String NI_LOOPSDIR = "It also contains directed self-loops.";

	public static String NI_LOOPSUNDIR = "It also contains undirected self-loops.";

	public static String NI_MIXED = "The network contains both directed and undirected edges.";

	public static String NI_NOTCOMB = "Do not combine paired edges.";

	public static String NI_PAIRED = "The directed edges are paired.";

	public static String NI_R_DIR = "Directed.";

	public static String NI_R_DIRL = " Undirected self-loops were ignored.";

	public static String NI_R_UNDIR = "Undirected.";

	public static String NI_R_UNDIRC = " Paired edges were combined.";

	public static String NI_TD = "Treat the network as directed.";

	public static String NI_TU = "Treat the network as undirected.";

	public static String NI_UNDIR = "The network contains only undirected edges.";

	public static String NI_UNPAIRED = "The directed edges are not paired.";

	// Labels to display in settings dialogs

	public static final String SET_PREFIX = "SET_";

	public static String SET_BARCOLOR = "Color of bars";

	public static String SET_BACKGROUNDCOLOR = "Background color for parameter visualization";

	public static String SET_BGCOLOR = "Background color";

	public static String SET_BRIGHTCOLOR = "Bright color to map parameters";

	public static String SET_COLORBUTTON = "Click to Change";

	public static String SET_DARKCOLOR = "Dark color to map parameters";

	public static String SET_DOMAINAXISLABEL = "Label of category axis";

	public static String SET_EXPANDABLE = "Use expandable dialog interface for the display of network statistics";

	public static String SET_GRIDLINESCOLOR = "Color of gridlines";

	public static String SET_HELPURLSTRING = "Location of the help documents";

	public static String SET_HORIZONTALGRIDLINES = "Show horizontal gridlines";

	public static String SET_LOGARITHMICDOMAINAXIS = "Set domain (horizontal) axis to logarithmic";

	public static String SET_LOGARITHMICRANGEAXIS = "Set range (vertical) axis to logarithmic";

	public static String SET_MIDDLECOLOR = "Middle color to map parameters";

	public static String SET_OUTLINE = "Show outline of bars";

	public static String SET_POINTCOLOR = "Color of points";

	public static String SET_POINTSHAPE = "Shape of points";

	public static String SET_RANGEAXISLABEL = "Label of value axis";

	public static String SET_SCIRCLE = "circle";

	public static String SET_SCROSS = "cross";

	public static String SET_SFILLCIRCLE = "filled circle";

	public static String SET_SFILLSQUARE = "filled square";

	public static String SET_SPOINT = "point";

	public static String SET_SSQUARE = "square";

	public static String SET_TITLE = "Chart title";

	public static String SET_USEEDGEATTRIBUTES = "Store edge parameters in edge attributes";

	public static String SET_USENODEATTRIBUTES = "Store node parameters in node attributes";

	public static String SET_VERTICALGRIDLINES = "Show vertical gridlines";

	// Tool tip texts

	public static String TT_AXESSETTINGS = "Axes-related Settings";

	public static String TT_CHARTSETTINGS = "Adjust labels, colors and other visual attributes of the chart";

	public static String TT_CLICK2EXPAND = "Click to expand";

	public static String TT_CLICK2HIDE = "Click to hide";

	public static String TT_FILTERDATA = "Display part of the data in the chart";

	public static String TT_FITLINE = "<html>Fit a line.</html>";

	public static String TT_FITLOGLINE = "<html>Fit a line after computing logarithms of all positive values.</html>";

	public static String TT_FITPL = "<html>Fit a law of the form y = ax<sup>b</sup>.</html>";

	public static String TT_GENSETTINGS = "General Settings";

	public static String TT_GRIDSETTINGS = "Gridlines-related Settings";

	public static String TT_HISTSETTINGS = "Histogram Settings";

	public static String TT_IGNOREEDGEDIR = "Treat all edges as undirected";

	public static String TT_ONLHELP = "Visit the online help of NetworkAnalyzer";

	public static String TT_REMOVEFILTER = "Display the whole data in the chart";

	public static String TT_SAVECHART = "Save the chart as an image";

	public static String TT_SAVEDATA = "Save chart data to a text file";

	public static String TT_SAVENUMBEREDGES = "<html>Edge attribute represents the number of duplicated edges<br>"
			+ "between two nodes, i.e. 1 means no duplicated edges.</html>";

	public static String TT_SCATSETTINGS = "Scatter Plot Settings";

	public static String TT_VIEWENLARGED = "View the chart in a separate window";

	/**
	 * Checks if a description for a given simple parameter is present.
	 * 
	 * @param aParamID
	 *            ID of the simple parameter to inspect.
	 * @return <code>true</code> if a human-readable description for the specified simple parameter exists;
	 *         <code>false</code> otherwise.
	 */
	public static boolean containsSimpleParam(String aParamID) {
		return simpleParams.containsKey(aParamID);
	}

	/**
	 * Gets simple parameter description by the specified ID.
	 * 
	 * @param aParamID
	 *            ID of the simple parameter to get.
	 * @return Human-readable description mapped to the given <code>aParamID</code>; <code>null</code> if such
	 *         a description does not exist.
	 */
	public static String get(String aParamID) {
		return simpleParams.get(aParamID);
	}

	/**
	 * Gets attribute name for the specified ID.
	 * 
	 * @param aID
	 *            ID of attribute name.
	 * @return Attribute name in human-readable form that is mapped to the given ID; <code>null</code> if such
	 *         a name does not exist.
	 */
	public static String getAttr(String aID) {
		String attribute = nodeAttributes.get(aID);
		if (attribute == null) {
			attribute = edgeAttributes.get(aID);
		}
		return attribute;
	}

	/**
	 * Gets all possible computed edge attributes.
	 * 
	 * @return Set of the names of all edge attributes which are computed by NetworkAnalyzer.
	 */
	public static Set<String> getEdgeAttributes() {
		return new HashSet<String>(edgeAttributes.values());
	}

	/**
	 * Gets all possible computed node attributes.
	 * 
	 * @return Set of the names of all node attributes which are computed by NetworkAnalyzer.
	 */
	public static Set<String> getNodeAttributes() {
		return new HashSet<String>(nodeAttributes.values());
	}

	/**
	 * Gets node attributes computed for directed network interpretation.
	 * 
	 * @return Set of the names of all node attributes which are computed by NetworkAnalyzer for a directed
	 *         network interpretation.
	 */
	public static Set<String> getDirNodeAttributes() {
		return new HashSet<String>(dirNodeAttributes);
	}

	/**
	 * Gets node attributes computed for undirected network interpretation.
	 * 
	 * @return Set of the names of all node attributes which are computed by NetworkAnalyzer for an undirected
	 *         network interpretation.
	 */
	public static Set<String> getUndirNodeAttributes() {
		return new HashSet<String>(undirNodeAttributes);
	}

	/**
	 * Constructs a two-line message for an HTML label.
	 * 
	 * @param aLine1
	 *            First line of the text in the label. This text will be bold.
	 * @param aLine2
	 *            Second line of the text in the label.
	 * @return String of newly constructed HTML message.
	 */
	public static String constructLabel(String aLine1, String aLine2) {
		return "<html><b>" + aLine1 + "</b><br><br>" + aLine2 + "</html>";
	}

	/**
	 * Constructs a multi-line report for an HTML label.
	 * 
	 * @param aValues
	 *            Values to be reported, one value per network.
	 * @param aAction
	 *            Action performed on each network.
	 * @param aNetworks
	 *            Networks on which the action was performed.
	 * @return String of newly constructed HTML report.
	 * 
	 * @throws IllegalArgumentException
	 *             If the length of <code>aValues</code> is different than the length of
	 *             <code>aNetworks</code>.
	 * @throws NullPointerException
	 *             If ant of the given parameters is <code>null</code>.
	 */
	public static String constructReport(int[] aValues, String aAction, String[] aNetworks) {
		if (aValues.length != aNetworks.length) {
			throw new IllegalArgumentException();
		}
		final StringBuilder answer = new StringBuilder("<html>");
		for (int i = 0; i < aValues.length; ++i) {
			answer.append(String.valueOf(aValues[i]) + Messages.SM_REMDUPEDGES + aNetworks[i]);
			answer.append("<br>");
		}
		answer.append("</html>");
		return answer.toString();
	}
}
