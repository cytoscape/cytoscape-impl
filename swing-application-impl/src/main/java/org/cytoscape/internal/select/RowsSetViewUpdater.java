package org.cytoscape.internal.select;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.view.NetworkViewMediator;
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
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

// TODO move to NetworkViewManager?
/**
 * Once table values are modified, this object updates the views if necessary.
 * 
 */
public class RowsSetViewUpdater implements RowsSetListener {

	private final VisualMappingManager vmm;
	private final CyNetworkViewManager vm;
	private final CyApplicationManager am;
	private final NetworkViewMediator netViewMediator;
	private final RowViewTracker tracker;
	private final CyColumnIdentifierFactory colIdFactory;

	public RowsSetViewUpdater(final CyApplicationManager am, final CyNetworkViewManager vm,
			final VisualMappingManager vmm, final RowViewTracker tracker, final NetworkViewMediator netViewMediator,
			final CyColumnIdentifierFactory colIdFactory) {
		this.am = am;
		this.vm = vm;
		this.vmm = vmm;
		this.netViewMediator = netViewMediator;
		this.tracker = tracker;
		this.colIdFactory = colIdFactory;
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
			@Override
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
		CyNetworkView networkView;
		if (views.isEmpty())
			return;
		else
			networkView = views.iterator().next();

		final VisualStyle vs = vmm.getVisualStyle(networkView);
		Map<CyRow, View<? extends CyIdentifiable>> rowViewMap = tracker.getRowViewMap(networkView);
		
		for (final RowSetRecord record : e.getPayloadCollection()) {
			final CyRow row = record.getRow();
			final String columnName = record.getColumn();
			final CyColumn column = row.getTable().getColumn(columnName);
			
			if (column == null)
				continue;
			
			final VirtualColumnInfo virtualColInfo = column.getVirtualColumnInfo();
			final boolean virtual = virtualColInfo.isVirtual();
			final View<? extends CyIdentifiable> v = rowViewMap.get(row);

			if (v == null)
				continue;

			if (v.getModel() instanceof CyNode) {
				final CyNode node = (CyNode) v.getModel();
				
				if (network.containsNode(node) && isStyleAffected(vs, columnName)) {
					vs.apply(row, v);
					refreshView = false;
				}
			} else if (v.getModel() instanceof CyEdge) {
				final CyEdge edge = (CyEdge) v.getModel();
				
				if (network.containsEdge(edge) && isStyleAffected(vs, columnName)) {
					vs.apply(row, v);
					refreshView = false;
				}
			}

			// If virtual, it may be used in other networks.
			if (refreshView && virtual)
				refreshOtherViews = true;
			
//			if (refreshView)
//				vs.apply(record.getRow(), (View<? extends CyIdentifiable>) v);
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
						netViewMediator.setUpdateFlag(view);
					}
				}
			}
		}
	}

	/**
	 * Check if the columnName is used by any property of the passed Visual Style.
	 * @param vs
	 * @param columnName
	 * @return
	 */
	private boolean isStyleAffected(final VisualStyle vs, final String columnName) {
		boolean result = false;
		final RenderingEngine<CyNetwork> renderer = am.getCurrentRenderingEngine();
		
		if (renderer != null) {
			final CyColumnIdentifier colId = colIdFactory.createColumnIdentifier(columnName);
			final Set<VisualProperty<?>> properties = renderer.getVisualLexicon().getAllVisualProperties();
			
			for (final VisualProperty<?> vp : properties) {
				final VisualMappingFunction<?, ?> f = vs.getVisualMappingFunction(vp);
				
				if (f != null && f.getMappingColumnName().equalsIgnoreCase(columnName)) {
					result = true;
					break;
				}
				
				final Object defValue = vs.getDefaultValue(vp);
				
				if (defValue instanceof MappableVisualPropertyValue) {
					final Set<CyColumnIdentifier> mappedColIds = ((MappableVisualPropertyValue)defValue).getMappedColumns();
					
					if (mappedColIds.contains(colId)) {
						result = true;
						break;
					}
				}
			}
		}
		
		return result;
	}
}
