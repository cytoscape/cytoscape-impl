package org.cytoscape.biopax.internal.action;

import static org.cytoscape.biopax.internal.BioPaxMapper.BIOPAX_ENTITY_TYPE;

import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.biopax.internal.BioPaxMapper;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.biopax.internal.view.BioPaxContainer;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;


/**
 * Listens for Network Events, and takes appropriate Actions.
 * May be subclassed.
 *
 * @author Ethan Cerami, Gary Bader, Chris Sander, Benjamin Gross, Igor Rodchenkov.
 */
public class BioPaxViewTracker implements NetworkViewAddedListener,
	NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, RowsSetListener {
	
	private final BioPaxDetailsPanel bpPanel;
	private final BioPaxContainer bpContainer;
	private final CyApplicationManager cyApplicationManager;
	private final VisualMappingManager visualMappingManager;
	private final BioPaxVisualStyleUtil bioPaxVisualStyleUtil;

	/**
	 * Constructor.
	 *
	 * @param bpPanel BioPaxDetails Panel Object.
	 */
	public BioPaxViewTracker(BioPaxDetailsPanel bpPanel, 
			BioPaxContainer bpContainer, 
			CyApplicationManager cyApplicationManager,
			VisualMappingManager visualMappingManager,
			BioPaxVisualStyleUtil bioPaxVisualStyleUtil) 
	{
		this.bpPanel = bpPanel;
		this.bpContainer = bpContainer;
		this.cyApplicationManager = cyApplicationManager;
		this.visualMappingManager = visualMappingManager;
		this.bioPaxVisualStyleUtil = bioPaxVisualStyleUtil;
	}


	/**
	 * Network Created Event
	 */
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {	
		final CyNetworkView view = e.getNetworkView();
		if(isBioPAXNetwork(view.getModel())) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					bpContainer.showLegend();
					bpPanel.resetText();
					
					// apply BioPAX visual style and set tool tips
					setNodeToolTips(view);
					VisualStyle bioPaxVisualStyle = bioPaxVisualStyleUtil.getBioPaxVisualStyle();
					visualMappingManager.setVisualStyle(bioPaxVisualStyle, view);
					bioPaxVisualStyle.apply(view);
					view.updateView();
				}
			});
		}
	}

	/**
	 * Network Focus Event.
	 */
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView view = e.getNetworkView();
		
		// update bpPanel accordingly
       	if (view != null && isBioPAXNetwork(view.getModel())) {
       		SwingUtilities.invokeLater(new Runnable() {
       			@Override
       			public void run() {
       	            bpPanel.resetText();
       			}
       		});
        }
	}


	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		if (isBioPAXNetwork(e.getNetworkView().getModel())) {
			//TODO nothing?
		}
	}


	@Override
	public void handleEvent(RowsSetEvent e) {
		CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
		if(view == null) return;
		
		final CyNetwork network = view.getModel();
		if (isBioPAXNetwork(network)) {

			if (!network.getDefaultNodeTable().equals(e.getSource()))
				return;

			try {
				CyNode selected = null;
				for (CyNode node : network.getNodeList()) {
					if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
						selected = node;
						break;
					}
				}

				if (selected != null) {
					final CyNode node = selected;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// Show the details
							bpPanel.showDetails(network, node);
							// If legend is showing, show details
							bpContainer.showDetails();
						}
					});
				}
			} finally {
				// update custom nodes
				customNodes(view);
			}
		}
	}

	
	private void setNodeToolTips(CyNetworkView networkView) {
		// iterate through the nodes
		CyNetwork network = networkView.getModel();
		for (CyNode node : network.getNodeList()) {
			CyRow row = network.getRow(node);
			String tip = row.get(BIOPAX_ENTITY_TYPE, String.class) + "\n"
					+ row.get("/cellularLocation", String.class);
			View<CyNode> nodeView = networkView.getNodeView(node);
			nodeView.setLockedValue(BasicVisualLexicon.NODE_TOOLTIP, tip);
		}
	}
	
	
	private static void customNodes(CyNetworkView networkView) {
		// grab node attributes
		CyNetwork cyNetwork = networkView.getModel();

		// iterate through the nodes
		Iterator<CyNode> nodesIt = cyNetwork.getNodeList().iterator();
		if (nodesIt.hasNext()) {
			// grab the node
			CyNode node = nodesIt.next();

			// get chemical modifications
			int count = 0;
			boolean isPhosphorylated = false;
			// TODO: MultiHashMap
//			MultiHashMapDefinition mhmdef = nodeAttributes.getMultiHashMapDefinition();
//
//			if (mhmdef.getAttributeValueType(BIOPAX_CHEMICAL_MODIFICATIONS_MAP) != -1) {
//				MultiHashMap mhmap = nodeAttributes.getMultiHashMap();
//				CountedIterator modsIt = mhmap.getAttributeKeyspan(node.getIdentifier(),
//                               BIOPAX_CHEMICAL_MODIFICATIONS_MAP, null);
//
//				// do we have phosphorylation ?
//				while (modsIt.hasNext()) {
//					String modification = (String) modsIt.next();
//
//					if (modification.equals(BioPaxUtil.PHOSPHORYLATION_SITE)) {
//						isPhosphorylated = true;
//
//						Object[] key = { BioPaxUtil.PHOSPHORYLATION_SITE };
//						String countStr = (String) mhmap.getAttributeValue(node.getIdentifier(),
//                            BIOPAX_CHEMICAL_MODIFICATIONS_MAP, key);
//						count = ((Integer) Integer.valueOf(countStr)).intValue();
//
//						break;
//					}
//				}
//			}

			// if phosphorylated, add custom node
			if (isPhosphorylated) {
				addCustomShapes(networkView, node, "PHOSPHORYLATION_GRAPHICS", count);
			}
		}
	}


	/**
	 * Based on given arguments, adds proper custom node shape to node.
	 */
	private static void addCustomShapes(CyNetworkView networkView, CyNode node, String shapeType,
	                                    int modificationCount) {
		// TODO: Custom graphics
//		// create refs to help views
//		CyNetwork cyNetwork = networkView.getModel();
//		View<CyNode> nodeView = networkView.getNodeView(node);
//		DNodeView dingNodeView = (DNodeView) nodeView;
//
//		// remove existing custom nodes
//		Iterator<CustomGraphic> it = dingNodeView.customGraphicIterator();
//		while ( it.hasNext() ) {
//			dingNodeView.removeCustomGraphic( it.next() );
//		}
//
//		for (int lc = 0; lc < modificationCount; lc++) {
//			// set image
//			BufferedImage image = null;
//
//			if (shapeType.equals(PHOSPHORYLATION_GRAPHICS)) {
//				image = (cyNetwork.isSelected(node)) ? customPhosGraphics[lc] : phosNode;
//			}
//
//			// set rect
//			Rectangle2D rect = getCustomShapeRect(image, lc);
//
//			// create our texture paint
//			Paint paint = null;
//
//			try {
//				paint = new java.awt.TexturePaint(image, rect);
//			} catch (Exception exc) {
//				paint = java.awt.Color.black;
//			}
//
//			// add the graphic
//			dingNodeView.addCustomGraphic(rect, paint, NodeDetails.ANCHOR_CENTER);
//		}
	}

	
	private static boolean isBioPAXNetwork(CyNetwork cyNetwork) {
		return Boolean.TRUE == cyNetwork.getRow(cyNetwork)
			.get(BioPaxMapper.BIOPAX_NETWORK, Boolean.class);
	}
}
