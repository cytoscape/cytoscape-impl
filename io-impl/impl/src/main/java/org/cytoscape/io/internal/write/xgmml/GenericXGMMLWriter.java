package org.cytoscape.io.internal.write.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * This writer serializes CyNetworks and CyNetworkViews as standard XGMML files.
 */
public class GenericXGMMLWriter extends AbstractTask implements CyWriter {

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

    protected final OutputStream outputStream;
    protected final CyNetwork network;
    protected final CyRootNetwork rootNetwork;
    protected Set<CySubNetwork> subNetworks;
    protected CyNetworkView networkView;
    protected VisualStyle visualStyle;
    protected final VisualLexicon visualLexicon;
    protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
    protected final CyNetworkManager networkMgr;
    protected final CyRootNetworkManager rootNetworkMgr;
    protected final RenderingEngineManager renderingEngineMgr;
    private final GroupUtil groupUtil;

    protected final Map<CyNode, CyNode> writtenNodeMap = new WeakHashMap<CyNode, CyNode>();
    protected final Map<CyEdge, CyEdge> writtenEdgeMap = new WeakHashMap<CyEdge, CyEdge>();
    protected final Map<CyNetwork, CyNetwork> writtenNetMap = new WeakHashMap<CyNetwork, CyNetwork>();

    protected int depth = 0;
    private String indentString = "";
    private Writer writer;

    private boolean doFullEncoding;
    
    final static private Logger logger = LoggerFactory.getLogger(GenericXGMMLWriter.class);

    public GenericXGMMLWriter(final OutputStream outputStream,
                              final RenderingEngineManager renderingEngineMgr,
                              final CyNetworkView networkView,
                              final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
                              final CyNetworkManager networkMgr,
                              final CyRootNetworkManager rootNetworkMgr,
                              final VisualMappingManager vmMgr,
                              final GroupUtil groupUtil) {
        this(outputStream, renderingEngineMgr, networkView.getModel(), unrecognizedVisualPropertyMgr, networkMgr,
                rootNetworkMgr, groupUtil);
        this.networkView = networkView;
        
        setVisualStyle(vmMgr.getVisualStyle(networkView));
    }
    
    public GenericXGMMLWriter(final OutputStream outputStream,
                              final RenderingEngineManager renderingEngineMgr,
                              final CyNetwork network,
                              final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
                              final CyNetworkManager networkMgr,
                              final CyRootNetworkManager rootNetworkMgr,
                              final GroupUtil groupUtil) {
        this.outputStream = outputStream;
        this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
        this.networkMgr = networkMgr;
        this.rootNetworkMgr = rootNetworkMgr;
        this.renderingEngineMgr = renderingEngineMgr;
        this.visualLexicon = renderingEngineMgr.getDefaultVisualLexicon();
        this.groupUtil = groupUtil;
        
        if (network instanceof CyRootNetwork) {
            this.network = this.rootNetwork = (CyRootNetwork) network;
            this.subNetworks = getSerializableSubNetworks(rootNetwork);
        } else {
            this.network = network;
            this.rootNetwork = rootNetworkMgr.getRootNetwork(network);
            this.subNetworks = new HashSet<CySubNetwork>();
        }
        
        // Create our indent string (480 blanks);
        for (int i = 0; i < 20; i++)
            indentString += "                        ";
        
        doFullEncoding = Boolean.valueOf(System.getProperty(ENCODE_PROPERTY, "true"));
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        taskMonitor.setProgress(0.0);
        init(taskMonitor);

        writeRootElement();
        taskMonitor.setProgress(0.2);
        depth++;
        
        writeMetadata();
        taskMonitor.setProgress(0.3);
        
        writeRootGraphAttributes();
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

        // Clean up the group stuff a little
        if (groupUtil != null) {
            groupUtil.groupsSerialized(Collections.singletonList(network), null);
        }
    }
    
    protected void init(TaskMonitor tm) {
        writer = new OutputStreamWriter(outputStream);
        prepareGroupsForSerialization();
    }
    
    /**
     * Necessary only when exporting to XGMML.
     */
    protected void prepareGroupsForSerialization() {
        // Don't do this when saving the session-type XGMML files
        if (groupUtil != null) {
            groupUtil.prepareGroupsForSerialization(Collections.singletonList(network));
        }
    }

    /**
     * Output the XML preamble.  This includes the XML line as well as the initial
     * &lt;graph&gt; element, along with all of our namespaces.
     *
     * @throws IOException
     */
    protected void writeRootElement() throws IOException {
        writeElement(XML_STRING + "\n");
        writeElement("<graph");
        
        writeRootElementAtributes();
        writeAttributePair(DOCUMENT_VERSION_NAME, VERSION);
        
        for (int ns = 0; ns < NAMESPACES.length; ns++)
            write(" " + NAMESPACES[ns]);
        
        write(">\n");
        
        writtenNetMap.put(network, network);
    }
    
    protected void writeRootElementAtributes() throws IOException {
        writeAttributePair("id", network.getSUID());
        
        String label = networkView != null ? getLabel(networkView) : getLabel(network, network);
        writeAttributePair("label", label);
        
        writeAttributePair("directed", getDirectionality());
    }

    /**
     * Output the network metadata.  This includes our format version and our RDF data.
     * @throws IOException
     */
    protected void writeMetadata() throws IOException {
        writeElement("<att name=\"networkMetadata\">\n");
        depth++;
        
        // Write RDF
        String title = networkView != null ? getLabel(networkView) : getLabel(network, network);
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
        
        depth--;
        writeElement("</att>\n");
    }

    /**
     * Output any network attributes we have defined, including
     * the network graphics information we encode as attributes:
     * backgroundColor, zoom, and the graph center.
     *
     * @throws IOException
     */
    protected void writeRootGraphAttributes() throws IOException {
        writeAttributes(network.getRow(network));
        writeAttributes(network.getRow(network, CyNetwork.HIDDEN_ATTRS));
        writeGraphics(networkView, false);
    }

    protected void writeSubGraph(final CyNetwork net) throws IOException {
        if (net == null)
            return;
        
        if (writtenNetMap.containsKey(net)) {
            // This sub-network has already been written
            writeSubGraphReference(net);
        } else {
            // Check if this network is from the same root network as the base network
            final CyRootNetwork otherRoot = rootNetworkMgr.getRootNetwork(net);
            boolean sameRoot = rootNetwork.equals(otherRoot);
            
            if (sameRoot) {
                // Write it for the first time
                writtenNetMap.put(net, net);
                
                writeElement("<att>\n");
                depth++;
                writeElement("<graph");
                // Always write the network ID
                writeAttributePair("id", net.getSUID());
                // Save the label to make it more human readable
                writeAttributePair("label", getLabel(net, net));
                writeAttributePair("cy:registered", ObjectTypeMap.toXGMMLBoolean(isRegistered(net)));
                write(">\n");
                depth++;
        
                writeAttributes(net.getRow(net));
                writeAttributes(net.getRow(net, CyNetwork.HIDDEN_ATTRS));

                for (CyNode childNode : net.getNodeList())
                    writeNode(net, childNode);
                for (CyEdge childEdge : net.getEdgeList())
                    writeEdge(net, childEdge);
        
                depth--;
                writeElement("</graph>\n");
                depth--;
                writeElement("</att>\n");
            }
        }
    }
    
    protected void writeSubGraphReference(CyNetwork net) throws IOException {
        if (net == null)
            return;
        
        String href = "#" + net.getSUID();
        final CyRootNetwork otherRoot = rootNetworkMgr.getRootNetwork(net);
        final boolean sameRoot = rootNetwork.equals(otherRoot);
        
        if (!sameRoot) {
            // This network belongs to another XGMML file,
            // so add the other root-network's file name to the XLink URI
            final String fileName = SessionUtil.getXGMMLFilename(otherRoot);
            href = fileName + href;
        }
        
        writeElement("<att>\n");
        depth++;
        writeElement("<graph");
        writeAttributePair("xlink:href", href);
        write("/>\n");
        depth--;
        writeElement("</att>\n");
    }

    /**
     * Output Cytoscape nodes as XGMML
     * @throws IOException
     */
    protected void writeNodes() throws IOException {
        List<CyNode> pointerNodes = new ArrayList<CyNode>();

        for (CyNode node : network.getNodeList()) {
            // Save all of the nodes with network pointers until last
            // this allows us to have embedded networks...
            if (node.getNetworkPointer() != null) {
                pointerNodes.add(node);
                continue;
            }

            // Only if not already written inside a nested graph
            if (!writtenNodeMap.containsKey(node))
                writeNode(network, node);
        }

        for (CyNode node : pointerNodes) {
            if (!writtenNodeMap.containsKey(node))
                writeNode(network, node);
        }

        // Now, output nodes for expanded groups.  We'll clean
        // these up on import...
        if (groupUtil != null) {
            for (CyNode node : groupUtil.getExpandedGroups(network)) {
                if (!writtenNodeMap.containsKey(node))
                    writeNode(network, node);
            }
        }
    }
    
    /**
     * Output Cytoscape edges as XGMML
     * @throws IOException
     */
    protected void writeEdges() throws IOException {
        for (CyEdge edge : network.getEdgeList()) {
            // Only if not already written inside a nested graph
            if (!writtenEdgeMap.containsKey(edge))
                writeEdge(network, edge);
        }

        // Now, output hidden edges groups.  
        // For collapsed groups, we need to output external edges,
        // for expanded groups, we need to output edges to the group node
        if (groupUtil != null) {
            // Handle edges to the group node
            for (CyEdge edge : groupUtil.getGroupNodeEdges(network)) {
                if (!writtenEdgeMap.containsKey(edge))
                    writeEdge(network, edge);
            }
            // Now handle external edges
            for (CyEdge edge : groupUtil.getExternalEdges(network)) {
                if (!writtenEdgeMap.containsKey(edge))
                    writeEdge(network, edge);
            }
        }
    }

    /**
     * Output a single CyNode as XGMML
     *
     * @param node the node to output
     * @throws IOException
     */
    protected void writeNode(final CyNetwork net, final CyNode node) throws IOException {
        boolean written = writtenNodeMap.containsKey(node);

        // Output the node
        writeElement("<node");
        
        if (written) {
            // Write as an XLink only
            writeAttributePair("xlink:href", "#" + node.getSUID());
            write("/>\n");
        } else {
            // Remember that we've wrote this node
             writtenNodeMap.put(node, node);
            
            // Write the actual node with its properties
            writeAttributePair("id", node.getSUID());
            writeAttributePair("label", getLabel(net, node));
            write(">\n");
            depth++;
            
            // Output the node attributes
            writeAttributes(net.getRow(node));
            writeAttributes(net.getRow(node, CyNetwork.HIDDEN_ATTRS));
            
            // Write node's sub-graph
            final CyNetwork netPointer = node.getNetworkPointer();
            
            if (netPointer != null && isSerializable(netPointer))
                writeSubGraph(netPointer);
            
            // Output the node graphics if we have a view
            if (networkView != null && network.containsNode(node))
                writeGraphics(networkView.getNodeView(node), false);
            
            depth--;
            writeElement("</node>\n");
        }
    }
    
    /**
     * Output a Cytoscape edge as XGMML
     *
     * @param edge the edge to output
     * @throws IOException
     */
    protected void writeEdge(final CyNetwork net, final CyEdge edge) throws IOException {
        writeElement("<edge");
        final boolean written = writtenEdgeMap.containsKey(edge);
        
        if (written) {
            // Write as an XLink only
            writeAttributePair("xlink:href", "#" + edge.getSUID());
            write("/>\n");
        } else {
            // Remember that we've wrote this edge
            writtenEdgeMap.put(edge, edge);
            
            writeAttributePair("id", edge.getSUID());
            writeAttributePair("label", getLabel(net, edge));
            writeAttributePair("source", edge.getSource().getSUID());
            writeAttributePair("target", edge.getTarget().getSUID());
            writeAttributePair("cy:directed",  ObjectTypeMap.toXGMMLBoolean(edge.isDirected()));
            
            write(">\n");
            depth++;

            // Write the edge attributes
            writeAttributes(getRowFromNetOrRoot(net, edge, null));
            writeAttributes(getRowFromNetOrRoot(net, edge, CyNetwork.HIDDEN_ATTRS));
    
            // Write the edge graphics
            if (networkView != null)
                writeGraphics(networkView.getEdgeView(edge), false);

            depth--;
            writeElement("</edge>\n");
        }
    }
    
    /**
     * Writes a graphics tag under graph, node, edge.
     * @param view
     * @param groupLockedProperties Whether or not locked visual properties must be grouped under a list-type att tag.
     * @throws IOException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void writeGraphics(View<? extends CyIdentifiable> view, final boolean groupLockedProperties)
            throws IOException {
        if (view == null)
            return;
        
        writeElement("<graphics");
        
        CyIdentifiable element = view.getModel();
        final VisualProperty<?> root;
        
        if (element instanceof CyNode)
            root = BasicVisualLexicon.NODE;
        else if (element instanceof CyEdge)
            root = BasicVisualLexicon.EDGE;
        else
            root = BasicVisualLexicon.NETWORK;
        
        final Collection<VisualProperty<?>> visualProperties = visualLexicon.getAllDescendants(root);
        final List<VisualProperty<?>> attProperties = new ArrayList<VisualProperty<?>>(); // To be written as att tags
        final List<VisualProperty<?>> lockedProperties = new ArrayList<VisualProperty<?>>();
        final Set<String> writtenKeys = new HashSet<String>();
        
        for (VisualProperty vp : visualProperties) {
            // If network, ignore node and edge visual properties,
            // because they are also returned as NETWORK's descendants
            if (root == BasicVisualLexicon.NETWORK && vp.getTargetDataType() != CyNetwork.class)
                continue;
            
            // It doesn't have to write the property if the value is null
            Object value = view.getVisualProperty(vp);
            
            if (value == null)
                continue;
            
            if (groupLockedProperties && view.isDirectlyLocked(vp)) {
                lockedProperties.add(vp);
                continue;
            } else {
                // If not a bypass, write only leaf nodes
                final VisualLexiconNode node = visualLexicon.getVisualLexiconNode(vp);

                if (!node.getChildren().isEmpty())
                    continue;
            }
            
            // Use XGMML graphics attribute names for some visual properties
            final String[] keys = getGraphicsKey(vp);
            
            if (keys != null && keys.length > 0) {
                // XGMML graphics attributes...
                value = vp.toSerializableString(value);
                
                if (value != null) {
                    for (int i = 0; i < keys.length; i++) {
                        final String k = keys[i];
                        
                        if (!writtenKeys.contains(k) && !ignoreGraphicsAttribute(element, k)) {
                            writeAttributePair(k, value);
                            writtenKeys.add(k); // to avoid writing the same key twice, because of dependencies!
                        }
                    }
                }
            } else if (!ignoreGraphicsAttribute(element, vp.getIdString())) {
                // So it can be written as nested att tags
                attProperties.add(vp);
            }
        }
        
        Map<String, String> unrecognizedMap = unrecognizedVisualPropertyMgr
                .getUnrecognizedVisualProperties(networkView, view);

        if (attProperties.isEmpty() && lockedProperties.isEmpty() && unrecognizedMap.isEmpty()) {
            write("/>\n");
        } else {
            write(">\n");
            depth++;
            
            // write Cy3-specific properties 
            for (VisualProperty vp : attProperties) {
                writeVisualPropertyAtt(view, vp);
            }
            
            // also save unrecognized visual properties
            for (Map.Entry<String, String> entry : unrecognizedMap.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                
                if (v != null)
                    writeAttributeXML(k, ObjectType.STRING, v, false, true);
            }
            
            // serialize locked properties as <att> tags inside <graphics>
            if (!lockedProperties.isEmpty()) {
                writeAttributeXML("lockedVisualProperties", ObjectType.LIST, null, false, false);
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
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void writeVisualPropertyAtt(View<? extends CyIdentifiable> view, VisualProperty vp) throws IOException {
    	Object value = view.getVisualProperty(vp);
    	
    	try {
	        value = vp.toSerializableString(value);
    	} catch (final ClassCastException e) {
        	logger.error("Error getting serializable string of Visual Property \"" + vp.getIdString() + "\" (value: " +
        			value + ")", e);
        	return;
        }
	        
        if (value != null)
            writeAttributeXML(vp.getIdString(), ObjectType.STRING, value, false, true);
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

        return  ObjectTypeMap.toXGMMLBoolean(directed);
    }
    
    /**
     * Do not use this method with locked visual properties.
     * @param element
     * @param attName
     * @return
     */
    protected boolean ignoreGraphicsAttribute(final CyIdentifiable element, String attName) {
        return false;
    }
    
    private String[] getGraphicsKey(VisualProperty<?> vp) {
        //Nodes
        if (vp.equals(BasicVisualLexicon.NODE_X_LOCATION)) return new String[]{"x"};
        if (vp.equals(BasicVisualLexicon.NODE_Y_LOCATION)) return new String[]{"y"};
        if (vp.equals(BasicVisualLexicon.NODE_Z_LOCATION)) return new String[]{"z"};
        if (vp.equals(BasicVisualLexicon.NODE_SIZE)) return new String[]{"w", "h"};
        if (vp.equals(BasicVisualLexicon.NODE_WIDTH)) return new String[]{"w"};
        if (vp.equals(BasicVisualLexicon.NODE_HEIGHT)) return new String[]{"h"};
        if (vp.equals(BasicVisualLexicon.NODE_FILL_COLOR)) return new String[]{"fill"};
        if (vp.equals(BasicVisualLexicon.NODE_SHAPE)) return new String[]{"type"};
        if (vp.equals(BasicVisualLexicon.NODE_BORDER_WIDTH)) return new String[]{"width"};
        if (vp.equals(BasicVisualLexicon.NODE_BORDER_PAINT)) return new String[]{"outline"};

        // Edges
        if (vp.equals(BasicVisualLexicon.EDGE_WIDTH)) return new String[]{"width"};
        if (vp.equals(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)) return new String[]{"fill"};
        
        // TODO: also write these attributes to keep compatibility with 2.x an Cytoscape Web (?):
        // cy:nodeTransparency cy:nodeLabelFont cy:nodeLabel cy:borderLineType
        // cy:sourceArrow cy:targetArrow cy:sourceArrowColor cy:targetArrowColor cy:edgeLabelFont cy:edgeLabel cy:edgeLineType cy:curved

        return new String[]{};
    }
    
    protected void writeAttributes(final CyRow row) throws IOException {
        if (row != null) {
	        final CyTable table = row.getTable();
	
	        for (final CyColumn column : table.getColumns()) {
	            if (!CyIdentifiable.SUID.equals(column.getName()))
	                writeAttribute(row, column.getName());
	        }
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
    protected void writeAttribute(final CyRow row, final String attName) throws IOException {
        // create an attribute and its type:
        final CyTable table = row.getTable();
        final CyColumn column = table.getColumn(attName);
        
        if (column == null)
            return;
        
        final boolean hidden = !table.isPublic();
        final Class<?> attType = column.getType();

        if (attType == Double.class) {
            Double dAttr = row.get(attName, Double.class);
            writeAttributeXML(attName, ObjectType.REAL, dAttr, hidden, true);
        } else if (attType == Integer.class) {
            Integer iAttr = row.get(attName, Integer.class);
            writeAttributeXML(attName, ObjectType.INTEGER, iAttr, hidden, true);
        } else if (attType == Long.class) {
            Long lAttr = row.get(attName, Long.class);
            writeAttributeXML(attName, ObjectType.REAL, lAttr, hidden, true);
        } else if (attType == String.class) {
            String sAttr = row.get(attName, String.class);
            // Protect tabs and returns
            if (sAttr != null) {
                sAttr = sAttr.replace("\n", "\\n");
                sAttr = sAttr.replace("\t", "\\t");
            }

            writeAttributeXML(attName, ObjectType.STRING, sAttr, hidden, true);
        } else if (attType == Boolean.class) {
            Boolean bAttr = row.get(attName, Boolean.class);
            writeAttributeXML(attName, ObjectType.BOOLEAN, ObjectTypeMap.toXGMMLBoolean(bAttr), hidden, true);
        } else if (attType == List.class) {
            final List<?> listAttr = row.getList(attName, column.getListElementType());
            final ObjectType elementType = getObjectType(column.getListElementType());
            
            writeAttributeXML(attName, ObjectType.LIST, elementType, null, hidden, false);

            if (listAttr != null) {
                depth++;
                // iterate through the list
                for (Object obj : listAttr) {
                    String sAttr = null;
                    
                    if (obj instanceof Boolean) {
                        sAttr = ObjectTypeMap.toXGMMLBoolean((Boolean) obj);
                    } else {
                        // Protect tabs and returns (if necessary)
                        sAttr = obj.toString();
                        if (sAttr != null) {
                            sAttr = sAttr.replace("\n", "\\n");
                            sAttr = sAttr.replace("\t", "\\t");
                        }
                    }
                    // set child attribute value & label
                    writeAttributeXML(attName, getObjectType(obj), sAttr, hidden, true);
                }
                depth--;
            }
            
            writeAttributeXML(null, null, null, hidden, true);
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
    protected void writeAttributeXML(String name, ObjectType type, Object value, boolean hidden, boolean end)
    		throws IOException {
    	writeAttributeXML(name, type, null, value, hidden, end);
    }
    
	/**
     * writeAttributeXML outputs an XGMML attribute
     *
     * @param name is the name of the attribute we are outputting
     * @param type is the XGMML type of the attribute
     * @param listType is the XGMML type of the list elements
     * @param value is the value of the attribute we're outputting
     * @param end is a flag to tell us if the attribute should include a tag end
     * @throws IOException
     */
    protected void writeAttributeXML(String name, ObjectType type, ObjectType listType, Object value, boolean hidden, 
    		boolean end) throws IOException {
        if (name == null && type == null)
            writeElement("</att>\n");
        else {
            writeElement("<att");

            if (name != null)
                writeAttributePair("name", name);
            if (value != null)
                writeAttributePair("value", value);

            writeAttributePair("type", type);
            
            if (type == ObjectType.LIST && listType != null)
            	writeAttributePair("cy:elementType", listType); // Since Cytoscape v3.3
            
            if (hidden)
                writeAttributePair("cy:hidden", ObjectTypeMap.toXGMMLBoolean(hidden));
            
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
    protected void write(String str) throws IOException {
        writer.write(str);
    }
    
    /**
     * writeAttributePair outputs the name,value pairs for an attribute
     *
     * @param name is the name of the attribute we are outputting
     * @param value is the value of the attribute we're outputting
     * @throws IOException
     */
    protected void writeAttributePair(String name, Object value) throws IOException {
        write(" " + name + "=" + quote(value.toString()));
    }

    /**
     * writeElement outputs the name,value pairs for an attribute
     *
     * @param line is the element string to output
     * @throws IOException
     */
    protected void writeElement(String line) throws IOException {
        while (depth * 2 > indentString.length() - 1)
            indentString = indentString + "                        ";
        writer.write(indentString, 0, depth * 2);
        writer.write(line);
    }

    /**
     * Return the type of an attribute value.
     */
    private ObjectType getObjectType(final Object obj) {
        return getObjectType(obj.getClass());
    }
    
    /**
     * Return the type corresponding to a class.
     */
    private ObjectType getObjectType(final Class<?> type) {
        if (type == String.class)
            return ObjectType.STRING;
        else if (type == Integer.class)
            return ObjectType.INTEGER;
        else if (type == Double.class || type == Float.class || type == Long.class)
            return ObjectType.REAL;
        else if (type == Boolean.class)
            return ObjectType.BOOLEAN;
        else if (type == List.class)
            return ObjectType.LIST;

        return null;
    }

    protected String getLabel(final CyNetwork network, final CyIdentifiable entry) {
        String label = encode(getRowFromNetOrRoot(network, entry, null).get(CyNetwork.NAME, String.class));
        
        if (label == null || label.isEmpty())
            label = Long.toString(entry.getSUID());
        
        return label;
    }
    
    protected String getLabel(final CyNetworkView view) {
        String label = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
        
        if (label == null || label.isEmpty())
            label = Long.toString(view.getSUID());
        
        return label;
    }

    protected CyRow getRowFromNetOrRoot(final CyNetwork network, final CyIdentifiable entry, String namespace) {
        CyRow row = null;
        
        try {
            row = namespace == null ? network.getRow(entry) : network.getRow(entry, namespace);
        } catch (final IllegalArgumentException e) {
        	// Ignore this exception
        } catch (final RuntimeException e) {
        	logger.error("Cannot get \"" + namespace +"\" row for entry \"" + entry + "\" in network \"" + network + 
        			"\".", e);
        }
        
        if (row == null && network instanceof CySubNetwork) {
        	// Doesn't exist in subnetwork? Try to get it from the root network.
            final CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
            row = namespace == null ? root.getRow(entry) : root.getRow(entry, namespace);
        }
        
        return row;
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
     * Used when saving the view-type XGMML. 
     * @param visualStyleName
     */
    private void setVisualStyle(final VisualStyle visualStyle) {
        this.visualStyle = visualStyle;
    }
    
    /**
     * @param rootNet
     * @return A set with all the subnetworks that should be serialized.
     */
    protected Set<CySubNetwork> getSerializableSubNetworks(final CyRootNetwork rootNet) {
        final Set<CySubNetwork> serializableSet = new LinkedHashSet<CySubNetwork>();
        final List<CySubNetwork> subNetList = rootNet.getSubNetworkList();
        final CySubNetwork baseNetwork = rootNet.getBaseNetwork();
        
        // The base network must be the first one!
        if (isSerializable(baseNetwork))
            serializableSet.add(baseNetwork);
        
        for (final CySubNetwork sn : subNetList) {
            if (isSerializable(sn))
                serializableSet.add(sn);
        }
        
        return serializableSet;
    }
    
    protected boolean isSerializable(final CyNetwork net) {
        return net.getSavePolicy() != SavePolicy.DO_NOT_SAVE;
    }
    
	protected boolean isRegistered(final CyNetwork net) {
        return networkMgr.networkExists(net.getSUID());
    }
}
