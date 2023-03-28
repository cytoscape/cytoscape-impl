package org.cytoscape.view.vizmap.gui.internal.action;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.FitLabelMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class GenerateDiscreteValuesAction extends AbstractVizMapperAction {

	private static final long serialVersionUID = 3227895615738968589L;
	
	private final DiscreteMappingGenerator<?> generator;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public GenerateDiscreteValuesAction(String name, DiscreteMappingGenerator<?> generator, ServicesUtil servicesUtil) {
		super(name, servicesUtil);
		this.generator = generator;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void actionPerformed(ActionEvent e) {
		var selVpSheet = getVizMapperMainPanel().getSelectedVisualPropertySheet();
		
		if (selVpSheet == null)
			return;
		
		var vpSheetItems = selVpSheet.getSelectedItems();
		
		new Thread() {
			@Override
			public void run() {
				var previousMappingValues = new HashMap<DiscreteMapping<?, ?>, Map<Object, Object>>();
				var newMappingValues = new HashMap<DiscreteMapping<?, ?>, Map<Object, ?>>();
				
				for (var vpsItem : vpSheetItems) {
					var model = vpsItem.getModel();
			
					var vp = model.getVisualProperty();
					var vpValueType = vp.getRange().getType();
					var generatorType = generator.getDataType();
		
					var propSheetPnl = vpsItem.getPropSheetPnl();
					var value = (Item) propSheetPnl.getTable().getValueAt(0, 0);
					
					if (value.isProperty()) {
						var prop = (VizMapperProperty<?, ?, ?>) value.getProperty();
					
						if ( vpValueType.isAssignableFrom(generatorType)
								|| ((generator instanceof NumberSeriesMappingGenerator 
										|| generator instanceof RandomNumberMappingGenerator)
										&& Number.class.isAssignableFrom(vpValueType)) )
							generateValues(vpsItem, prop.getValue().toString(), vp, previousMappingValues,
									newMappingValues);
					}
				}
				
				// Undo support
				if (!previousMappingValues.isEmpty()) {
					var undo = servicesUtil.get(UndoSupport.class);
					undo.postEdit(new GenerateValuesEdit(previousMappingValues, newMappingValues));
				}
			}
		}.start();
	}
	
	@Override
	public void updateEnableState() {
		boolean enabled = false;
		var vpSheet = getVizMapperMainPanel().getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (var item : vpSheet.getSelectedItems()) {
				var mapping = item.getModel().getVisualMappingFunction();
				
				if (mapping != null && mapping instanceof DiscreteMapping) {
					var vp = item.getModel().getVisualProperty();
					var vpValueType = vp.getRange().getType();
					var generatorType = generator.getDataType();
					
					if (generator instanceof FitLabelMappingGenerator) {
						enabled = vp == BasicVisualLexicon.NODE_SIZE || vp == BasicVisualLexicon.NODE_WIDTH;
						break;
					}
					
					if ( vpValueType.isAssignableFrom(generatorType)
							|| ((generator instanceof NumberSeriesMappingGenerator 
									|| generator instanceof RandomNumberMappingGenerator)
									&& Number.class.isAssignableFrom(vpValueType)) ) {
						enabled = true;
						break;
					}
				}
			}
		}
		
		setEnabled(enabled);
	}
	
	public DiscreteMappingGenerator<?> getGenerator() {
		return generator;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateValues(
			VisualPropertySheetItem<?> vpsItem,
			String attrName,
			VisualProperty<?> vp,
			Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues,
			Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues
	) {
		var style = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		var mapping = style.getVisualMappingFunction(vp);

		if (!(mapping instanceof DiscreteMapping))
			return;

		var dm = (DiscreteMapping) mapping;
		var propSheetPnl = vpsItem.getPropSheetPnl();
		var keySet = new TreeSet<Object>();

		var previousValues = new HashMap<Object, Object>();
		
		for (var p : propSheetPnl.getProperties()) {
			var vmp = (VizMapperProperty<?, ?, ?>) p;
			
			if (vmp.getCellType().equals(CellType.DISCRETE)) {
				keySet.add(vmp.getKey());
				
				// Save the current value for undo
				previousValues.put(vmp.getKey(), vmp.getValue());
			}
		}

		if (!keySet.isEmpty()) {
			// Generate values
			var newValues = generator.generateMap(keySet);
			
			// Save the mapping->old_values for undo
			previousMappingValues.put(dm, previousValues);
			// Save the mapping->new_values for redo
			newMappingValues.put(dm, newValues);
			
			// Update the visual mapping
			dm.putAll(newValues);
		}
	}
		
	// ==[ CLASSES ]====================================================================================================

	private class GenerateValuesEdit extends AbstractCyEdit {

		private final Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues;
		private final Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues;
		
		public GenerateValuesEdit(
				Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues,
				Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues
		) {
			super("Mapping Value Generators");
			this.previousMappingValues = previousMappingValues;
			this.newMappingValues = newMappingValues;
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void undo() {
			for (var entry : previousMappingValues.entrySet()) {
				var dm = entry.getKey();
				dm.putAll((Map) entry.getValue());
			}
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void redo() {
			for (var entry : newMappingValues.entrySet()) {
				var dm = entry.getKey();
				dm.putAll((Map) entry.getValue());
			}
		}
	}

	@Override
	public void setIsInMenuBar(boolean b) {
		// Ignore...
	}

	@Override
	public void setIsInToolBar(boolean b) {
		// Ignore...
	}

	@Override
	public void setPreferredMenu(String menu) {
		// Ignore...
	}

	@Override
	public void setToolbarGravity(float f) {
		// Ignore...
	}

	@Override
	public void setMenuGravity(float f) {
		// Ignore...
	}
}
