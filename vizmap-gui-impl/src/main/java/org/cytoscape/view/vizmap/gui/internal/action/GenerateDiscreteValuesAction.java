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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.FitLabelMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheet;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItemModel;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

public class GenerateDiscreteValuesAction extends AbstractVizMapperAction {

	private static final long serialVersionUID = 3227895615738968589L;
	
	private final DiscreteMappingGenerator<?> generator;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public GenerateDiscreteValuesAction(final String name, final DiscreteMappingGenerator<?> generator,
			final ServicesUtil servicesUtil) {
		super(name, servicesUtil);
		this.generator = generator;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final VisualPropertySheet selVpSheet = getVizMapperMainPanel().getSelectedVisualPropertySheet();
		
		if (selVpSheet == null)
			return;
		
		final Set<VisualPropertySheetItem<?>> vpSheetItems = selVpSheet.getSelectedItems();
		
		new Thread() {
			@Override
			public void run() {
				final Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues = 
						new HashMap<DiscreteMapping<?,?>, Map<Object, Object>>();
				final Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues = 
						new HashMap<DiscreteMapping<?,?>, Map<Object, ?>>();
				
				for (final VisualPropertySheetItem<?> vpsItem : vpSheetItems) {
					final VisualPropertySheetItemModel<?> model = vpsItem.getModel();
			
					final VisualProperty<?> vp = (VisualProperty<?>) model.getVisualProperty();
					final Class<?> vpValueType = vp.getRange().getType();
					final Class<?> generatorType = generator.getDataType();
		
					final PropertySheetPanel propSheetPnl = vpsItem.getPropSheetPnl();
					final Item value = (Item) propSheetPnl.getTable().getValueAt(0, 0);
					
					if (value.isProperty()) {
						final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) value.getProperty();
					
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
					final UndoSupport undo = servicesUtil.get(UndoSupport.class);
					undo.postEdit(new GenerateValuesEdit(previousMappingValues, newMappingValues));
				}
			}
		}.start();
	}
	
	@Override
	public void updateEnableState() {
		boolean enabled = false;
		final VisualPropertySheet vpSheet = getVizMapperMainPanel().getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems()) {
				final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
				
				if (mapping != null && mapping instanceof DiscreteMapping) {
					final VisualProperty<?> vp = item.getModel().getVisualProperty();
					final Class<?> vpValueType = vp.getRange().getType();
					final Class<?> generatorType = generator.getDataType();
					
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
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void generateValues(final VisualPropertySheetItem<?> vpsItem,
								final String attrName,
								final VisualProperty<?> vp,
								final Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues,
								final Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues) {
		final VisualStyle style = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

		if (!(mapping instanceof DiscreteMapping))
			return;

		final DiscreteMapping<Object, Object> dm = (DiscreteMapping) mapping;
		final PropertySheetPanel propSheetPnl = vpsItem.getPropSheetPnl();
		final SortedSet<Object> keySet = new TreeSet<Object>();

		final Map<Object, Object> previousValues = new HashMap<Object, Object>();
		
		for (final Property p : propSheetPnl.getProperties()) {
			final VizMapperProperty<?, ?, ?> vmp = (VizMapperProperty<?, ?, ?>) p;
			
			if (vmp.getCellType().equals(CellType.DISCRETE)) {
				keySet.add(vmp.getKey());
				
				// Save the current value for undo
				previousValues.put(vmp.getKey(), vmp.getValue());
			}
		}

		if (!keySet.isEmpty()) {
			// Generate values
			final Map<Object, ?> newValues = generator.generateMap(keySet);
			
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
		
		public GenerateValuesEdit(final Map<DiscreteMapping<?, ?>, Map<Object, Object>> previousMappingValues,
								  final Map<DiscreteMapping<?, ?>, Map<Object, ?>> newMappingValues) {
			super("Mapping Value Generators");
			this.previousMappingValues = previousMappingValues;
			this.newMappingValues = newMappingValues;
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
			for (final Entry<DiscreteMapping<?, ?>, Map<Object, ?>> entry : newMappingValues.entrySet()) {
				final DiscreteMapping dm = entry.getKey();
				dm.putAll(entry.getValue());
			}
		}
	}
}
