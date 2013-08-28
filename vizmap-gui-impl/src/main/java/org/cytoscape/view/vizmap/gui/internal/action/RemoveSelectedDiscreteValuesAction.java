package org.cytoscape.view.vizmap.gui.internal.action;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheet;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItemModel;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

/**
 * Action that deletes all the values from selected discrete mapping rows.
 */
public class RemoveSelectedDiscreteValuesAction extends AbstractVizMapperAction {
	
	public static final String NAME = "Remove Selected Discrete Mapping Values";

	private static final long serialVersionUID = 5111684472796917297L;
	
	private final Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues;

	// ==[ CONSTRUCTORS ]===============================================================================================

	public RemoveSelectedDiscreteValuesAction(final ServicesUtil servicesUtil) {
		super(NAME, servicesUtil);
		previousMappingValues = new HashMap<DiscreteMapping<?,?>, Map<Object, Object>>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	/**
	 * Remove all selected values at once. This is for Discrete Mapping only.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void actionPerformed(final ActionEvent e) {
		final VizMapperMainPanel vizMapperMainPanel = getVizMapperMainPanel();
		
		if (vizMapperMainPanel == null)
			return;
		
		final VisualPropertySheet vpSheet = vizMapperMainPanel.getSelectedVisualPropertySheet();
		
		if (vpSheet == null)
			return;
		
		for (final VisualPropertySheetItem<?> vpSheetItem : vpSheet.getSelectedItems()) {
			final VisualPropertySheetItemModel<?> model = vpSheetItem.getModel();
			final PropertySheetTable table = vpSheetItem.getPropSheetPnl().getTable();
			final int[] selected = table.getSelectedRows();
			
			if (selected == null || selected.length == 0
					|| !(model.getVisualMappingFunction() instanceof DiscreteMapping))
				continue;
	
			// Test with the first selected item
			final DiscreteMapping dm = (DiscreteMapping) model.getVisualMappingFunction();
			final Map<Object, Object> newValues = new HashMap<Object, Object>();
			final Map<Object, Object> previousValues = new HashMap<Object, Object>();
			
			for (int i = 0; i < selected.length; i++) {
				final Item item = ((Item) table.getValueAt(selected[i], 0));
				
				if (item != null && item.getProperty() instanceof VizMapperProperty) {
					final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) item.getProperty();
					
					if (prop.getCellType() == CellType.DISCRETE) {
						// Save the current value for undo
						if (prop.getValue() != null)
							previousValues.put(prop.getKey(), prop.getValue());
						
						// Mapping values to be removed
						newValues.put(prop.getKey(), null);
					}
				}
			}
			
			// Save the mapping->old_values for undo
			if (!previousValues.isEmpty())
				previousMappingValues.put(dm, previousValues);
			
			// Update the visual mapping
			dm.putAll(newValues);
		}
		
		// Undo support
		if (!previousMappingValues.isEmpty()) {
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new RemoveSelectedDiscreteValuesEdit());
		}
	}

	@Override
	public void updateEnableState() {
		boolean enabled = false;
		final VizMapperMainPanel vizMapperMainPanel = getVizMapperMainPanel();
		VisualPropertySheet vpSheet = null;
		
		if (vizMapperMainPanel != null)
			vpSheet = vizMapperMainPanel.getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> vpSheetItem : vpSheet.getSelectedItems()) {
				final VisualPropertySheetItemModel<?> model = vpSheetItem.getModel();
				final PropertySheetTable table = vpSheetItem.getPropSheetPnl().getTable();
				final int[] selected = table.getSelectedRows();
				
				if (selected != null && model.getVisualMappingFunction() instanceof DiscreteMapping) {
					// Make sure the selected rows have at least one Discrete Mapping entry with non-null value
					for (int i = 0; i < selected.length; i++) {
						final Item item = (Item) table.getValueAt(selected[i], 0);
						
						if (item != null && item.getProperty() instanceof VizMapperProperty) {
							final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) item.getProperty();
							
							if (prop.getCellType() == CellType.DISCRETE && prop.getValue() != null) {
								enabled = true;
								break;
							}
						}
					}
				}
			}
		}
		
		setEnabled(enabled);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RemoveSelectedDiscreteValuesEdit extends AbstractCyEdit {

		public RemoveSelectedDiscreteValuesEdit() {
			super(NAME);
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void undo() {
			for (final Entry<DiscreteMapping<?, ?>, Map<Object, Object>> entry : previousMappingValues.entrySet()) {
				final DiscreteMapping dm = entry.getKey();
				dm.putAll(entry.getValue());
			}
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void redo() {
			for (final Entry<DiscreteMapping<?, ?>, Map<Object, Object>> entry : previousMappingValues.entrySet()) {
				final DiscreteMapping dm = entry.getKey();
				final Map<Object, Object> newValues = new HashMap<Object, Object>();
				
				for (final Entry<Object, Object> originalEntry : entry.getValue().entrySet())
					newValues.put(originalEntry.getKey(), null);
				
				if (!newValues.isEmpty())
					dm.putAll(newValues);
			}
		}
	}
}
