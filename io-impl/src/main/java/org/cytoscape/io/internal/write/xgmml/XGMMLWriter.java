/*
 Copyright (c) 2006, 2010 The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.io.internal.write.xgmml;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.presentation.property.RichVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

enum ObjectType {
    LIST("list"),
    STRING("string"),
    REAL("real"),
    INTEGER("integer"),
    BOOLEAN("boolean");

    private final String value;

    ObjectType(String v) {
        value = v;
    }

    String value() {
        return value;
    }

    static ObjectType fromValue(String v) {
        for (ObjectType c : ObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

    public String toString() {
        return value;
    }
}

public class XGMMLWriter extends AbstractTask implements CyWriter {

    // XML preamble information
    public static final String ENCODING = "UTF-8";
    public static final float VERSION = 3.0f;
    
    private static final String XML_STRING = "<?xml version=\"1.0\" encoding=\"" + ENCODING + "\" standalone=\"yes\"?>";

    private static final String[] NAMESPACES = { "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"",
            "xmlns:xlink=\"http://www.w3.org/1999/xlink\"",
            "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"", "xmlns:cy=\"http://www.cytoscape.org\"",
            "xmlns=\"http://www.cs.rpi.edu/XGMML\"" };

    // File format version. For compatibility.
    private static final String FORMAT_VERSION = "documentVersion";

    // Node types
    protected static final String NORMAL = "normal";
    protected static final String METANODE = "group";
    protected static final String REFERENCE = "reference";

    // Object types
    protected static final int NODE = 1;
    protected static final int EDGE = 2;
    protected static final int NETWORK = 3;

    public static final String BACKGROUND = "backgroundColor";
    public static final String GRAPH_VIEW_ZOOM = "GRAPH_VIEW_ZOOM";
    public static final String GRAPH_VIEW_CENTER_X = "GRAPH_VIEW_CENTER_X";
    public static final String GRAPH_VIEW_CENTER_Y = "GRAPH_VIEW_CENTER_Y";
    public static final String ENCODE_PROPERTY = "cytoscape.encode.xgmml.attributes";

    private final OutputStream outputStream;
    private final CyNetwork network;
    private final VisualLexicon visualLexicon;
    private final CyNetworkView networkView;
    private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

    private HashMap<CyNode, CyNode> nodeMap = new HashMap<CyNode, CyNode>();
    private HashMap<CyEdge, CyEdge> edgeMap = new HashMap<CyEdge, CyEdge>();

    private int depth = 0; // XML
    // depth
    private String indentString = "";
    private Writer writer = null;

    private boolean doFullEncoding;
    private boolean sessionFormat;

    public XGMMLWriter(final OutputStream outputStream,
                       final RenderingEngineManager renderingEngineManager,
                       final CyNetworkView networkView,
                       final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr) {
        this.outputStream = outputStream;
        this.networkView = networkView;
        this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
        this.network = networkView.getModel();
        this.visualLexicon = renderingEngineManager.getDefaultVisualLexicon();

        // Create our indent string (480 blanks);
        for (int i = 0; i < 20; i++)
            indentString += "                        ";

        doFullEncoding = Boolean.valueOf(System.getProperty(ENCODE_PROPERTY, "true"));
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        writer = new OutputStreamWriter(outputStream);

        // write out the XGMML preamble
        writePreamble();
        depth++;

        // write out our metadata
        writeMetadata();

        // write out network attributes
        writeNetworkAttributes();

        // Output our nodes
        writeNodes();
        // TODO obviously, fix this
        // writeGroups();

        // Create edge objects
        writeEdges();

        depth--;
        // Wwrite final tag
        writeElement("</graph>\n");

        writer.flush();
    }

    /**
     * To tell the writer whether or not the XGMML file will be saved as part of a Cytoscape session file.
     * @param sessionFormat
     */
	public void setSessionFormat(boolean sessionFormat) {
		this.sessionFormat = sessionFormat;
	}

	/**
     * Output the XML preamble.  This includes the XML line as well as the initial
     * &lt;graph&gt; element, along with all of our namespaces.
     *
     * @throws IOException
     */
    private void writePreamble() throws IOException {
        String directed = getDirectionality();
        writeElement(XML_STRING + "\n");
        writeElement("<graph label=\"" + getNetworkName(network) + "\" directed=\"" + directed + "\" ");
        for (int ns = 0; ns < NAMESPACES.length; ns++)
            writer.write(NAMESPACES[ns] + " ");
        writer.write(">\n");
    }

    /**
     * Check directionality of edges, return directionality string to use in xml
     * file as attribute of graph element.
     *
     * Set isMixed field true if network is a mixed network (contains directed
     * and undirected edges), and false otherwise (if only one type of edges are
     * present.)
     *
     * @returns flag to use in XGMML file for graph element's 'directed'
     *          attribute
     */
    private String getDirectionality() {
        boolean seen_directed = false;
        boolean seen_undirected = false;

        for (CyEdge edge : network.getEdgeList()) {
            if (edge.isDirected())
                seen_directed = true;
            else
                seen_undirected = true;
        }

        if ((!seen_directed) && seen_undirected)
            return "0"; // only undir. edges
        else
            return "1"; // either only directed or mixed. For both cases, use dir. as default
    }

    /**
     * Output the network metadata.  This includes our format version and our RDF
     * data.
     *
     * @throws IOException
     */
    private void writeMetadata() throws IOException {
        writeElement("<att name=\"" + FORMAT_VERSION + "\" value=\"" + VERSION + "\"/>\n");
        writeElement("<att name=\"networkMetadata\">\n");
        depth++;
        writeRDF();
        depth--;
        writeElement("</att>\n");
    }

    /**
     * Output the RDF information for this network.
     *     <rdf:RDF>
     *         <rdf:Description rdf:about="http://www.cytoscape.org/">
     *             <dc:type>Protein-Protein Interaction</dc:type>
     *             <dc:description>N/A</dc:description>
     *             <dc:identifier>N/A</dc:identifier>
     *             <dc:date>2007-01-16 13:29:50</dc:date>
     *             <dc:title>Amidohydrolase Superfamily--child</dc:title>
     *             <dc:source>http://www.cytoscape.org/</dc:source>
     *             <dc:format>Cytoscape-XGMML</dc:format>
     *         </rdf:Description>
     *     </rdf:RDF>
     *
     * @throws IOException
     */
    private void writeRDF() throws IOException {
        writeElement("<rdf:RDF>\n");
        depth++;
        writeElement("<rdf:Description rdf:about=\"http://www.cytoscape.org/\">\n");
        depth++;
        writeElement("<dc:type>Protein-Protein Interaction</dc:type>\n");
        writeElement("<dc:description>N/A</dc:description>\n");
        writeElement("<dc:identifier>N/A</dc:identifier>\n");
        java.util.Date now = new java.util.Date();
        java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeElement("<dc:date>" + df.format(now) + "</dc:date>\n");
        writeElement("<dc:title>" + getNetworkName(network) + "</dc:title>\n");
        writeElement("<dc:source>http://www.cytoscape.org/</dc:source>\n");
        writeElement("<dc:format>Cytoscape-XGMML</dc:format>\n");
        depth--;
        writeElement("</rdf:Description>\n");
        depth--;
        writeElement("</rdf:RDF>\n");
    }

    /**
     * Output any network attributes we have defined, including
     * the network graphics information we encode as attributes:
     * backgroundColor, zoom, and the graph center.
     *
     * @throws IOException
     */
    private void writeNetworkAttributes() throws IOException {
        if (networkView != null) {
            if (!sessionFormat) {
				// Write the background color
				Paint paint = networkView.getVisualProperty(MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT);
				String bgColor = paint2string(paint, Color.WHITE);

				writeElement("<att type=\"string\" name=\"" + BACKGROUND + "\" value=\"" + bgColor + "\"/>\n");
            }

            // Write the graph zoom
            final Double zoom = networkView.getVisualProperty(MinimalVisualLexicon.NETWORK_SCALE_FACTOR);
            writeAttributeXML(GRAPH_VIEW_ZOOM, ObjectType.REAL, zoom, true);

            // Write the graph center
            final Double cx = networkView.getVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION);
            writeAttributeXML(GRAPH_VIEW_CENTER_X, ObjectType.REAL, cx, true);

            final Double cy = networkView.getVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION);
            writeAttributeXML(GRAPH_VIEW_CENTER_Y, ObjectType.REAL, cy, true);
        }

        // Now handle all of the other network attributes, but only if exporting to XGMML directly
        if (!sessionFormat) {
	        CyRow row = network.getCyRow();
	        CyTable table = row.getTable();
	
	        for (final CyColumn column : table.getColumns())
	            writeAttribute(row, column.getName());
        }
    }

    /**
     * Output Cytoscape nodes as XGMML
     *
     * @throws IOException
     */
    private void writeNodes() throws IOException {
        for (CyNode node : network.getNodeList()) {
            // TODO
            // if (!CyGroupManager.isaGroup(curNode))
            writeNode(node, null);
        }
    }

    /**
     * Output a single CyNode as XGMML
     *
     * @param node the node to output
     * @throws IOException
     */
    private void writeNode(CyNode node, List<CyNode> groupList) throws IOException {
        // Remember that we've seen this node
        nodeMap.put(node, node);

        // Output the node
        String id = quote(Long.toString(node.getSUID()));
		String label = quote(node.getCyRow().get(CyNetwork.NAME, String.class));

		writeElement("<node id=" + id + " label=" + label + ">\n");
        depth++;
        
		if (!sessionFormat) {
			// Output the node attributes
			// TODO This isn't handling namespaces
			for (final CyColumn column : node.getCyRow().getTable().getColumns())
				writeAttribute(node.getCyRow(), column.getName());
		}

        // TODO deal with groups
        //        if (groupList != null && groupList.size() > 0) {
        //            // If we're a group, output the graph attribute now
        //            writeElement("<att>\n");
        //            depth++;
        //            writeElement("<graph>\n");
        //            depth++;
        //            for (CyNode childNode : groupList) {
        //                if (CyGroupManager.isaGroup(childNode)) {
        //                    // We have an embeddedgroup -- recurse
        //                    CyGroup childGroup = CyGroupManager.getCyGroup(childNode);
        //                    writeNode(childGroup.getGroupNode(), childGroup.getNodes());
        //                } else {
        //                    if (nodeMap.containsKey(childNode))
        //                        writeElement("<node xlink:href=\"#" + childNode.getSUID() + "\"/>\n");
        //                    else
        //                        writeNode(childNode, null);
        //                }
        //            }
        //            depth--;
        //            writeElement("</graph>\n");
        //            depth--;
        //            writeElement("</att>\n");
        //        }
		
        // Output the node graphics if we have a view
        if (networkView != null) writeGraphics(networkView.getNodeView(node));

        depth--;
        writeElement("</node>\n");
    }

    /*
     * // TODO fix! private void writeGroups() throws IOException { // Two pass
     * approach. First, walk through the list // and see if any of the children
     * of a group are // themselves a group. If so, remove them from // the list
     * & will pick them up on recursion groupList =
     * CyGroupManager.getGroupList();
     *
     * if ((groupList == null) || groupList.isEmpty()) return;
     *
     * HashMap<CyGroup,CyGroup> embeddedGroupList = new
     * HashMap<CyGroup,CyGroup>();
     *
     * for (CyGroup group: groupList) { List<CyNode> childList =
     * group.getNodes();
     *
     * if ((childList == null) || (childList.size() == 0)) continue;
     *
     * for (CyNode childNode: childList) { if
     * (CyGroupManager.isaGroup(childNode)) { // Get the actual group CyGroup
     * embGroup = CyGroupManager.getCyGroup(childNode);
     * embeddedGroupList.put(embGroup, embGroup); } } }
     *
     * for (CyGroup group: groupList) { // Is this an embedded group? if
     * (embeddedGroupList.containsKey(group)) continue; // Yes, skip it
     *
     * writeGroup(group); } }
     *
     * private void writeGroup(CyGroup group) throws IOException { CyNode
     * groupNode = group.getGroupNode(); writeNode(groupNode, group.getNodes());
     * }
     */
    /**
     * Output Cytoscape edges as XGMML
     *
     * @throws IOException
     */
    private void writeEdges() throws IOException {
        for (CyEdge edge : network.getEdgeList()) {
            edgeMap.put(edge, edge);
            writeEdge(edge);
        }
    }

    /**
     * Output a Cytoscape edge as XGMML
     *
     * @param curEdge the edge to output
     * @throws IOException
     */
	private void writeEdge(CyEdge curEdge) throws IOException {
		// Write the edge
		String target = quote(Long.toString(curEdge.getTarget().getSUID()));
		String source = quote(Long.toString(curEdge.getSource().getSUID()));

		// Make sure these nodes exist
		if (!nodeMap.containsKey(curEdge.getTarget()) || !nodeMap.containsKey(curEdge.getSource())) return;

		String id = quote(Long.toString(curEdge.getSUID()));
		String label = quote(curEdge.getCyRow().get(CyNetwork.NAME, String.class));
		String directed = quote(curEdge.isDirected() ? "1" : "0");

		writeElement("<edge id=" + id + " label=" + label + " source=" + source + " target=" + target +
					 " cy:directed=" + directed + ">\n");
		depth++;

		if (!sessionFormat) {
			// Write the edge attributes
			// TODO This isn't handling namespaces
			for (final CyColumn column : curEdge.getCyRow().getTable().getColumns())
				writeAttribute(curEdge.getCyRow(), column.getName());
		}

		// Write the edge graphics
		if (networkView != null) writeGraphics(networkView.getEdgeView(curEdge));

		depth--;
		writeElement("</edge>\n");
	}

    @SuppressWarnings("unchecked")
	private void writeGraphics(View<? extends CyTableEntry> view) throws IOException {
        if (view == null) return;
        CyTableEntry element = view.getModel();

        writeElement("<graphics");

        VisualProperty<?> root  = (element instanceof CyNode) ? MinimalVisualLexicon.NODE : MinimalVisualLexicon.EDGE;
        Collection<VisualProperty<?>> visualProperties = visualLexicon.getAllDescendants(root);
        List<VisualProperty<?>> lockedProperties = new ArrayList<VisualProperty<?>>();

        for (VisualProperty vp : visualProperties) {
            String key = getGraphicsKey(vp);
            Object value = view.getVisualProperty(vp);

            if (value != null && view.isValueLocked(vp))
            	lockedProperties.add(vp);
            
            if (key != null && value != null && !ignoreGraphicsAttribute(element, key)) {
                if (key.toLowerCase().contains("transparency") && value instanceof Integer) {
                    // Cytoscape's XGMML specifies transparency as between 0-1.0 when it is a <graphics> attribute!
                    float transparency  = ((Integer) value).floatValue();
                    value = transparency / 255;
                } else {
                    value = vp.toSerializableString(value);
                }
                
                if (value != null)
                	writeAttributePair(key, value);
            }
        }
        
		Map<String, String> unrecognizedMap = unrecognizedVisualPropertyMgr
				.getUnrecognizedVisualProperties(networkView, view);

		if (lockedProperties.size() > 0 || unrecognizedMap.size() > 0) {
			writer.write(">\n");
			depth++;
            
            // serialize locked properties as <att> tags inside <graphics>
            for (VisualProperty vp : lockedProperties) {
            	Object value = view.getVisualProperty(vp);
            	value = vp.toSerializableString(value);
            	
            	if (value != null)
            		writeAttributeXML(vp.getIdString(), ObjectType.STRING, value, true);
            }
            
            // also save unrecognized visual properties
            for (Map.Entry<String, String> entry : unrecognizedMap.entrySet()) {
            	String key = entry.getKey();
            	String value = entry.getValue();
            	
            	if (value != null)
            		writeAttributeXML(key, ObjectType.STRING, value, true);
            }
            
            depth--;
            writeElement("</graphics>\n");
        } else {
        	writer.write("/>\n");
        }

        // TODO: Handle bends
        if (element instanceof CyEdge) {
			//   final CyEdge edge = (CyEdge) element;
			//   final Bend bendData = edge.getBend();
            //   final List<Point2D> handles = new ArrayList<Point2D>(); //final List<Point2D> handles = bendData.getHandles();
            //
            //   if (handles.size() == 0) {
            //       writer.write("/>\n");
            //   } else {
            //       writer.write(">\n");
            //       depth++;
            //       writeElement("<att name=\"edgeBend\">\n");
            //       depth++;
            //       for (Point2D handle: handles) {
            //           String x = Double.toString(handle.getX());
            //           String y = Double.toString(handle.getY());
            //           writeElement("<att name=\"handle\" x=\""+x+"\" y=\""+y+"\" />\n");
            //       }
            //       depth--;
            //       writeElement("</att>\n");
            //       depth--;
            //       writeElement("</graphics>\n");
            //   }
        }
    }

    /**
	 * @param element
	 * @param attName
	 * @return
	 */
    private boolean ignoreGraphicsAttribute(final CyTableEntry element, String attName) {
		boolean b = (sessionFormat && (element instanceof CyNode) && !attName.matches("x|y|z"));
		b = b || (sessionFormat && (element instanceof CyEdge));
		
		return b;
	}
    
    private String getGraphicsKey(VisualProperty<?> vp) {
        //Nodes
        if (vp.equals(MinimalVisualLexicon.NODE_X_LOCATION)) return "x";
        if (vp.equals(MinimalVisualLexicon.NODE_Y_LOCATION)) return "y";
        if (vp.equals(RichVisualLexicon.NODE_Z_LOCATION)) return "z";
        if (vp.equals(MinimalVisualLexicon.NODE_WIDTH)) return "w";
        if (vp.equals(MinimalVisualLexicon.NODE_HEIGHT)) return "h";
        if (vp.equals(MinimalVisualLexicon.NODE_FILL_COLOR)) return "fill";
        if (vp.equals(RichVisualLexicon.NODE_SHAPE)) return "type";
        if (vp.equals(RichVisualLexicon.NODE_BORDER_WIDTH)) return "width";
        if (vp.equals(RichVisualLexicon.NODE_BORDER_PAINT)) return "outline";
        if (vp.equals(RichVisualLexicon.NODE_TRANSPARENCY)) return "cy:nodeTransparency";
        if (vp.equals(RichVisualLexicon.NODE_BORDER_LINE_TYPE)) return "cy:borderLineType";
        if (vp.equals(RichVisualLexicon.NODE_LABEL)) return "cy:nodeLabelFont";

        // Edges
        if (vp.equals(MinimalVisualLexicon.EDGE_WIDTH)) return "width";
        if (vp.equals(RichVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)) return "fill";
        if (vp.equals(MinimalVisualLexicon.EDGE_LABEL)) return "cy:edgeLabelFont";
        if (vp.equals(RichVisualLexicon.EDGE_LINE_TYPE)) return "cy:edgeLineType";

        return null;
    }

    /**
     * Creates an attribute to write into XGMML file.
     *
     * @param id -
     *            id of node, edge or network
     * @param row -
     *            CyRow to load
     * @param attName -
     *            attribute name
     * @return att - Att to return (gets written into xgmml file - CAN BE NULL)
     *
     * @throws IOException
     */
    private void writeAttribute(final CyRow row, final String attName) throws IOException {
    	// create an attribute and its type:
		final CyColumn column = row.getTable().getColumn(attName);
		if (column == null) return;
		final Class<?> attType = column.getType();

		if (attType == Double.class) {
			Double dAttr = row.get(attName, Double.class);
			writeAttributeXML(attName, ObjectType.REAL, dAttr, true);
		} else {
			if (attType == Integer.class) {
				Integer iAttr = row.get(attName, Integer.class);
				writeAttributeXML(attName, ObjectType.INTEGER, iAttr, true);
			} else if (attType == String.class) {
				String sAttr = row.get(attName, String.class);
				// Protect tabs and returns
				if (sAttr != null) {
					sAttr = sAttr.replace("\n", "\\n");
					sAttr = sAttr.replace("\t", "\\t");
				}
				// TODO: nested networks
				//                if (attName.equals(CyNode.NESTED_NETWORK_ID_ATTR)) {
				//                    // This is a special attribute for nested network.
				//                    sAttr = Cytoscape.getNetwork(sAttr).getTitle();
				//                }
				writeAttributeXML(attName, ObjectType.STRING, sAttr, true);
			} else if (attType == Boolean.class) {
				Boolean bAttr = row.get(attName, Boolean.class);
				writeAttributeXML(attName, ObjectType.BOOLEAN, bAttr, true);
			} else if (attType == List.class) {
				final List<?> listAttr = row.getList(attName, column.getListElementType());
				writeAttributeXML(attName, ObjectType.LIST, null, false);

				if (listAttr != null) {
					depth++;
					// interate through the list
					for (Object obj : listAttr) {
						// Protect tabs and returns (if necessary)
						String sAttr = obj.toString();
						if (sAttr != null) {
							sAttr = sAttr.replace("\n", "\\n");
							sAttr = sAttr.replace("\t", "\\t");
						}
						// set child attribute value & label
						writeAttributeXML(attName, checkType(obj), sAttr, true);
					}
					depth--;
				}
				writeAttributeXML(null, null, null, true);
			}
		}
	}

    /**
     * writeAttributeXML outputs an XGMML attribute
     *
     * @param name is the name of the attribute we are outputting
     * @param type is the XGMML type of the attribute
     * @param value is the value of the attribute we're outputting
     * @param end is a flag to tell us if the attribute should include a tag end
     *
     * @throws IOException
     */
    private void writeAttributeXML(String name, ObjectType type, Object value, boolean end) throws IOException {
        if (name == null && type == null)
            writeElement("</att>\n");
        else {
            writeElement("<att type=" + quote(type.toString()));

            if (name != null) writer.write(" name=" + quote(name));
            if (value != null) writer.write(" value=" + quote(value.toString()));

            if (end)
                writer.write("/>\n");
            else
                writer.write(">\n");
        }
    }

    /**
     * writeAttributePair outputs the name,value pairs for an attribute
     *
     * @param name is the name of the attribute we are outputting
     * @param value is the value of the attribute we're outputting
     *
     * @throws IOException
     */
    private void writeAttributePair(String name, Object value) throws IOException {
        writer.write(" " + name + "=" + quote(value.toString()));
    }

    /**
     * writeElement outputs the name,value pairs for an attribute
     *
     * @param line is the element string to output
     *
     * @throws IOException
     */
    private void writeElement(String line) throws IOException {
        while (depth * 2 > indentString.length() - 1)
            indentString = indentString + "                        ";
        writer.write(indentString, 0, depth * 2);
        writer.write(line);
    }

    /**
     * Convert color (paint) to RGB string.<br>
     *
     * @param paint Paint object to be converted.
     * @param defColor An optional default color, in case paint is not a simple Color.
     * @return Color in RGB string.
     */
    private String paint2string(final Paint paint, Color defColor) {
        Color c = null;

        if (paint instanceof Color)
            c = (Color) paint;
        else
            c = defColor == null ? Color.WHITE : defColor;

        return ("#" // +Integer.toHexString(c.getRGB());
                +
                Integer.toHexString(256 + c.getRed()).substring(1) +
                Integer.toHexString(256 + c.getGreen()).substring(1) + Integer.toHexString(256 + c.getBlue())
                .substring(1));
    }

    /**
     * Check the type of Attributes.
     *
     * @param obj
     * @return Attribute type in string.
     *
     */
    private ObjectType checkType(final Object obj) {
        if (obj.getClass() == String.class) {
            return ObjectType.STRING;
        } else if (obj.getClass() == Integer.class) {
            return ObjectType.INTEGER;
        } else if ((obj.getClass() == Double.class) || (obj.getClass() == Float.class)) {
            return ObjectType.REAL;
        } else if (obj.getClass() == Boolean.class) {
            return ObjectType.BOOLEAN;
        } else {
            return null;
        }
    }

    private String getNetworkName(CyNetwork network) {
        String name = encode(network.getCyRow().get(CyNetwork.NAME, String.class));
        if (name == null) name = "UNDEFINED";

        return name;
    }

    /**
     * encode returns a quoted string appropriate for use as an XML attribute
     *
     * @param str the string to encode
     * @return the encoded string
     */
    private String encode(String str) {
        // Find and replace any "magic", control, non-printable etc. characters
        // For maximum safety, everything other than printable ASCII (0x20 thru 0x7E) is converted into a character entity
        String s = null;

        if (str != null) {
            StringBuilder sb = new StringBuilder(str.length());

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);

                if ((c < ' ') || (c > '~')) {
                    if (doFullEncoding) {
                        sb.append("&#x");
                        sb.append(Integer.toHexString((int) c));
                        sb.append(";");
                    } else {
                        sb.append(c);
                    }
                } else if (c == '"') {
                    sb.append("&quot;");
                } else if (c == '\'') {
                    sb.append("&apos;");
                } else if (c == '&') {
                    sb.append("&amp;");
                } else if (c == '<') {
                    sb.append("&lt;");
                } else if (c == '>') {
                    sb.append("&gt;");
                } else {
                    sb.append(c);
                }
            }

            s = sb.toString();
        }

        return s;
    }

    /**
     * quote returns a quoted string appropriate for use as an XML attribute
     *
     * @param str the string to quote
     * @return the quoted string
     */
    private String quote(String str) {
        return '"' + encode(str) + '"';
    }
}
