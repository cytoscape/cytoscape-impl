package org.cytoscape.internal.select;

import java.util.Collection;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.view.NetworkViewManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.VirtualColumnInfo;
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

/**
 * Once table values are modified, this object updates the views if necessary.
 * 
 */
public class RowsSetViewUpdater implements RowsSetListener {

	private final VisualMappingManager vmm;
	private final Map<CyRow, View<?>> rowViewMap;
	private final CyNetworkViewManager vm;
	private final CyApplicationManager am;
	private final NetworkViewManager viewManager;
	

	private VisualMappingFunction<?, ?> targetFunction;

	public RowsSetViewUpdater(final CyApplicationManager am, final CyNetworkViewManager vm,
			final VisualMappingManager vmm, final RowViewTracker tracker, final NetworkViewManager viewManager) {
		this.am = am;
		this.vm = vm;
		this.vmm = vmm;
		this.viewManager = viewManager;
		this.rowViewMap = tracker.getRowViewMap();
	}

	/**
	 * Called whenever {@link CyRow}s are changed. Will attempt to set the
	 * visual property on the view with the new value that has been set in the
	 * row.
	 * 
	 * @param RowsSetEvent
	 *            The event to be processed.
	 */
	@Override
	public void handleEvent(final RowsSetEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateView(e);
			}
		});
	}

	private final void updateView(final RowsSetEvent e) {
		boolean refreshView = false;
		boolean refreshOtherViews = false;
		
		final CyNetwork network = am.getCurrentNetwork();
		if (network == null)
			return;
		
		// 1: Update current network view
		final Collection<CyNetworkView> views = vm.getNetworkViews(network);
		CyNetworkView networkView = null;
		if (views.isEmpty())
			return;
		else
			networkView = views.iterator().next();

		final VisualStyle vs = vmm.getVisualStyle(networkView);
		
		for (final RowSetRecord record : e.getPayloadCollection()) {
			
			final CyRow row = record.getRow();
			final String columnName = record.getColumn();
			final CyColumn column = row.getTable().getColumn(columnName);
			final VirtualColumnInfo virtualColInfo = column.getVirtualColumnInfo();
			final boolean virtual = virtualColInfo.isVirtual();
			final View<?> v = rowViewMap.get(row);

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
			} else {
				// FIXME: NETWORK?
			}

			if (vp != null) {
				targetFunction.apply(record.getRow(), (View<? extends CyIdentifiable>) v);
				refreshView = true;
				
				// If virtual, it may be used in other networks.
				if(virtual)
					refreshOtherViews = true;
			}
		}

		if (refreshView) {
			vs.apply(networkView);
			networkView.updateView();
			
			if (refreshOtherViews) {
				// Check other views. If update is required, set the flag.
				for (final CyNetworkView view : vm.getNetworkViewSet()) {
					if (view == networkView)
						continue;

					final VisualStyle style = vmm.getVisualStyle(view);
					if (style == vs) {
						// Same style is in use. Need to apply.
						viewManager.setUpdateFlag(view);
					}
				}
			}
		}
	}

	// Check if the columnName is the name of mapping attribute in visualStyle,
	// If yes, return the VisualProperty associated with this columnName
	private VisualProperty<?> getVPfromVS(VisualStyle vs, String columnName) {
		VisualProperty<?> vp = null;
		final Collection<VisualMappingFunction<?, ?>> vmfs = vs.getAllVisualMappingFunctions();

		for (final VisualMappingFunction<?, ?> f : vmfs) {
			if (f.getMappingColumnName().equalsIgnoreCase(columnName)) {
				vp = f.getVisualProperty();
				targetFunction = f;
				break;
			}
		}
		return vp;
	}
}
