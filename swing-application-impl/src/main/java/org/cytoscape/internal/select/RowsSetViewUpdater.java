package org.cytoscape.internal.select;

import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.View;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import java.util.Collection;
import java.util.Iterator;
import org.cytoscape.application.CyApplicationManager;


public class RowsSetViewUpdater implements RowsSetListener {

	private VisualMappingManager vmm;
	private final Map<CyRow, View<?>> rowViewMap;
	private final CyNetworkViewManager vm;
	private final CyApplicationManager am;
	/**
	 * Constructor.
	 */
	public RowsSetViewUpdater(CyApplicationManager am, CyNetworkViewManager vm, VisualMappingManager vmm, RowViewTracker tracker) {
		this.am = am;
		this.vm = vm;
		this.vmm = vmm;
		this.rowViewMap = tracker.getRowViewMap();
	}

	
	/**
	 * Called whenever {@link CyRow}s are changed. Will attempt to set the
	 * visual property on the view with the new value that has been set in the
	 * row.
	 * 
	 * @param RowsSetEvent The event to be processed.
	 */
	@SuppressWarnings("unchecked")
	public void handleEvent(final RowsSetEvent e) {

		SwingUtilities.invokeLater( new Runnable() {
		public void run() {

		boolean refreshView = false;
		CyNetwork network = am.getCurrentNetwork();		
		if ( network == null )
			return;

		CyNetworkView networkView = vm.getNetworkView(network);
		if ( networkView == null )
			return;

		VisualStyle vs = vmm.getVisualStyle(networkView);

		VisualProperty<?> vp = null;
		
		for (RowSetRecord record : e.getPayloadCollection()) {

			String columnName = record.getColumn();

			View<?> v = rowViewMap.get(record.getRow());
			
			if (v == null){
				return;
			}
			
			if (v.getModel() instanceof CyNode){
				CyNode node = (CyNode) v.getModel();
				if (network.containsNode(node)){
					vp = getVPfromVS(vs, columnName);
				}
			}
			else if (v.getModel() instanceof CyEdge){
				CyEdge edge = (CyEdge) v.getModel();
								
				if (network.containsEdge(edge)){
					vp = getVPfromVS(vs, columnName);
				}
			}

			if (vp != null) {
				v.setVisualProperty(vp, record.getValue());
				refreshView = true;
			}
		}

		if (refreshView){
			networkView.updateView();			
		}

		}});
	}
	
	
	// Check if the columnName is the name of mapping attribute in visualStyle,
	// If yes, return the VisualProperty associated with this columnName
	private VisualProperty<?> getVPfromVS(VisualStyle vs, String columnName){
		VisualProperty<?> vp = null;
		Collection<VisualMappingFunction<?,?>> vmfs = vs.getAllVisualMappingFunctions();
		
		Iterator<VisualMappingFunction<?,?>> it = vmfs.iterator();
		while (it.hasNext()){
			VisualMappingFunction<?,?> f = it.next();

			if (f.getMappingColumnName().equalsIgnoreCase(columnName)){
				vp = f.getVisualProperty();
				break;
			}
		}					

		return vp;
	}
}
