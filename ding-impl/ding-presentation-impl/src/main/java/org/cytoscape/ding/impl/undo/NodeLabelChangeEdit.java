package org.cytoscape.ding.impl.undo;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.InputHandlerGlassPane;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;


/**
 * Class to undo or redo Label Move. 
 * @author jingchen
 *
 */
public class NodeLabelChangeEdit extends AbstractCyEdit {
	
	
	private ObjectPosition oldValue;
	private ObjectPosition newValue;
	private Double oldRotation;
	private double newRotation;
	private CyServiceRegistrar serviceRegistrar;
	private CyNetworkView netView;
	private Long  nodeId;
  private InputHandlerGlassPane listener;

	public NodeLabelChangeEdit(CyServiceRegistrar serviceRegistrar, InputHandlerGlassPane listener, ObjectPosition previousValue, Double previousRotation,
			CyNetworkView netview, Long nodeId, String label) {
		super(label);
		this.serviceRegistrar = serviceRegistrar;
		
		this.oldValue = previousValue;
		this.oldRotation = previousRotation;
		this.netView = netview;
		this.nodeId = nodeId;
    this.listener = listener;
	}
	

	
	public void post(ObjectPosition newPosition, double newRotation) {
		this.newValue = newPosition;
		this.newRotation = newRotation;
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
			
			CyNode cyN = netView.getModel().getNode(nodeId.longValue());
			View<CyNode> node = netView.getNodeView(cyN);
			if ( oldValue != null)
				node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, oldValue);
			else
				node.clearValueLock(DVisualLexicon.NODE_LABEL_POSITION);

      if (oldRotation != null)
				node.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, oldRotation);
			else
				node.clearValueLock(DVisualLexicon.NODE_LABEL_ROTATION);
			
//      listener.resetLabelSelection();
			updateView();
		}		
	}

	@Override
	public void redo() {
		if (isNetworkViewRegistered()) {
			View<CyNode> node = netView.getNodeView(netView.getModel().getNode(nodeId.longValue()));
			node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, newValue);
			node.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, Math.toDegrees(newRotation));
			updateView();
		}	
	}

}
