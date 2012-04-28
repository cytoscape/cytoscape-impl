/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal.event;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.event.VizMapEventHandler;
import org.cytoscape.view.vizmap.gui.internal.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.AttributeSetManager;
import org.cytoscape.view.vizmap.gui.internal.VizMapPropertySheetBuilder;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

// TODO: Should be refactored for readability!!
/**
 *
 */
public class CellEditorEventHandler implements VizMapEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(CellEditorEventHandler.class);

	private final SelectedVisualStyleManager selectedStyleManager;
	private final CyNetworkTableManager tableMgr;

	protected final VizMapPropertySheetBuilder vizMapPropertySheetBuilder;
	protected final PropertySheetPanel propertySheetPanel;
	protected final CyApplicationManager applicationManager;

	private final AttributeSetManager attrManager;

	private final VizMapperUtil util;

	/**
	 * Creates a new CellEditorEventHandler object.
	 */
	public CellEditorEventHandler(final SelectedVisualStyleManager manager,
			final PropertySheetPanel propertySheetPanel, final CyNetworkTableManager tableMgr,
			final CyApplicationManager applicationManager, final VizMapPropertySheetBuilder vizMapPropertySheetBuilder,
			final AttributeSetManager attrManager, final VizMapperUtil util) {

		this.propertySheetPanel = propertySheetPanel;
		this.tableMgr = tableMgr;
		this.applicationManager = applicationManager;
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
		this.selectedStyleManager = manager;
		this.attrManager = attrManager;
		this.util = util;
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
	 * @param e
	 *            DOCUMENT ME!
	 */
	@Override
	public void processEvent(PropertyChangeEvent e) {		

		final Object newVal = e.getNewValue();
		final Object oldVal = e.getOldValue();

		// Check update is necessary or not.
		if (newVal == null && oldVal == null)
			return;

		// Same value.  No change required.
		if (newVal != null && newVal.equals(oldVal))
			return;

		// find selected cell
		final PropertySheetTable table = propertySheetPanel.getTable();
		final int selected = table.getSelectedRow();

		// If nothing selected, ignore.
		if (selected < 0)
			return;

		// Extract selected Property object in the table.
		final Item selectedItem = (Item) propertySheetPanel.getTable().getValueAt(selected, 0);
		final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) selectedItem.getProperty();

		logger.debug("#### Got new PROP: Name = " + prop.getDisplayName());
		logger.debug("#### Got new PROP: new Value = " + newVal);

		VisualProperty<?> type = null;

		// Case 1: Attribute type changed.
		if (prop.getCellType().equals(CellType.VISUAL_PROPERTY_TYPE)) {
			
			if (e.getNewValue() == null)
				throw new NullPointerException("New controlling attr name is null.");

			VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) prop.getInternalValue();

			if (factory == null) {
				logger.debug("## Factory is still null.");
				Property[] children = prop.getSubProperties();
				for (int i = 0; i < children.length; i++) {
					final VizMapperProperty<?, ?, ?> child = (VizMapperProperty<?, ?, ?>) children[i];
					if (child.getCellType().equals(CellType.MAPPING_TYPE)
							&& child.getValue() instanceof VisualMappingFunctionFactory) {
						factory = (VisualMappingFunctionFactory) child.getValue();
						break;
					}
				}
				if (factory == null)
					return;
			}

			final AttributeComboBoxPropertyEditor editor = (AttributeComboBoxPropertyEditor) e.getSource();
			switchControllingAttr(factory, editor, prop, e.getNewValue().toString());
		}

		// 2. Switch mapping type
		if (prop.getCellType().equals(CellType.MAPPING_TYPE)) {
			if (e.getNewValue() == e.getOldValue())
				return;

			// Parent is always root.
			VizMapperProperty<?, ?, ?> parent = (VizMapperProperty<?, ?, ?>) prop.getParentProperty();
			type = (VisualProperty<?>) parent.getKey();
			Object controllingAttrName = parent.getValue();

			if (type == null || controllingAttrName == null)
				return;

			logger.debug("New Type = " + type.getDisplayName());
			logger.debug("New Attr Name = " + controllingAttrName);

			switchMappingType(prop, type, (VisualMappingFunctionFactory) e.getNewValue(),
					controllingAttrName.toString());
		} else if (prop.getParentProperty() != null) {
			// Discrete Cell editor event. Create new map entry and register it.
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

			selectedStyleManager.getCurrentVisualStyle().apply(applicationManager.getCurrentNetworkView());
			applicationManager.getCurrentNetworkView().updateView();
		}
	}

	private <K, V> void switchControllingAttr(final VisualMappingFunctionFactory factory,
			final AttributeComboBoxPropertyEditor editor, VizMapperProperty<K, V, ?> prop, final String ctrAttrName) {
		final VisualStyle currentStyle = selectedStyleManager.getCurrentVisualStyle();

		final VisualProperty<V> vp = (VisualProperty<V>) prop.getKey();
		VisualMappingFunction<K, V> mapping = (VisualMappingFunction<K, V>) currentStyle.getVisualMappingFunction(vp);

		/*
		 * Ignore if not compatible.
		 */
		@SuppressWarnings("unchecked")
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) editor.getTargetObjectType();
		final CyTable attrForTest = tableMgr.getTable(applicationManager.getCurrentNetwork(), type,
				CyNetwork.DEFAULT_ATTRS);

		final Class<K> dataType = (Class<K>) attrForTest.getColumn(ctrAttrName).getType();

		if (mapping == null) {
			// Need to create new one
			logger.debug("Mapping is still null: " + ctrAttrName);

			if (factory == null)
				return;

			mapping = factory.createVisualMappingFunction(ctrAttrName, dataType, attrForTest, vp);
			currentStyle.addVisualMappingFunction(mapping);
		}

		// If same, do nothing.
		if (ctrAttrName.equals(mapping.getMappingColumnName())) {
			logger.debug("Same controlling attr.  Do nothing for: " + ctrAttrName);
			return;
		}

		VisualMappingFunction<K, V> newMapping = null;
		if (mapping instanceof PassthroughMapping) {
			// Create new Passthrough mapping and register to current style.
			newMapping = factory.createVisualMappingFunction(ctrAttrName, dataType, attrForTest, vp);
			currentStyle.addVisualMappingFunction(newMapping);
			logger.debug("Changed to new Map from " + mapping.getMappingColumnName() + " to "
					+ newMapping.getMappingColumnName());
		} else if (mapping instanceof ContinuousMapping) {
			if ((dataType == Double.class) || (dataType == Integer.class)) {
				// Do nothing
			} else {
				JOptionPane.showMessageDialog(null,
						"Continuous Mapper can be used with Numbers only.\nPlease select numerical attributes.",
						"Incompatible Mapping Type!", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

		} else if (mapping instanceof DiscreteMapping) {
			newMapping = factory.createVisualMappingFunction(ctrAttrName, dataType, attrForTest, vp);
			currentStyle.addVisualMappingFunction(newMapping);
			logger.debug("Changed to new Map from " + mapping.getMappingColumnName() + " to "
					+ newMapping.getMappingColumnName());
		}

		// Remove old property
		propertySheetPanel.removeProperty(prop);

		// Create new one.
		logger.warn("Creating new prop sheet objects for " + newMapping.getMappingColumnName() + ", "
				+ vp.getDisplayName());

		final VisualProperty<Visualizable> category = util.getCategory((Class<? extends CyIdentifiable>) vp
				.getTargetDataType());
		VizMapperProperty<VisualProperty<V>, String, ?> newRootProp = vizMapPropertySheetBuilder.getPropertyBuilder()
				.buildProperty(newMapping, category.getDisplayName(), propertySheetPanel, factory);

		vizMapPropertySheetBuilder.removeProperty(prop, currentStyle);

		final List<Property> propList = vizMapPropertySheetBuilder.getPropertyList(currentStyle);
		propList.add(newRootProp);

		prop = null;

		vizMapPropertySheetBuilder.expandLastSelectedItem(vp.getIdString());
		vizMapPropertySheetBuilder.updateTableView();

		// Finally, update graph view and focus.
		currentStyle.apply(applicationManager.getCurrentNetworkView());
		applicationManager.getCurrentNetworkView().updateView();
		return;

	}

	private void switchMappingType(final VizMapperProperty<?, ?, ?> prop, final VisualProperty<?> vp,
			final VisualMappingFunctionFactory factory, final String controllingAttrName) {

		// This is the currently selected Visual Style.
		final VisualStyle style = selectedStyleManager.getCurrentVisualStyle();

		final VisualProperty<Visualizable> startVP = util.getCategory((Class<? extends CyIdentifiable>) vp.getTargetDataType());
		final VisualMappingFunction<?, ?> currentMapping = style.getVisualMappingFunction(vp);

		logger.debug("Current Mapping for " + vp.getDisplayName() + " is: " + currentMapping);

		final VisualMappingFunction<?, ?> newMapping;
		logger.debug("!! New factory Category: " + factory.getMappingFunctionType());
		logger.debug("!! Current Mapping type: " + currentMapping);

		if (currentMapping == null || currentMapping.getClass() != factory.getMappingFunctionType()) {

			// Mapping does not exist. Need to create new one.
			final AttributeSet attrSet = attrManager.getAttributeSet(applicationManager.getCurrentNetwork(),
					vp.getTargetDataType());
			final Class<?> attributeDataType = attrSet.getAttrMap().get(controllingAttrName);

			if (factory.getMappingFunctionType() == ContinuousMapping.class) {
				if (Number.class.isAssignableFrom(attributeDataType) == false) {
					JOptionPane.showMessageDialog(null,
							"Selected column data type is not Number.\nPlease select numerical attributes.",
							"Incompatible Column Type!", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}

			newMapping = factory.createVisualMappingFunction(controllingAttrName, attributeDataType, null, vp);
			style.addVisualMappingFunction(newMapping);
		} else
			newMapping = currentMapping;

		logger.debug("New VisualMappingFunction Created: Mapping Type = "
				+ style.getVisualMappingFunction(vp).toString());
		logger.debug("New VisualMappingFunction Created: Controlling attr = "
				+ style.getVisualMappingFunction(vp).getMappingColumnName());

		// First, remove current property
		Property parent = prop.getParentProperty();
		propertySheetPanel.removeProperty(parent);

		final VizMapperProperty<?, ?, VisualMappingFunctionFactory> newRootProp;

		newRootProp = vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(newMapping,
				startVP.getDisplayName(), propertySheetPanel, factory);

		vizMapPropertySheetBuilder.expandLastSelectedItem(vp.getDisplayName());
		vizMapPropertySheetBuilder.removeProperty(parent, style);

		final List<Property> propList = vizMapPropertySheetBuilder.getPropertyList(style);
		propList.add(newRootProp);

		parent = null;
		final VisualStyle currentStyle = selectedStyleManager.getCurrentVisualStyle();
		currentStyle.apply(applicationManager.getCurrentNetworkView());
		applicationManager.getCurrentNetworkView().updateView();

		vizMapPropertySheetBuilder.updateTableView();
	}
}
