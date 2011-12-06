package org.cytoscape.io.internal.read.xgmml.handler;

import static org.cytoscape.io.internal.util.session.SessionUtil.ENTRY_SUID_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.ID_MAPPING_TABLE;
import static org.cytoscape.io.internal.util.session.SessionUtil.INDEX_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_POINTERS_TABLE;
import static org.cytoscape.io.internal.util.session.SessionUtil.ORIGINAL_ID_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.ORIGINAL_NETWORK_ID_COLUMN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

public class ReadDataManager {

	protected final static String XLINK = "http://www.w3.org/1999/xlink";
	
	/* RDF Data */
	protected String RDFDate;
	protected String RDFTitle;
	protected String RDFIdentifier;
	protected String RDFDescription;
	protected String RDFSource;
	protected String RDFType;
	protected String RDFFormat;

	/* Internal lists of the created nodes and edges */
	protected List<CyNode> nodeList;
	protected List<CyEdge> edgeList;
	/* Map of XML ID's to nodes */
	private Map<String, CyNode> nodeIdMap;
	/* Map of XML ID's to edges */
	private Map<String, CyEdge> edgeIdMap;
	/* Map of XML ID's to networks */
	private Map<String, CyNetwork> networkIdMap;
	/* Stack of original network IDs */
	private Stack<String> networkStack;
	
	private Map<CyNetwork, Set<String>> nodeLinkMap;
	private Map<CyNetwork, Set<String>> edgeLinkMap;

	protected CyNode currentNode;
	protected CyEdge currentEdge;

	/* Attribute values */
	protected ParseState attState = ParseState.NONE;
	protected String currentAttributeID;
	protected CyRow currentAttributes;

	/** Edge handle list */
	protected List<String> handleList;
	/** X handle */
	protected String edgeBendX;
	/** Y handle */
	protected String edgeBendY;

	protected List<Object> listAttrHolder;
	
	/** The graph-global directedness, which will be used as default directedness of edges. */
	protected boolean currentNetworkIsDirected = true;

	private Map<String/*att name*/, String/*att value*/> networkAttributes;
	private Map<CyNode, Map<String/*att name*/, String/*att value*/>> nodeGraphics;
	private Map<CyEdge, Map<String/*att name*/, String/*att value*/>> edgeGraphics;
	
	private Map<CyRow, Map<String/*column name*/, String/*equation*/>> equations;

	protected int graphCount;
	protected int graphDoneCount;
	
	private boolean sessionFormat;
	private boolean viewFormat;
	private double documentVersion;
	private CyNetwork currentNetwork;
	private CyRootNetwork parentNetwork;
	private Set<CyNetwork> networks;
	
	// Network view format properties
	private String networkViewId;
	private String networkId;
	private String visualStyleName;
	private String rendererName;
	private String currentElementId; // node/edge/network old id
	private Map<String/*old model id*/, Map<String/*att name*/, String/*att value*/>> viewGraphics;
	private Map<String/*old model id*/, Map<String/*att name*/, String/*att value*/>> viewLockedGraphics;
	
	private final EquationCompiler equationCompiler;
	private final CyTableManager tableManager;
	private final CyNetworkFactory networkFactory;
	private final CyRootNetworkManager rootNetworkManager;
	
	private static final Logger logger = LoggerFactory.getLogger(ReadDataManager.class);

	public ReadDataManager(final EquationCompiler equationCompiler,
						   final CyTableManager tableManager,
						   final CyNetworkFactory networkFactory,
						   final CyRootNetworkManager rootNetworkManager) {
		this.equationCompiler = equationCompiler;
		this.tableManager = tableManager;
		this.networkFactory = networkFactory;
		this.rootNetworkManager = rootNetworkManager;
		init();
	}

	public void init() {
		sessionFormat = false;
		viewFormat = false;
		graphCount = 0;
		graphDoneCount = 0;
		documentVersion = 0;
		
		/* RDF Data */
		RDFDate = null;
		RDFTitle = null;
		RDFIdentifier = null;
		RDFDescription = null;
		RDFSource = null;
		RDFType = null;
		RDFFormat = null;

		nodeList = new ArrayList<CyNode>();
		edgeList = new ArrayList<CyEdge>();

		nodeIdMap = new HashMap<String, CyNode>();
		edgeIdMap = new HashMap<String, CyEdge>();
		networkIdMap = new HashMap<String, CyNetwork>();
		networkStack = new Stack<String>();
		
		nodeLinkMap = new HashMap<CyNetwork, Set<String>>();
		edgeLinkMap = new HashMap<CyNetwork, Set<String>>();

		networkAttributes = new LinkedHashMap<String, String>();
		nodeGraphics = new LinkedHashMap<CyNode, Map<String, String>>();
		edgeGraphics = new LinkedHashMap<CyEdge, Map<String, String>>();
		
		equations = new Hashtable<CyRow, Map<String,String>>();
		
		currentNode = null;
		currentEdge = null;
		currentNetwork = null;
		parentNetwork = null;
		networks = new HashSet<CyNetwork>();
		currentNetworkIsDirected = true;

		attState = ParseState.NONE;
		currentAttributeID = null;
		currentAttributes = null;

		/* Edge handle list */
		handleList = null;

		edgeBendX = null;
		edgeBendY = null;
		
		networkViewId = null;
		networkId = null;
		visualStyleName = null;
		rendererName = null;
		viewGraphics = new LinkedHashMap<String, Map<String,String>>();
		viewLockedGraphics = new LinkedHashMap<String, Map<String,String>>();
	}

	public double getDocumentVersion() {
		return documentVersion;
	}
	
	public void setDocumentVersion(String documentVersion) {
		this.documentVersion = AttributeValueUtil.parseDocumentVersion(documentVersion);
	}

	public boolean isSessionFormat() {
		return sessionFormat;
	}
	
	public void setSessionFormat(boolean sessionFormat) {
		this.sessionFormat = sessionFormat;
	}
	
	public boolean isViewFormat() {
		return viewFormat;
	}

	public void setViewFormat(boolean viewFormat) {
		this.viewFormat = viewFormat;
	}

	public Set<CyNetwork> getNetworks() {
		return networks;
	}
	
	public Set<CyNode> getNodes() {
		return nodeGraphics.keySet();
	}

	public Set<CyEdge> getEdges() {
		return edgeGraphics.keySet();
	}

	/**
	 * @param <T> CyNode or CyEdge
	 * @param element A CyNode or CyEdge
	 * @param attName The name of the attribute
	 * @param attValue The value of the attribute
	 */
	@SuppressWarnings("unchecked")
	protected <T extends CyTableEntry> void addGraphicsAttribute(T element, String attName, String attValue) {
		if (!ignoreGraphicsAttribute(element, attName)) {
			Map<T, Map<String, String>> graphics = null;

			if (element instanceof CyNode) graphics = (Map<T, Map<String, String>>) nodeGraphics;
			else if (element instanceof CyEdge) graphics = (Map<T, Map<String, String>>) edgeGraphics;

			Map<String, String> attributes = graphics.get(element);

			if (attributes == null) {
				attributes = new HashMap<String, String>();
				graphics.put(element, attributes);
			}

			attributes.put(attName, attValue);
		}
	}
	
	/**
	 * Used only when reading Cy3 view format XGMML. Because there is no network yet, we use the old model Id as
	 * mapping key.
	 * @param oldModelId The original ID of the CyNode, CyEdge or CyNetwork.
	 * @param attName
	 * @param attValue
	 * @param locked
	 */
	protected void addViewGraphicsAttribute(String oldModelId, String attName, String attValue, boolean locked) {
		Map<String, Map<String, String>> graphics = locked ? viewLockedGraphics : viewGraphics;
		Map<String, String> attributes = graphics.get(oldModelId);

		if (attributes == null) {
			attributes = new HashMap<String, String>();
			graphics.put(oldModelId, attributes);
		}

		attributes.put(attName, attValue);
	}
	
	protected void addNetworkGraphicsAttribute(String attName, String attValue) {
		if (attName != null) {
			networkAttributes.put(attName, attValue);
		}
	}

	protected void addGraphicsAttributes(CyTableEntry element, Attributes atts) {
		if (element != null) {
			final int attrLength = atts.getLength();

			for (int i = 0; i < attrLength; i++) {
				if (element instanceof CyNode)
					addGraphicsAttribute((CyNode) element, atts.getLocalName(i), atts.getValue(i));
				else if (element instanceof CyEdge)
					addGraphicsAttribute((CyEdge) element, atts.getLocalName(i), atts.getValue(i));
			}
		}
	}
	
	protected void addViewGraphicsAttributes(String oldModelId, Attributes atts, boolean locked) {
		if (oldModelId != null) {
			final int attrLength = atts.getLength();
			
			for (int i = 0; i < attrLength; i++)
				addViewGraphicsAttribute(oldModelId, atts.getLocalName(i), atts.getValue(i), locked);
		}
	}

	public <T extends CyTableEntry> Map<String, String> getGraphicsAttributes(T element) {
		if (element instanceof CyNetwork) return networkAttributes;
		if (element instanceof CyNode) return nodeGraphics.get(element);
		if (element instanceof CyEdge) return edgeGraphics.get(element);

		return null;
	}
	
	public <T extends CyTableEntry> Map<String, String> getViewGraphicsAttributes(String oldId, boolean locked) {
		return locked ? viewLockedGraphics.get(oldId) : viewGraphics.get(oldId);
	}

	public Map<CyNode, Map<String, String>> getNodeGraphics() {
		return nodeGraphics;
	}

	public Map<CyEdge, Map<String, String>> getEdgeGraphics() {
		return edgeGraphics;
	}

	public void setParentNetwork(CyRootNetwork parent) {
		this.parentNetwork = parent;
	}
	
	public CyRootNetwork getParentNetwork() {
		return this.parentNetwork;
	}

	/**
	 * Just stores all the equation strings per CyTableEntry and column name.
	 * It does not create the real Equation objects yet.
	 * @param row The network/node/edge row
	 * @param columnName The name of the column
	 * @param formula The equation formula
	 */
	public void addEquationString(CyRow row, String columnName, String formula) {
		Map<String, String> colEquationMap = equations.get(row);
		
		if (colEquationMap == null) {
			colEquationMap = new HashMap<String, String>();
			equations.put(row, colEquationMap);
		}
		
		colEquationMap.put(columnName, formula);
	}
	
	/**
	 * Should be called only after all XGMML attributes have been read.
	 */
	public void parseAllEquations() {
		for (Map.Entry<CyRow, Map<String, String>> entry : equations.entrySet()) {
			CyRow row = entry.getKey();
			Map<String, String> colEquationMap = entry.getValue();
			
			Map<String, Class<?>> colNameTypeMap = new Hashtable<String, Class<?>>();
			Collection<CyColumn> columns = row.getTable().getColumns();
			
			for (CyColumn col : columns) {
				colNameTypeMap.put(col.getName(), col.getType());
			}
			
			for (Map.Entry<String, String> colEqEntry : colEquationMap.entrySet()) {
				String columnName = colEqEntry.getKey();
				String formula = colEqEntry.getValue();

				if (equationCompiler.compile(formula, colNameTypeMap)) {
					Equation equation = equationCompiler.getEquation();
					row.set(columnName, equation);
				} else {
					logger.error("Error parsing equation \"" + formula + "\": " + equationCompiler.getLastErrorMsg());
				}
			}
		}
	}
	
	protected void setCurrentNetwork(CyNetwork network) {
		this.currentNetwork = network;
	}

	protected CyNetwork getCurrentNetwork() {
		return this.currentNetwork;
	}
	
	protected Stack<String> getNetworkStack() {
		return networkStack;
	}

	protected CyRootNetwork createRootNetwork() {
		final CyNetwork baseNet = networkFactory.createNetwork();
		final CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(baseNet);
		
		return rootNetwork;
	}
	
	protected CyRootNetwork getRootNetwork() {
		if (currentNetwork != null)
			return (currentNetwork instanceof CyRootNetwork) ? (CyRootNetwork) currentNetwork : 
				                                               rootNetworkManager.getRootNetwork(currentNetwork);
		return null;
	}
	
    protected CyNode createNode(String id, String label) {
        if (id == null) id = label;
        CyNode node = null;
        
        if (this.getCurrentNetwork() instanceof CySubNetwork && this.getParentNetwork() != null) {
        	// Do not create the element again if the network is a sub-network!
	        Integer index = this.getIndex(id);
	        
	        if (index != null) {
	        	node = this.getParentNetwork().getNode(index);
	        	((CySubNetwork) this.getCurrentNetwork()).addNode(node);
	        	node = this.getCurrentNetwork().getNode(index); // in order to return the correct instance!
	        }
        }
        
        if (node == null) {
	        // OK, create it
	        node = this.getCurrentNetwork().addNode();
        }
        
        // Add to internal cache
        this.cache(node, id);
        
        return node;
    }

    protected CyEdge createEdge(CyNode source, CyNode target, String id, String label, boolean directed) {
		if (id == null) id = label;
		CyEdge edge = null;
        
        if (this.getCurrentNetwork() instanceof CySubNetwork && this.getParentNetwork() != null) {
        	// Do not create the element again if the network is a sub-network and the edge already exists!
	        Integer index = this.getIndex(id);
	        
	        if (index != null) {
	        	edge = this.getParentNetwork().getEdge(index);
	        	((CySubNetwork) this.getCurrentNetwork()).addEdge(edge);
	        	edge = this.getCurrentNetwork().getEdge(index); // in order to return the correct instance!
	        }
        }
        
        if (edge == null) {
	        // OK, create it
        	CyNetwork net = getCurrentNetwork();
        	// But first get the actual source/target instances from the current network,
        	// because both node instances have to belong to the same root or sub-network.
        	CyNode actualSrc = net.getNode(source.getIndex());
        	CyNode actualTgt = net.getNode(target.getIndex());
        	
        	if (actualSrc == null || actualTgt == null) {
				if ((getDocumentVersion() < 3.0 || !isSessionFormat()) && (net == getRootNetwork().getBaseNetwork())) {
	        		// The nodes might have been added to the root network only, but not the base one in this case,
	        		// because the root-graph element of old and generic XGMML formats are handled as base-network in 3.0
	        		// (3.0 session XGMML has the root-graph as the CyRootNetwork, though).
	        		if (actualSrc == null) {
	        			((CySubNetwork) net).addNode(source);
	        			actualSrc = net.getNode(source.getIndex());
	        		}
	        		if (actualTgt == null) {
	        			((CySubNetwork) net).addNode(target);
	        			actualTgt = net.getNode(target.getIndex());
	        		}
	        	}
        	}
        	
			edge = this.getCurrentNetwork().addEdge(actualSrc, actualTgt, directed);
        }
        
        // Add to internal cache
     	this.cache(edge, id);

		return edge;
	}
	
    protected <T extends CyTableEntry> void cache(T element, String strId) {
    	int index = -1;
    	
    	if (element instanceof CyNode) {
    		nodeIdMap.put(strId, (CyNode) element);
    		nodeList.add((CyNode) element);
    		index = ((CyNode) element).getIndex();
    	} else if (element instanceof CyEdge) {
    		edgeIdMap.put(strId, (CyEdge) element);
    		edgeList.add((CyEdge) element);
    		index = ((CyEdge) element).getIndex();
    	} else if (element instanceof CyNetwork) {
	    	networkIdMap.put(strId, (CyNetwork) element);
	    }
    	
    	// The id mapping is only necessary when loading XGMML from 3.0+ format session.
    	// Should NOT be done with older versions or simple XGMML import.
    	if (this.isSessionFormat()) {
        	this.cache(strId, element.getSUID(), index);
		}
    }
    
	protected void cache(String oldId, long newId, int index) {
		if (oldId != null && !oldId.isEmpty()) {
			CyTable tbl = getIdMappingTable();
			
			if (tbl != null) {
				CyRow row = tbl.getRow(oldId);
				row.set(ENTRY_SUID_COLUMN, newId);
				row.set(INDEX_COLUMN, index);
			} else {
				logger.warn("Cannot find table \"" + ID_MAPPING_TABLE + "\".");
			}
		}
	}
	
	protected void addNetwork(String oldId, CyNetwork net) {
		this.networks.add(net);
	}
	
	protected void addNetworkPointer(Long nodeId, String oldNetworkId) {
		CyTable tbl = getNetworkPointersTable();
		
		if (tbl != null) {
			CyRow row = tbl.getRow(nodeId);
			row.set(ORIGINAL_NETWORK_ID_COLUMN, oldNetworkId);
		} else {
			logger.warn("Cannot find table \"" + NETWORK_POINTERS_TABLE
					+ "\". The node's network pointer will not be restored.");
		}
	}
	
	protected void addElementLink(String href, Class<? extends CyTableEntry> clazz) {
		Map<CyNetwork, Set<String>> map = null;
		String id = AttributeValueUtil.getIdFromXLink(href);
		
		if (clazz == CyNode.class)      map = nodeLinkMap;
		else if (clazz == CyEdge.class) map = edgeLinkMap;
		
		CyNetwork net = getCurrentNetwork();
		
		if (map != null && net != null) {
			Set<String> idSet = map.get(net);
			
			if (idSet == null) {
				idSet = new HashSet<String>();
				map.put(net, idSet);
			}
			
			idSet.add(id);
		}
	}
	
	protected Map<CyNetwork, Set<String>> getNodeLinks() {
		return nodeLinkMap;
	}

	protected Map<CyNetwork, Set<String>> getEdgeLinks() {
		return edgeLinkMap;
	}

	public CyNetwork getNetwork(String oldId) {
		return networkIdMap.get(oldId);
	}
	
	public CyNode getNode(String oldId) {
		return nodeIdMap.get(oldId);
	}
	
	public CyEdge getEdge(String oldId) {
		return edgeIdMap.get(oldId);
	}
	
	protected void cache(String oldId, long newId) {
		cache(oldId, newId, 0);
	}
	
	protected String getNetworkViewId() {
		return networkViewId;
	}

	protected void setNetworkViewId(String networkViewId) {
		this.networkViewId = networkViewId;
	}

	public String getNetworkId() {
		return networkId;
	}

	protected void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getVisualStyleName() {
		return visualStyleName;
	}

	protected void setVisualStyleName(String visualStyleName) {
		this.visualStyleName = visualStyleName;
	}

	public String getRendererName() {
		return rendererName;
	}

	protected void setRendererName(String rendererName) {
		this.rendererName = rendererName;
	}
	
	protected String getCurrentElementId() {
		return currentElementId;
	}

	protected void setCurrentElementId(String currentElementId) {
		this.currentElementId = currentElementId;
	}

	public Integer getIndex(String oldId) {
		CyRow row = oldId != null ? getIdMappingTable().getRow(oldId) : null;
		
		if (row != null)
			return row.get(INDEX_COLUMN, Integer.class);
		
		return null;
	}
	
	public Long getSUID(String oldId) {
		CyRow row = oldId != null ? getIdMappingTable().getRow(oldId) : null;
		
		if (row != null)
			return row.get(ENTRY_SUID_COLUMN, Long.class);
		
		return null;
	}
	
	public String getOldId(Long suid) {
		Collection<CyRow> rows = suid != null ? getIdMappingTable().getMatchingRows(ENTRY_SUID_COLUMN, suid) : null;
		
		if (rows != null) {
			for (CyRow row : rows)
				return row.get(ORIGINAL_ID_COLUMN, String.class);
		}
		
		return null;
	}

	private CyTable getIdMappingTable() {
		CyTable tbl = null;
		
		if (SessionUtil.getIdMappingTableSUID() != null)
			tbl = tableManager.getTable(SessionUtil.getIdMappingTableSUID());
		
		return tbl;
	}
	
	private CyTable getNetworkPointersTable() {
		CyTable tbl = null;
		
		if (SessionUtil.getNetworkPointersTableSUID() != null)
			tbl = tableManager.getTable(SessionUtil.getNetworkPointersTableSUID());
		
		return tbl;
	}
	
	/**
	 * It controls which graphics attributes should be parsed.
	 * @param element The network, node or edge
	 * @param attName The name of the XGMML attribute
	 * @return
	 */
	private boolean ignoreGraphicsAttribute(final CyTableEntry element, String attName) {
		boolean b = false;
		
		// When reading XGMML as part of a CYS file, these graphics attributes should not be parsed.
		if (sessionFormat && element != null && attName != null) {
			// Network
			b = b || (element instanceof CyNetwork && attName.matches("backgroundColor"));
			// Nodes or Edges (these are standard XGMML and 2.x <graphics> attributes, not 3.0 bypass properties)
			b = b ||
				((element instanceof CyNode || element instanceof CyEdge) && attName
						.matches("type|fill|w|h|size|width|outline|"
								 + "(cy:)?((node|edge)Transparency|(node|edge)LabelFont|(border|edge)LineType)|"
								 + "(cy:)?(source|target)Arrow(Color)?"));
		}

		return b;
	}
}
