package org.cytoscape.internal.select;

import java.util.Collection;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;


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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				boolean refreshView = false;
				final CyNetwork network = am.getCurrentNetwork();
				if (network == null)
					return;

				final Collection<CyNetworkView> views = vm.getNetworkViews(network);
				CyNetworkView networkView = null;
				if (views.size() != 0)
					networkView = views.iterator().next();

				if (networkView == null)
					return;

				final VisualStyle vs = vmm.getVisualStyle(networkView);
				
				for (final RowSetRecord record : e.getPayloadCollection()) {
					final String columnName = record.getColumn();
					final View<?> v = rowViewMap.get(record.getRow());

					if (v == null)
						continue;

					VisualProperty<?> vp = null;
					if (v.getModel() instanceof CyNode) {
						final CyNode node = (CyNode) v.getModel();
						if (network.containsNode(node))
							vp = getVPfromVS(vs, columnName);
					} else if (v.getModel() instanceof CyEdge) {
						final CyEdge edge = (CyEdge) v.getModel();

						if (network.containsEdge(edge))
							vp = getVPfromVS(vs, columnName);
					}

					if (vp != null) {
						targetFunction.apply(record.getRow(), (View<? extends CyIdentifiable>) v);
						refreshView = true;
					}
				}

				if (refreshView)
					networkView.updateView();
			}
		});
	}
	
	private VisualMappingFunction<?, ?> targetFunction;
	
	// Check if the columnName is the name of mapping attribute in visualStyle,
	// If yes, return the VisualProperty associated with this columnName
	private VisualProperty<?> getVPfromVS(VisualStyle vs, String columnName){
		VisualProperty<?> vp = null;
		final Collection<VisualMappingFunction<?,?>> vmfs = vs.getAllVisualMappingFunctions();
		
		for(final VisualMappingFunction<?,?> f: vmfs) {
			if (f.getMappingColumnName().equalsIgnoreCase(columnName)){
				vp = f.getVisualProperty();
				targetFunction = f;
				break;
			}
		}
		return vp;
	}
}
