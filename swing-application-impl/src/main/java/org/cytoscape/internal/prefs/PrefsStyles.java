package org.cytoscape.internal.prefs;

import java.awt.Dimension;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.cytoscape.internal.prefs.lib.CheckList;
import org.cytoscape.internal.prefs.lib.HBox;
import org.cytoscape.internal.prefs.lib.VBox;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PrefsStyles extends AbstractPrefsPanel {

	private static final long serialVersionUID = 1L;

	protected PrefsStyles(Cy3PreferencesPanel dlog) {
		super(dlog, "vizmap");
	}

	@Override
	public void initUI() {
		super.initUI();
//		box.setMaximumSize(dims);
		JLabel intro  = new JLabel("These lists the visible attributes for Nodes, Edges and Networks.");
		JLabel intro1  = new JLabel("Select the items you want to be shown in the Styles control panel");
		
//		JLabel intro2  = new JLabel("Double click a style to include it in legends.");
		add(new HBox(false, false, intro));
		add(new HBox(false, false, intro1));
		HBox box = new HBox(true, true, makeNodeList(), makeEdgeList(), makeNetworkList());
		box.setBorder(BorderFactory.createEmptyBorder(13,15,0,15));
		add(box);
		box.setPreferredSize(new Dimension(550, 300));
		setBorder(BorderFactory.createEmptyBorder(13,15,0,15));
	}

	CheckList nodeAttributes;
	CheckList edgeAttributes;
	CheckList networkAttributes;

	String[] node = { "Border Line Type" , "Border Paint", "Border Transparency", "Border Width", "Fill Color", "Height",
			 "Label", "Label Color", "Label Font Face", "Label Font Size", "Label Position", "Label Transparency", "Label Width", 
			 "Nested Network Image Visible", "Padding (Compound Node)", "Paint", "Selected Paint", "Shape", "Shape (Compound Node)",  "Size", "Tooltip", "Transparency", "Visible", "Width", 
			 "X Location", "Y Location", "Z Location" };
			 
	String[] edge = { "Bend" , "Color (Selected)", "Color (Unselected)", "Curved", "Label", "Label Color", "Label Font Face", "Label Font Size", "Label Transparency", "Label Width", 
			"Line Type", "Paint", "Source Arrow Selected Paint", "Source Arrow Shape", "Source Arrow Size", "Source Arrow Selected Paint", "Stroke Color (Selected)", "Stroke Color (Unselected)",
			"Target Arrow Selected Paint", "Target Arrow Shape", "Target Arrow Size", "Target Arrow Selected Paint", "Tooltip", "Transparency", "Visible", "Width"};
	
	String[] network = { "Background Paint" , "Center X Location", "Center Y Location", "Edge Selection", "Height", "Node Selection", "Scale Factor", "Size", "Title", "Width"};
	
	private VBox makeNodeList() {
		nodeAttributes = new CheckList(node);
		components.put("defaultVisualProperties.node", nodeAttributes);
		JScrollPane container = new JScrollPane(nodeAttributes, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return new VBox(false, true, new HBox(true, true, new JLabel("Node")), container);
	}
	private VBox makeEdgeList() {
		edgeAttributes = new CheckList(edge);
		components.put("defaultVisualProperties.edge", edgeAttributes);
		JScrollPane container = new JScrollPane(edgeAttributes, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return new VBox(false, true, new HBox(true, true, new JLabel("Edge")), container);
	}
	private VBox makeNetworkList() {
		networkAttributes = new CheckList(network);
		components.put("defaultVisualProperties.network", networkAttributes);
		JScrollPane container = new JScrollPane(networkAttributes, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return new VBox(false, true, new HBox(true, true, new JLabel("Network")), container);
	}

	
	@Override protected String getPropFileName()	{ return "vizmapper";	}

	@Override
	public void install(Properties properties) {
		String node = properties.getProperty("defaultVisualProperties.node");
		nodeAttributes.setValues(node, "NODE_");

		String edge = properties.getProperty("defaultVisualProperties.edge");
		edgeAttributes.setValues(edge, "EDGE_");

		String netw = properties.getProperty("defaultVisualProperties.network");
		networkAttributes.setValues(netw, "NETWORK_");
	}

	    @Override public void extract(Properties properties)
	    {
			properties.put("defaultVisualProperties.node", nodeAttributes.getValues("NODE_"));
			properties.put("defaultVisualProperties.edge", edgeAttributes.getValues("EDGE_"));
			properties.put("defaultVisualProperties.network", networkAttributes.getValues("NETWORK_"));
			if (verbose)  
			{
				System.out.println("--  " + getName());
				dump(properties, "defaultVisualProperties");
			}
	    }
		//--------------------------------------------------------------------------
     }
