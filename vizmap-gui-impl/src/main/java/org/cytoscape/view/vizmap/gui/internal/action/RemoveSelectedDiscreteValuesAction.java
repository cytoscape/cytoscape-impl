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

import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheet;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItemModel;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

/**
 * Action that deletes all the values from selected discrete mapping rows.
 */
public class RemoveSelectedDiscreteValuesAction extends AbstractVizMapperAction {
	
	public static final String NAME = "Remove Selected Discrete Mapping Values";

	private static final long serialVersionUID = 5111684472796917297L;


	public RemoveSelectedDiscreteValuesAction(final ServicesUtil servicesUtil) {
		super(NAME, servicesUtil);
	}

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
			final Map<Object, Object> changes = new HashMap<Object, Object>();
			
			for (int i = 0; i < selected.length; i++) {
				final Item item = ((Item) table.getValueAt(selected[i], 0));
				
				if (item != null && item.getProperty() instanceof VizMapperProperty) {
					final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) item.getProperty();
					
					if (prop.getCellType() == CellType.DISCRETE) {
						// First, update property sheet
						prop.setValue(null);
						// Then update the mapping
						changes.put(item.getProperty().getDisplayName(), null);
					}
				}
			}
			
			dm.putAll(changes);
			table.repaint();
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
}
