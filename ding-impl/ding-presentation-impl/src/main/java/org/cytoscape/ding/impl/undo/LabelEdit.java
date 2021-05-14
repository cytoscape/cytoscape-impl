package org.cytoscape.ding.impl.undo;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.LabelSelection;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.AbstractCyEdit;


public class LabelEdit extends AbstractCyEdit {

	private final CyServiceRegistrar serviceRegistrar;
	private final DRenderingEngine re;
	private final CyNetworkView netView;
	private final Long nodeId;
	private final LabelSelection labelSelection;
	
	private ObjectPosition startPosition;
	private Double startAngle;
	
	private ObjectPosition endPosition;
	private Double endAngle;

	public LabelEdit(
			CyServiceRegistrar serviceRegistrar,
			DRenderingEngine re,
			CyNetworkView netview,
			Long nodeId,
			LabelSelection labelSelection
	) {
		super("Move Label");
		this.re = re;
		this.serviceRegistrar = serviceRegistrar;
		this.netView = netview;
		this.nodeId = nodeId;
		this.labelSelection = labelSelection;
		
		View<CyNode> mutableNode = re.getViewModelSnapshot().getMutableNodeView(labelSelection.getNode().getSUID());
		if(mutableNode.isValueLocked(DVisualLexicon.NODE_LABEL_POSITION))
			this.startPosition = labelSelection.getPosition();
		if(mutableNode.isValueLocked(DVisualLexicon.NODE_LABEL_ROTATION))
			this.startAngle = labelSelection.getAngleDegrees();
	}

	private boolean isNetworkViewRegistered() {
		CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		return netViewMgr.getNetworkViews(netView.getModel()).contains(netView);
	}

	private void updateView() {
		VisualStyle style = serviceRegistrar.getService(VisualMappingManager.class).getVisualStyle(netView);
		style.apply(netView);
		netView.updateView();
	}
	
	private View<CyNode> getMutableNodeView() {
		return re.getViewModelSnapshot().getMutableNodeView(labelSelection.getNode().getSUID());
	}

	public void savePositionAndAngle() {
		this.endPosition = labelSelection.getPosition();
		this.endAngle = labelSelection.getAngleDegrees();
	}
	
	@Override
	public void undo() {
		re.getLabelSelectionManager().clear();
		if (isNetworkViewRegistered()) { // Make sure the network view still exists!
			View<CyNode> node = getMutableNodeView();
			
			if(startPosition == null)
				node.clearValueLock(DVisualLexicon.NODE_LABEL_POSITION);
			else
				node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, startPosition);
			
			if(startAngle == null)
				node.clearValueLock(DVisualLexicon.NODE_LABEL_ROTATION);
			else
				node.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, startAngle);
			
			updateView();
		}
	}

	@Override
	public void redo() {
		re.getLabelSelectionManager().clear();
		if (isNetworkViewRegistered()) {
			View<CyNode> node = getMutableNodeView();
			node.setLockedValue(DVisualLexicon.NODE_LABEL_POSITION, endPosition);
			node.setLockedValue(DVisualLexicon.NODE_LABEL_ROTATION, endAngle);
			updateView();
		}
	}

}
