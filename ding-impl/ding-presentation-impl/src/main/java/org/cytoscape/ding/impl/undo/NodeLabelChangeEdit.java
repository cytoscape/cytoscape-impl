package org.cytoscape.ding.impl.undo;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Class to undo or redo Label Move. 
 * @author jingchen
 *
 */
public class NodeLabelChangeEdit extends AbstractCyEdit {
	
	
	private ObjectPosition oldValue;
	private ObjectPosition newValue;
	private CyServiceRegistrar serviceRegistrar;
	private CyNetworkView netView;
	private View<CyNode>  node;

	public NodeLabelChangeEdit(CyServiceRegistrar serviceRegistrar, ObjectPosition previousValue,
			CyNetworkView netview, View<CyNode> nodeView) {
		super("Move Label");
		// TODO Auto-generated constructor stub
		this.serviceRegistrar = serviceRegistrar;
		
		this.oldValue = previousValue;
		this.netView = netview;
		this.node = nodeView;
	}
	

	
	public void post(ObjectPosition newPosition) {
		this.newValue = newPosition;
		serviceRegistrar.getService(UndoSupport.class).postEdit(this);
	}

	
	private boolean isNetworkViewRegistered() {
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		return netViewMgr.getNetworkViews(netView.getModel()).contains(netView);
	}
	
	
	private void updateView() {
		final VisualStyle style = serviceRegistrar.getService(VisualMappingManager.class).getVisualStyle(netView);
		style.apply(netView);
		netView.updateView();
	}
	
	@Override
	public void undo() {
		if (isNetworkViewRegistered()) { // Make sure the network view still exists!
			if ( oldValue != null)
				node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, oldValue);
			else
				node.clearValueLock(DVisualLexicon.NODE_LABEL_POSITION);
			
			updateView();
		}		
	}

	@Override
	public void redo() {
		if (isNetworkViewRegistered()) {
			node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, newValue);
			updateView();
		}	
	}

}
