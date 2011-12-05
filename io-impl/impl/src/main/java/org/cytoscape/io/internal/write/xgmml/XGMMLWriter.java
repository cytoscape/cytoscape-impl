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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
    private static final String DOCUMENT_VERSION_NAME = "cy:documentVersion";

    // Node types
    protected static final String NORMAL = "normal";
    protected static final String METANODE = "group";
    protected static final String REFERENCE = "reference";

    public static final String ENCODE_PROPERTY = "cytoscape.encode.xgmml.attributes";

    private final OutputStream outputStream;
    private final CyNetwork network;
    private Set<CySubNetwork> subNetworks;
    private CyNetworkView networkView;
    private final VisualLexicon visualLexicon;
    private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
    private final CyNetworkManager networkManager;
    private final CyRootNetworkManager rootNetworkManager;

    private HashMap<CyNode, CyNode> nodeMap = new HashMap<CyNode, CyNode>();
    private HashMap<CyEdge, CyEdge> edgeMap = new HashMap<CyEdge, CyEdge>();
    private HashMap<CyNetwork, CyNetwork> networkMap = new HashMap<CyNetwork, CyNetwork>();

    private int depth = 0;
    private String indentString = "";
    private Writer writer = null;

    private boolean doFullEncoding;
    private boolean sessionFormat;

    public XGMMLWriter(final OutputStream outputStream,
                       final RenderingEngineManager renderingEngineManager,
                       final CyNetworkView networkView,
                       final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
                       final CyNetworkManager networkManager,
                       final CyRootNetworkManager rootNetworkManager) {
		this(outputStream, renderingEngineManager, networkView.getModel(), unrecognizedVisualPropertyMgr,
				networkManager, rootNetworkManager);
		this.networkView = networkView;
    }
    
    public XGMMLWriter(final OutputStream outputStream,
                       final RenderingEngineManager renderingEngineManager,
                       final CyNetwork network,
                       final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
                       final CyNetworkManager networkManager,
                       final CyRootNetworkManager rootNetworkManager) {
		this.outputStream = outputStream;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
		this.networkManager = networkManager;
		this.rootNetworkManager = rootNetworkManager;
		this.visualLexicon = renderingEngineManager.getDefaultVisualLexicon();
		
		if (network instanceof CyRootNetwork) {
			CyRootNetwork rootNetwork = (CyRootNetwork) network;
			this.network = rootNetwork;
			this.subNetworks = getRegisteredSubNetworks(rootNetwork);
		} else {
			this.network = network;
		}
		
		// Create our indent string (480 blanks);
		for (int i = 0; i < 20; i++)
			indentString += "                        ";
		
		doFullEncoding = Boolean.valueOf(System.getProperty(ENCODE_PROPERTY, "true"));
	}

	@Override
    public void run(TaskMonitor taskMonitor) throws Exception {
    	taskMonitor.setProgress(0.0);
        writer = new OutputStreamWriter(outputStream);

        writePreamble();
        taskMonitor.setProgress(0.2);
        depth++;
        
        writeMetadata();
        taskMonitor.setProgress(0.3);
        
        writeNetworkAttributes();
        taskMonitor.setProgress(0.4);
        
        writeNodes();
        taskMonitor.setProgress(0.6);
        
        writeEdges();
        taskMonitor.setProgress(0.8);
        depth--;
        
        // Wwrite final tag
        writeElement("</graph>\n");

        writer.flush();
        taskMonitor.setProgress(1.0);
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
    	writeElement(XML_STRING + "\n");
        writeElement("<graph");
        
        long id = network.getSUID();
        
        if (sessionFormat && networkView != null) {
        	// Saving a network view into a CYS file?
        	id = networkView.getSUID();
        }
        
        writeAttributePair("id", id);
        
        // Save the label to make it more human readable
        String label = networkView != null ? getLabel(networkView) : getLabel(network);
        writeAttributePair("label", label);
        
        // Is is a network view serialization?
        if (sessionFormat) {
        	writeAttributePair("cy:view",  AttributeValueUtil.toXGMMLBoolean(networkView != null));
        	
        	if (networkView != null)
        		writeAttributePair("cy:networkId", network.getSUID());
        } else {
        	// Only if exporting to standard XGMML
        	writeAttributePair("directed", getDirectionality());
        }
        
        writeAttributePair(DOCUMENT_VERSION_NAME, VERSION);
        
        for (int ns = 0; ns < NAMESPACES.length; ns++)
            write(" " + NAMESPACES[ns]);
        
        write(">\n");
        
        networkMap.put(network, network);
    }

    /**
     * Output the network metadata.  This includes our format version and our RDF data.
     *
     * @throws IOException
     */
    private void writeMetadata() throws IOException {
    	if (!sessionFormat) {
			writeElement("<att name=\"networkMetadata\">\n");
			depth++;
			writeRDF();
			depth--;
	        writeElement("</att>\n");
    	}
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
    	String title = networkView != null ? getLabel(networkView) : getLabel(network);
    	Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
        writeElement("<rdf:RDF>\n");
        depth++;
        writeElement("<rdf:Description rdf:about=\"http://www.cytoscape.org/\">\n");
        depth++;
        writeElement("<dc:type>Protein-Protein Interaction</dc:type>\n");
        writeElement("<dc:description>N/A</dc:description>\n");
        writeElement("<dc:identifier>N/A</dc:identifier>\n");
        writeElement("<dc:date>" + df.format(now) + "</dc:date>\n");
        writeElement("<dc:title>" + title + "</dc:title>\n");
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
		// Handle all of the other network attributes, but only if exporting to XGMML directly
		writeAttributes(network.getCyRow());

		// Write sub-graphs first, but only if the XGMML is for a CYS file
		if (sessionFormat && subNetworks != null) {
			for (CySubNetwork subNet : subNetworks) {
				networkMap.put(subNet, subNet);
				
				writeElement("<att>\n");
				depth++;

				writeElement("<graph");
				// Always write the network ID
				writeAttributePair("id", subNet.getSUID());
				// Save the label to make it more human readable
				writeAttributePair("label", getLabel(subNet));
				write(">\n");
				depth++;

				for (CyNode childNode : subNet.getNodeList())
					writeNode(childNode);
				for (CyEdge childEdge : subNet.getEdgeList())
					writeEdge(childEdge);

				depth--;
				writeElement("</graph>\n");
				depth--;
				writeElement("</att>\n");
			}
		}

		// Root-network's graphics attributes
		if (networkView != null) {
			writeGraphics(networkView);
		}
	}

	/**
	 * Output Cytoscape nodes as XGMML
	 * @throws IOException
	 */
	private void writeNodes() throws IOException {
		if (sessionFormat && networkView != null) {
			for (View<CyNode> view : networkView.getNodeViews()) {
				writeNodeView(view);
			}
		} else {
			for (CyNode node : network.getNodeList()) {
				// Only if not already written inside a nested graph
				if (!nodeMap.containsKey(node))
					writeNode(node);
			}
		}
	}
	
	/**
     * Output Cytoscape edges as XGMML
     * @throws IOException
     */
    private void writeEdges() throws IOException {
    	if (sessionFormat && networkView != null) {
	    	for (View<CyEdge> view : networkView.getEdgeViews()) {
				writeEdgeView(view);
			}
    	} else {
    		for (CyEdge edge : network.getEdgeList()) {
    			// Only if not already written inside a nested graph
    			if (!edgeMap.containsKey(edge))
    				writeEdge(edge);
	        }
		}
    }

    /**
     * Output a single CyNode as XGMML
     *
     * @param node the node to output
     * @throws IOException
     */
	private void writeNode(CyNode node) throws IOException {
		boolean written = nodeMap.containsKey(node);
		
		// Output the node
		writeElement("<node");
		
		if (written) {
			// Write as an XLink only
			writeAttributePair("xlink:href", "#" + node.getSUID());
			write("/>\n");
		} else {
			// Remember that we've wrote this node
	     	nodeMap.put(node, node);
			
			// Write the actual node with its properties
			writeAttributePair("id", node.getSUID());
			writeAttributePair("label", getLabel(node));
			write(">\n");
			depth++;
			
			// Output the node attributes
			writeAttributes(node.getCyRow());
			
	        // Write node's sub-graph:
			CyNetwork subNet = node.getNetworkPointer();

			if (subNet != null) {
				// Write a nested graph element
				writeElement("<att>\n");
				depth++;
				writeElement("<graph");
				
				if (sessionFormat || networkMap.containsKey(subNet)) {
					// This network has already been written...
					String href = "#" + subNet.getSUID();
					
					if (sessionFormat && (subNetworks == null || !subNetworks.contains(subNet))) {
						// This XGMML file will be saved as part of a CYS file,
						// and the sub-network does NOT belong to the same root-network
						CyNetwork baseNet = (subNet instanceof CyRootNetwork) ? 
								((CyRootNetwork) subNet).getBaseNetwork() :
								rootNetworkManager.getRootNetwork(subNet).getBaseNetwork();
						// ...So add the file name to the XLink URI
						String fileName = SessionUtil.getXGMMLFilename(baseNet);
						href = fileName + href;
					}
					
					writeAttributePair("xlink:href", href);
					write("/>\n");
				} else {
					// New sub-network...
					networkMap.put(subNet, subNet);
					
					write(">\n");
					depth++;
					
					writeAttributes(subNet.getCyRow());
					
					for (CyNode n : subNet.getNodeList())
						writeNode(n);
					for (CyEdge e : network.getEdgeList())
						writeEdge(e);
					
					depth--;
					writeElement("</graph>\n");
				}
				
				depth--;
				writeElement("</att>\n");
			}
			
	        // Output the node graphics if we have a view and it is a simple XGMML export
			if (!sessionFormat && networkView != null) {
				writeGraphics(networkView.getNodeView(node));
			}
			
			depth--;
			writeElement("</node>\n");
		}
	}
	
	/**
     * Output a single node view as XGMML
     *
     * @param view the node view to output
     * @throws IOException
     */
	private void writeNodeView(View<CyNode> view) throws IOException {
		// Output as a node tag
		writeElement("<node");
		writeAttributePair("id", view.getSUID());
		writeAttributePair("label", getLabel(view.getModel()));
		writeAttributePair("cy:nodeId", view.getModel().getSUID());
		write(">\n");
        depth++;
        
        // Output the node graphics if we have a view and it is a simple XGMML export
		writeGraphics(view);

		depth--;
		writeElement("</node>\n");
	}

    /**
     * Output a Cytoscape edge as XGMML
     *
     * @param edge the edge to output
     * @throws IOException
     */
	private void writeEdge(CyEdge edge) throws IOException {
		// Make sure these nodes exist
		if (!nodeMap.containsKey(edge.getTarget()) || !nodeMap.containsKey(edge.getSource())) return;
		boolean written = edgeMap.containsKey(edge);

		writeElement("<edge");
		
		if (written) {
			// Write as an XLink only
			writeAttributePair("xlink:href", "#" + edge.getSUID());
			write("/>\n");
		} else {
			// Remember that we've wrote this edge
			edgeMap.put(edge, edge);
			
			writeAttributePair("id", edge.getSUID());
			writeAttributePair("label", getLabel(edge));
			writeAttributePair("source", edge.getSource().getSUID());
			writeAttributePair("target", edge.getTarget().getSUID());
			writeAttributePair("cy:directed",  AttributeValueUtil.toXGMMLBoolean(edge.isDirected()));
			write(">\n");
			depth++;

			// Write the edge attributes
			writeAttributes(edge.getCyRow());
	
			// Write the edge graphics
			if (networkView != null) {
				writeGraphics(networkView.getEdgeView(edge));
			}

			depth--;
			writeElement("</edge>\n");
		}
	}
	
	/**
     * Output a Cytoscape edge view as XGMML
     *
     * @param view the edge view to output
     * @throws IOException
     */
	private void writeEdgeView(View<CyEdge> view) throws IOException {
		// It is not necessary to write edges that have no locked visual properties
		boolean hasLockedVisualProps = false;
		Collection<VisualProperty<?>> visualProperties = visualLexicon.getAllDescendants(MinimalVisualLexicon.EDGE);
		
		for (VisualProperty<?> vp : visualProperties) {
			if (view.isValueLocked(vp)) {
				hasLockedVisualProps = true;
				break;
			}
		}
		
		if (hasLockedVisualProps) {
			writeElement("<edge");
			writeAttributePair("id", view.getSUID());
			writeAttributePair("label", getLabel(view.getModel()));
			writeAttributePair("cy:edgeId", view.getModel().getSUID());
			write(">\n");
			depth++;
	
			// Write the edge graphics
			writeGraphics(view);
	
			depth--;
			writeElement("</edge>\n");
		}
	}

    @SuppressWarnings({"unchecked", "rawtypes"})
	private void writeGraphics(View<? extends CyTableEntry> view) throws IOException {
        if (view == null) return;
        writeElement("<graphics");
        
        CyTableEntry element = view.getModel();
        final VisualProperty<?> root;
        
        if (element instanceof CyNode)
        	root = MinimalVisualLexicon.NODE;
        else if (element instanceof CyEdge)
        	root = MinimalVisualLexicon.EDGE;
        else
        	root = MinimalVisualLexicon.NETWORK;
        
        Collection<VisualProperty<?>> visualProperties = visualLexicon.getAllDescendants(root);
        List<VisualProperty<?>> cyProperties = new ArrayList<VisualProperty<?>>();
        List<VisualProperty<?>> lockedProperties = new ArrayList<VisualProperty<?>>();

        for (VisualProperty vp : visualProperties) {
        	if (root == MinimalVisualLexicon.NETWORK && vp.getTargetDataType() != CyNetwork.class) {
        		// If network, ignore node and edge visual properties (they are also returned as NETWORK's descendants).
        		continue;
        	}
        		
            Object value = view.getVisualProperty(vp);

            if (value != null && view.isValueLocked(vp)) {
            	lockedProperties.add(vp);
            	continue;
            }
            
            // Use XGMML graphics attribute names for some visual properties
            String key = getGraphicsKey(vp);
            
            if (key != null && value != null && !ignoreGraphicsAttribute(element, key)) {
            	// XGMML graphics attributes...
                if (key.toLowerCase().contains("transparency") && value instanceof Integer) {
                    // Cytoscape's XGMML specifies transparency as between 0-1.0 when it is a <graphics> attribute!
                    float transparency  = ((Integer) value).floatValue();
                    value = transparency / 255;
                } else {
                    value = vp.toSerializableString(value);
                }
                
                if (value != null)
                	writeAttributePair(key, value);
            } else {
            	// No XGMML correspondent--write as inner att tags...
            	key = vp.getIdString();
            	
            	if (!ignoreGraphicsAttribute(element, key))
            		cyProperties.add(vp);
            }
        }
        
		Map<String, String> unrecognizedMap = unrecognizedVisualPropertyMgr
				.getUnrecognizedVisualProperties(networkView, view);

		if (cyProperties.isEmpty() && lockedProperties.isEmpty() && unrecognizedMap.isEmpty()) {
			write("/>\n");
		} else {
			write(">\n");
			depth++;
            
			// write Cy3-specific properties 
			for (VisualProperty vp : cyProperties) {
            	writeVisualPropertyAtt(view, vp);
            }
			
			// also save unrecognized visual properties
            for (Map.Entry<String, String> entry : unrecognizedMap.entrySet()) {
            	String k = entry.getKey();
            	String v = entry.getValue();
            	
            	if (v != null)
            		writeAttributeXML(k, ObjectType.STRING, v, true);
            }
			
            // serialize locked properties as <att> tags inside <graphics>
            if (!lockedProperties.isEmpty()) {
            	writeAttributeXML("lockedVisualProperties", ObjectType.LIST, null, false);
            	depth++;
            	
	            for (VisualProperty vp : lockedProperties) {
	            	writeVisualPropertyAtt(view, vp);
	            }
	            
	            depth--;
	            writeElement("</att>\n");
            }
            
            depth--;
            writeElement("</graphics>\n");
        }

        // TODO: Handle bends
        if (element instanceof CyEdge) {
			//   final CyEdge edge = (CyEdge) element;
			//   final Bend bendData = edge.getBend();
            //   final List<Point2D> handles = new ArrayList<Point2D>(); //final List<Point2D> handles = bendData.getHandles();
            //
            //   if (handles.size() == 0) {
            //       write("/>\n");
            //   } else {
            //       write(">\n");
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

    @SuppressWarnings("unchecked")
	private void writeVisualPropertyAtt(View<? extends CyTableEntry> view, VisualProperty vp) throws IOException {
    	Object value = view.getVisualProperty(vp);
    	value = vp.toSerializableString(value);
    	
    	if (value != null) {
    		writeAttributeXML(vp.getIdString(), ObjectType.STRING, value, true);
    	}
    }
    
	/**
     * Check directionality of edges, return directionality string to use in xml
     * file as attribute of graph element.
     *
     * Set isMixed field true if network is a mixed network (contains directed
     * and undirected edges), and false otherwise (if only one type of edges are
     * present.)
     *
     * @returns flag to use in XGMML file for graph element's 'directed' attribute
     */
    private String getDirectionality() {
        boolean directed = false;

        // Either only directed or mixed -> Use directed as default
        for (CyEdge edge : network.getEdgeList()) {
            if (edge.isDirected()) {
                directed = true;
                break;
            }
        }

        return  AttributeValueUtil.toXGMMLBoolean(directed);
    }
    
    /**
     * Do not use this method with locked visual properties.
	 * @param element
	 * @param attName
	 * @return
	 */
    private boolean ignoreGraphicsAttribute(final CyTableEntry element, String attName) {
    	// If a session format, only those visual properties that belong to the view
    	// (not a visual style) should be saved in the XGMML file.
    	boolean b = (sessionFormat && (element instanceof CyNode) && !attName.matches("x|y|z"));
		b = b || (sessionFormat && (element instanceof CyEdge));
		b = b || (sessionFormat && (element instanceof CyNetwork) && 
				  attName.matches(MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT.getIdString()));
		
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

    private void writeAttributes(CyRow row) throws IOException {
    	if (!sessionFormat) {
    		// If it is a Cy Session XGMML, writing the CyRows would be redundant,
    		// because they are already serialized in .cytable files.
	    	CyTable table = row.getTable();
	
			for (final CyColumn column : table.getColumns())
				writeAttribute(row, column.getName());
    	}
    }
    
    /**
     * Creates an attribute to write into XGMML file.
     *
     * @param row CyRow to load
     * @param attName attribute name
     * @return att Att to return (gets written into xgmml file - CAN BE NULL)
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
     * @throws IOException
     */
    private void writeAttributeXML(String name, ObjectType type, Object value, boolean end) throws IOException {
        if (name == null && type == null)
            writeElement("</att>\n");
        else {
            writeElement("<att");

            if (name != null)
            	writeAttributePair("name", name);
            if (value != null)
            	writeAttributePair("value", value);

            writeAttributePair("type", type);
            
            if (end)
                write("/>\n");
            else
                write(">\n");
        }
    }

    /**
     * Write the string to the output.
     * 
     * @param str
     * @throws IOException
     */
    private void write(String str) throws IOException {
        writer.write(str);
    }
    
    /**
     * writeAttributePair outputs the name,value pairs for an attribute
     *
     * @param name is the name of the attribute we are outputting
     * @param value is the value of the attribute we're outputting
     * @throws IOException
     */
    private void writeAttributePair(String name, Object value) throws IOException {
        write(" " + name + "=" + quote(value.toString()));
    }

    /**
     * writeElement outputs the name,value pairs for an attribute
     *
     * @param line is the element string to output
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

    private String getLabel(CyTableEntry entry) {
        String label = encode(entry.getCyRow().get(CyNetwork.NAME, String.class));
        
        if (label == null || label.isEmpty())
        	label = Long.toString(entry.getSUID());
        
        return label;
    }
    
    private String getLabel(CyNetworkView view) {
    	String label = view.getVisualProperty(MinimalVisualLexicon.NETWORK_TITLE);
        
    	if (label == null || label.isEmpty())
        	label = Long.toString(view.getSUID());
        
		return label;
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
    
    /**
     * @param rootNetwork
     * @return A set with all the sub-networks that are registered in the network manager,
     *         except the base-network, which is the de facto root-network for serialization purposes.
     */
    private Set<CySubNetwork> getRegisteredSubNetworks(CyRootNetwork rootNetwork) {
		List<CySubNetwork> subNetList = rootNetwork.getSubNetworkList();
		Set<CySubNetwork> registeredSubNetSet = new HashSet<CySubNetwork>();
		
		for (CySubNetwork sn : subNetList) {
			if (networkManager.networkExists(sn.getSUID()))
				registeredSubNetSet.add(sn);
		}
		
		return registeredSubNetSet;
	}
}
