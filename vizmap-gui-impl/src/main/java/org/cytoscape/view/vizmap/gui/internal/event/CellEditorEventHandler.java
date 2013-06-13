package org.cytoscape.view.vizmap.gui.internal.event;

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

import java.beans.PropertyChangeEvent;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandler;
import org.cytoscape.view.vizmap.gui.internal.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

// TODO: Should be refactored for readability!!
/**
 *
 */
public final class CellEditorEventHandler implements VizMapEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(CellEditorEventHandler.class);

	private final AttributeSetManager attrManager;
	private final VizMapperUtil util;
	private final ServicesUtil servicesUtil;
	private final VizMapperMediator vizMapperMediator;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;

	/**
	 * Creates a new CellEditorEventHandler object.
	 */
	public CellEditorEventHandler(final AttributeSetManager attrManager,
								  final VizMapperUtil util,
								  final ServicesUtil servicesUtil,
								  final VizMapPropertyBuilder vizMapPropertyBuilder,
								  final VizMapperMediator vizMapperMediator/*TODO refactor and remove ref to Mediator*/) {
		this.attrManager = attrManager;
		this.util = util;
		this.servicesUtil = servicesUtil;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		this.vizMapperMediator = vizMapperMediator;
	}

	/**
	 * Execute commands based on PropertyEditor's local event.
	 * 
	 * In this handler, we should handle the following:
	 * <ul>
	 * <li>Mapping Type change
	 * <li>Attribute Name Change
	 * </ul>
	 * 
	 * Other old global events (ex. Cytoscape.NETWORK_LOADED) is replaced by new
	 * events.
	 * 
	 * @param e PCE to be processed in this handler.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void processEvent(final PropertyChangeEvent e) {
		final Object newVal = e.getNewValue();
		final Object oldVal = e.getOldValue();

		// Check update is necessary or not.
		if (newVal == null && oldVal == null)
			return;

		// Same value. No change required.
		if (newVal != null && newVal.equals(oldVal))
			return;

		final PropertySheetPanel propertySheetPanel = vizMapperMediator.getCurrentPropertySheetPanel();
		
		// find selected cell
		final PropertySheetTable table = propertySheetPanel.getTable();
		final int selected = table.getSelectedRow();

		// If nothing selected, ignore.
		if (selected < 0)
			return;

		// Extract selected Property object in the table.
		final Item selectedItem = (Item) propertySheetPanel.getTable().getValueAt(selected, 0);
		final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) selectedItem.getProperty();

		if (prop == null)
			return;
		
		if (prop.getCellType() == CellType.VISUAL_PROPERTY_TYPE) {
			// Case 1: Attribute type changed.
			if (newVal != null && e.getSource() instanceof AttributeComboBoxPropertyEditor) {
				final AttributeComboBoxPropertyEditor editor = (AttributeComboBoxPropertyEditor) e.getSource();
				final VisualMappingFunctionFactory factory = 
						(VisualMappingFunctionFactory) propertySheetPanel.getTable().getValueAt(1, 1);
				
				if (factory != null)
					switchColumn(factory, editor, prop, newVal.toString(), propertySheetPanel);
			}
		} else if (prop.getCellType() == CellType.MAPPING_TYPE) {
			// Case 2. Switch mapping type
			// Parent is always root.
			final VisualProperty<?> vp = vizMapperMediator.getCurrentVisualProperty();
			// TODO: refactor--this class should not have to know the row/column where the value is
			Object controllingAttrName = propertySheetPanel.getTable().getValueAt(0, 1);

			if (vp == null || controllingAttrName == null)
				return;

			logger.debug("New Type = " + vp.getDisplayName());
			logger.debug("New Attr Name = " + controllingAttrName);

			switchMappingType(prop, vp, (VisualMappingFunctionFactory) e.getNewValue(),
					controllingAttrName.toString(), propertySheetPanel);
		} else if (prop.getParentProperty() != null) {
			// Case 3: Discrete Cell editor event. Create new map entry and register it.
			logger.debug("Cell edit event: name = " + prop.getName());
			logger.debug("Cell edit event: old val = " + prop.getValue());
			logger.debug("Cell edit event: new val = " + newVal);
			logger.debug("Cell edit event: associated mapping = " + prop.getInternalValue());

			final VisualMappingFunction<?, ?> mapping = (VisualMappingFunction<?, ?>) prop.getInternalValue();

			if (mapping == null)
				return;

			if (mapping instanceof DiscreteMapping) {
				DiscreteMapping<Object, Object> discMap = (DiscreteMapping<Object, Object>) mapping;
				discMap.putMapValue(prop.getKey(), newVal);
			}
		}
		
		// TODO: DELETE this workaround!!!
		if (vizMapperMediator.getCurrentVisualPropertySheetItem() != null)
			vizMapperMediator.getCurrentVisualPropertySheetItem().update();
	}

	@SuppressWarnings("unchecked")
	private <K, V> void switchColumn(final VisualMappingFunctionFactory factory,
									 final AttributeComboBoxPropertyEditor editor,
									 final VizMapperProperty<K, V, ?> prop,
									 final String columnName,
									 final PropertySheetPanel propertySheetPanel) {
		final VisualStyle currentStyle = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		final VisualProperty<V> vp = (VisualProperty<V>) prop.getKey();
		VisualMappingFunction<K, V> mapping = (VisualMappingFunction<K, V>) currentStyle.getVisualMappingFunction(vp);

		// Ignore if not compatible.
		final CyNetworkTableManager netTblMgr = servicesUtil.get(CyNetworkTableManager.class);
		final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) editor.getTargetObjectType();
		final CyTable attrForTest = netTblMgr.getTable(appMgr.getCurrentNetwork(), type, CyNetwork.DEFAULT_ATTRS);

		final CyColumn column = attrForTest.getColumn(columnName);
		
		if (column == null)
			return;

		final Class<K> dataType = (Class<K>) column.getType();

		if (factory != null && (mapping == null || !columnName.equals(mapping.getMappingColumnName()))) {
			// Need to create new mapping function
			if (ContinuousMapping.class.isAssignableFrom(factory.getMappingFunctionType()) &&
					!Number.class.isAssignableFrom(dataType)) {
				JOptionPane.showMessageDialog(null,
						"Continuous Mapper can be used with numbers only.\nPlease select a numerical column type.",
						"Incompatible Mapping Type.", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			switchMappingType(prop, vp, factory, columnName, propertySheetPanel);
		}
	}

	private void switchMappingType(final VizMapperProperty<?, ?, ?> prop,
								   final VisualProperty<?> vp,
								   final VisualMappingFunctionFactory factory,
								   final String controllingAttrName,
								   final PropertySheetPanel propertySheetPanel) {
		// This is the currently selected Visual Style.
		final VisualStyle style = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		final VisualMappingFunction<?, ?> currentMapping = style.getVisualMappingFunction(vp);
		final VisualMappingFunction<?, ?> newMapping;

		if (currentMapping == null || currentMapping.getClass() != factory.getMappingFunctionType() 
				|| !currentMapping.getMappingColumnName().equals(controllingAttrName)) {
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			final CyNetwork currentNet = appMgr.getCurrentNetwork();
			
			if (currentNet == null)
				return;
				
			// Mapping does not exist. Need to create new one.
			final AttributeSet attrSet = attrManager.getAttributeSet(currentNet, vp.getTargetDataType());
			final Class<?> attributeDataType = attrSet.getAttrMap().get(controllingAttrName);

			if (factory.getMappingFunctionType() == ContinuousMapping.class) {
				if (attributeDataType == null) {
					JOptionPane.showMessageDialog(null, "The current table does not have the selected column (\""
							+ controllingAttrName + "\").\nPlease select another column.", "Invalid Column.",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (!Number.class.isAssignableFrom(attributeDataType)) {
					JOptionPane.showMessageDialog(null,
							"Selected column data type is not Number.\nPlease select a numerical column type.",
							"Incompatible Column Type.", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			newMapping = factory.createVisualMappingFunction(controllingAttrName, attributeDataType, vp);
			style.addVisualMappingFunction(newMapping); // TODO
		} else {
			newMapping = currentMapping;
		}

		// Disable listeners to avoid unnecessary updates
		final PropertySheetTableModel model = (PropertySheetTableModel) propertySheetPanel.getTable().getModel();
		final TableModelListener[] modelListeners = model.getTableModelListeners();
		
		for (final TableModelListener tm : modelListeners)
			model.removeTableModelListener(tm);

		logger.debug("New VisualMappingFunction Created: Mapping Type = "
				+ style.getVisualMappingFunction(vp).toString());
		logger.debug("New VisualMappingFunction Created: Controlling attr = "
				+ style.getVisualMappingFunction(vp).getMappingColumnName());

		vizMapPropertyBuilder.createMappingProperties(newMapping, propertySheetPanel, factory);

		// Restore listeners
		for (final TableModelListener tm : modelListeners)
			model.addTableModelListener(tm);
	}
}
