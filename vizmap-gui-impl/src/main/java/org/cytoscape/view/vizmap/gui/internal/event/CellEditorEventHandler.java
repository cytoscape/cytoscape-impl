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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyApplicationManager;
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

	private static final Logger logger = LoggerFactory
			.getLogger(CellEditorEventHandler.class);

	private final SelectedVisualStyleManager manager;
	
	// Keeps current discrete mappings. NOT PERMANENT
	private final Map<String, Map<Object, Object>> discMapBuffer;

	private final CyTableManager tableMgr;

	protected final VizMapPropertySheetBuilder vizMapPropertySheetBuilder;
	protected final PropertySheetPanel propertySheetPanel;
	protected final CyApplicationManager applicationManager;
	
	private final AttributeSetManager attrManager;
	
	private final VizMapperUtil util;
	

	/**
	 * Creates a new CellEditorEventHandler object.
	 */
	public CellEditorEventHandler(final SelectedVisualStyleManager manager,
			final PropertySheetPanel propertySheetPanel,
			final CyTableManager tableMgr,
			final CyApplicationManager applicationManager,
			final VizMapPropertySheetBuilder vizMapPropertySheetBuilder, final AttributeSetManager attrManager, final VizMapperUtil util) {
		
		discMapBuffer = new HashMap<String, Map<Object, Object>>();
		this.propertySheetPanel = propertySheetPanel;
		this.tableMgr = tableMgr;
		this.applicationManager = applicationManager;
		this.vizMapPropertySheetBuilder = vizMapPropertySheetBuilder;
		this.manager = manager;
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

		logger.debug("$$$$$$$$ Got Property Sheet event: " + e);

		final Object newVal = e.getNewValue();
		final Object oldVal = e.getOldValue();

		// Check update is necessary or not.
		if (newVal == null && oldVal == null) {
			logger.debug("Both null.");
			return;
		}

		if (newVal != null && newVal.equals(oldVal)) {
			logger.debug("No change.  No need to update.");
			return;
		}

		// find selected cell
		final PropertySheetTable table = propertySheetPanel.getTable();
		final int selected = table.getSelectedRow();

		// If not selected, ignore.
		if (selected < 0) {
			logger.debug("No celected Cells!!.");
			return;
		}

		// Extract selected Property object in the table.
		final Item selectedItem = (Item) propertySheetPanel.getTable().getValueAt(selected, 0);
		final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) selectedItem
				.getProperty();

		logger.debug("#### Got new PROP: Name = " + prop.getDisplayName());
		logger.debug("#### Got new PROP: new Value = " + newVal);

		VisualProperty<?> type = null;

		// Case 1: Attribute type changed.
		if (prop.getCellType().equals(CellType.VISUAL_PROPERTY_TYPE)) {
			if (e.getNewValue() == null)
				throw new NullPointerException(
						"New controlling attr name is null.");

			VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) prop
					.getInternalValue();

			if (factory == null) {
				logger.debug("## Factory is still null.");
				Property[] children = prop.getSubProperties();
				for (int i = 0; i < children.length; i++) {
					final VizMapperProperty<?, ?, ?> child = (VizMapperProperty<?, ?, ?>) children[i];
					if (child.getCellType().equals(CellType.MAPPING_TYPE)
							&& child.getValue() instanceof VisualMappingFunctionFactory) {
						factory = (VisualMappingFunctionFactory) child
								.getValue();
						break;
					}
				}
				if (factory == null)
					return;
			}

			final AttributeComboBoxPropertyEditor editor = (AttributeComboBoxPropertyEditor) e
					.getSource();
			switchControllingAttr(factory, editor, prop, e.getNewValue()
					.toString());
		}

		// 2. Switch mapping type
		if (prop.getCellType().equals(CellType.MAPPING_TYPE)) {
			logger.debug("Mapping type changed from: " + e.getOldValue());
			logger.debug("Mapping type new = " + e.getNewValue());

			if (e.getNewValue() == e.getOldValue())
				return;

			// Parent is always root.
			VizMapperProperty<?, ?, ?> parent = (VizMapperProperty<?, ?, ?>) prop
					.getParentProperty();
			type = (VisualProperty<?>) parent.getKey();
			Object controllingAttrName = parent.getValue();

			if (type == null || controllingAttrName == null)
				return;

			logger.debug("New Type = " + type.getDisplayName());
			logger.debug("New Attr Name = " + controllingAttrName);
			switchMappingType(prop, type,
					(VisualMappingFunctionFactory) e.getNewValue(),
					controllingAttrName.toString());
		} else if (prop.getParentProperty() != null) {
			// Discrete Cell editor.  Create new map entry and register it.
			logger.debug("Cell edit event: name = " + prop.getName());
			logger.debug("Cell edit event: old val = " + prop.getValue());
			logger.debug("Cell edit event: new val = " + newVal);
			logger.debug("Cell edit event: associated mapping = "
					+ prop.getInternalValue());
			
			final VisualMappingFunction<?, ?> mapping = (VisualMappingFunction<?, ?>) prop.getInternalValue();
			
			if(mapping == null)
				return;
			
			if(mapping instanceof DiscreteMapping) {
				DiscreteMapping<Object, Object> discMap =  (DiscreteMapping<Object, Object>) mapping;
				discMap.putMapValue(prop.getKey(), newVal);
			}
			
			manager.getCurrentVisualStyle().apply(applicationManager.getCurrentNetworkView());
			applicationManager.getCurrentNetworkView().updateView();
		}
	}
	
		// if (e.getNewValue() == null)
		// return;
		//
		// /*
		// * If invalid data type, ignore.
		// */
		// final Object parentValue = prop.getParentProperty().getValue();
		//
		// if (parentValue != null) {
		// ctrAttrName = parentValue.toString();
		//
		// CyTable attr =
		// tableMgr.getTableMap().(type.getObjectType(),applicationManager.getCurrentNetwork()).get(
		// CyNetwork.DEFAULT_ATTRS);
		//
		// final Class<?> dataClass = attr.getColumnTypeMap().get(
		// ctrAttrName);
		//
		// if (e.getNewValue().equals("Continuous Mapper")
		// && ((dataClass != Integer.class) && (dataClass != Double.class))) {
		// JOptionPane.showMessageDialog(vizMapperMainPanel,
		// "Continuous Mapper can be used with Numbers only.",
		// "Incompatible Mapping Type!",
		// JOptionPane.INFORMATION_MESSAGE);
		//
		// return;
		// }
		// } else {
		// return;
		// }
		//
		// if (e.getNewValue().toString().endsWith("Mapper") == false)
		// return;
		//
		// switchMapping(prop, e.getNewValue().toString(), prop
		// .getParentProperty().getValue());
		//
		// /*
		// * restore expanded props.
		// */
		// vizMapPropertySheetBuilder.expandLastSelectedItem(type
		// .getDisplayName());
		// vizMapPropertySheetBuilder.updateTableView();
		//
		// return;
		// }
		//
		// /*
		// * Extract calculator
		// */
		// VisualMappingFunction<?, ?> mapping = vmm.getVisualStyle(
		// applicationManager.getCurrentNetworkView())
		// .getVisualMappingFunction(type);
		//
		// /*
		// * Controlling Attribute has been changed.
		// */
		// if (ctrAttrName != null) {
		// /*
		// * Ignore if not compatible.
		// */
		// final CyTable attrForTest =
		// tableMgr.getTableMap(type.getObjectType(),applicationManager.getCurrentNetwork()).get(CyNetwork.DEFAULT_ATTRS);
		//
		// final Class<?> dataType = attrForTest.getColumnTypeMap().get(
		// ctrAttrName);
		//
		// // This part is for Continuous Mapping.
		// if (mapping instanceof ContinuousMapping) {
		// if ((dataType == Double.class) || (dataType == Integer.class)) {
		// // Do nothing
		// } else {
		// JOptionPane
		// .showMessageDialog(
		// vizMapperMainPanel,
		// "Continuous Mapper can be used with Numbers only.\nPlease select numerical attributes.",
		// "Incompatible Mapping Type!",
		// JOptionPane.INFORMATION_MESSAGE);
		//
		// return;
		// }
		// }
		//
		// // If same, do nothing.
		// if (ctrAttrName.equals(mapping.getMappingAttributeName()))
		// return;
		//
		// // Buffer current discrete mapping
		// if (mapping instanceof DiscreteMapping) {
		// final String curMappingName = mapping.toString() + "-"
		// + mapping.getMappingAttributeName();
		// final String newMappingName = mapping.toString() + "-"
		// + ctrAttrName;
		// final Map saved = discMapBuffer.get(newMappingName);
		//
		// if (saved == null) {
		// discMapBuffer.put(curMappingName,
		// ((DiscreteMapping) mapping).getAll());
		// mapping.(ctrAttrName);
		// } else if (saved != null) {
		// // Mapping exists
		// discMapBuffer.put(curMappingName,
		// ((DiscreteMapping) mapping).getAll());
		// mapping.setControllingAttributeName(ctrAttrName);
		// ((DiscreteMapping) mapping).putAll(saved);
		// }
		// } else {
		// mapping.setControllingAttributeName(ctrAttrName);
		// }
		//
		// propertySheetPanel.removeProperty(typeRootProp);
		//
		// final VizMapperProperty<?> newRootProp = new VizMapperProperty<>();
		// final VisualStyle targetVS =
		// vmm.getVisualStyle(applicationManager.getCurrentNetworkView());
		//
		// vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(
		// targetVS.getVisualMappingFunction(type), newRootProp,
		// type.getObjectType(),
		// propertySheetPanel);
		//
		//
		// vizMapPropertySheetBuilder.removeProperty(typeRootProp);
		//
		// if (vizMapPropertySheetBuilder.getPropertyMap().get(
		// targetVS) != null)
		// vizMapPropertySheetBuilder.getPropertyMap().get(
		// targetVS).add(newRootProp);
		//
		// typeRootProp = null;
		//
		// vizMapPropertySheetBuilder.expandLastSelectedItem(type.getIdString());
		// vizMapPropertySheetBuilder.updateTableView();
		//
		// // Finally, update graph view and focus.
		// // vmm.setNetworkView(applicationManager.getCurrentNetworkView());
		// // Cytoscape.redrawGraph(applicationManager.getCurrentNetworkView());
		// return;
		// }
		//
		// // Return if not a Discrete Mapping.
		// if (mapping instanceof ContinuousMapping
		// || mapping instanceof PassthroughMappingCalculator)
		// return;
		//
		// Object key = null;
		//
		// if ((type.getType() == Number.class)
		// || (type.getType() == String.class)) {
		// key = e.getOldValue();
		//
		// // TODO WTF?
		// // if (type.getDataType() == Number.class) {
		// // numberCellEditor = new CyDoublePropertyEditor(this);
		// // numberCellEditor.addPropertyChangeListener(this);
		// // editorReg.registerEditor(prop, numberCellEditor);
		// // }
		// } else {
		// key = ((Item) propertySheetPanel.getTable().getValueAt(selected, 0))
		// .getProperty().getDisplayName();
		// }
		//
		// /*
		// * Need to convert this string to proper data types.
		// */
		// final CyTable attr =
		// tableMgr.getTableMap(type.getObjectType(),applicationManager.getCurrentNetwork()).get(CyNetwork.DEFAULT_ATTRS);
		// ctrAttrName = mapping.getMappingAttributeName();
		//
		// // Byte attrType = attr.getType(ctrAttrName);
		// Class<?> attrType = attr.getColumnTypeMap().get(ctrAttrName);
		//
		// if (attrType == Boolean.class)
		// key = Boolean.valueOf((String) key);
		// else if (attrType == Integer.class)
		// key = Integer.valueOf((String) key);
		// else if (attrType == Double.class)
		// key = Double.valueOf((String) key);
		//
		// Object newValue = e.getNewValue();
		//
		// if (type.getType() == Number.class) {
		// if ((((Number) newValue).doubleValue() == 0)
		// || (newValue instanceof Number
		// && type.toString().endsWith("OPACITY") && (((Number) newValue)
		// .doubleValue() > 255))) {
		// int shownPropCount = table.getRowCount();
		// Property p = null;
		// Object val = null;
		//
		// for (int i = 0; i < shownPropCount; i++) {
		// p = ((Item) table.getValueAt(i, 0)).getProperty();
		//
		// if (p != null) {
		// val = p.getDisplayName();
		//
		// if ((val != null) && val.equals(key.toString())) {
		// p.setValue(((DiscreteMapping) mapping)
		// .getMapValue(key));
		//
		// return;
		// }
		// }
		// }
		//
		// return;
		// }
		// }
		//
		// ((DiscreteMapping) mapping).putMapValue(key, newValue);
		//
		// /*
		// * Update table and current network view.
		// */
		// vizMapPropertySheetBuilder.updateTableView();
		//
		// propertySheetPanel.repaint();
		//
		// // vmm.setNetworkView(applicationManager.getCurrentNetworkView());
		// // Cytoscape.redrawGraph(applicationManager.getCurrentNetworkView());
	//}

	private <K, V> void switchControllingAttr(
			final VisualMappingFunctionFactory factory,
			final AttributeComboBoxPropertyEditor editor,
			VizMapperProperty<K, V, ?> prop, final String ctrAttrName)
	{
		final VisualStyle currentStyle = manager.getCurrentVisualStyle();

		final VisualProperty<V> vp = (VisualProperty<V>) prop.getKey();
		VisualMappingFunction<K, V> mapping = (VisualMappingFunction<K, V>) currentStyle
				.getVisualMappingFunction(vp);

		logger.debug("!!!!!!! Got Mapping: " + mapping);

		/*
		 * Ignore if not compatible.
		 */
		final CyTable attrForTest = tableMgr.getTableMap(
				editor.getTargetObjectType(),
				applicationManager.getCurrentNetwork()).get(
				CyNetwork.DEFAULT_ATTRS);

		final Class<K> dataType = (Class<K>)attrForTest.getColumn(ctrAttrName).getType();

		if (mapping == null) {
			// Need to create new one
			logger.debug("Mapping is still null: " + ctrAttrName);

			if (factory == null)
				return;

			mapping = factory.createVisualMappingFunction(ctrAttrName,
					dataType, vp);
			currentStyle.addVisualMappingFunction(mapping);
		}

		// If same, do nothing.
		if (ctrAttrName.equals(mapping.getMappingAttributeName())) {
			logger.debug("Same controlling attr.  Do nothing for: "
					+ ctrAttrName);
			return;
		}

		VisualMappingFunction<K, V> newMapping = null;
		if (mapping instanceof PassthroughMapping) {
			// Create new Passthrough mapping and register to current style.
			newMapping = factory.createVisualMappingFunction(ctrAttrName,
					dataType, vp);
			currentStyle.addVisualMappingFunction(newMapping);
			logger.debug("Changed to new Map from "
					+ mapping.getMappingAttributeName() + " to "
					+ newMapping.getMappingAttributeName());
		} else if (mapping instanceof ContinuousMapping) {
			if ((dataType == Double.class) || (dataType == Integer.class)) {
				// Do nothing
			} else {
				JOptionPane
						.showMessageDialog(
								null,
								"Continuous Mapper can be used with Numbers only.\nPlease select numerical attributes.",
								"Incompatible Mapping Type!",
								JOptionPane.INFORMATION_MESSAGE);

				return;
			}

		} else if (mapping instanceof DiscreteMapping) {
			newMapping = factory.createVisualMappingFunction(ctrAttrName,
					dataType, vp);
			currentStyle.addVisualMappingFunction(newMapping);
			logger.debug("Changed to new Map from "
					+ mapping.getMappingAttributeName() + " to "
					+ newMapping.getMappingAttributeName());
			// final String curMappingName = mapping.toString() + "-" +
			// mapping.getMappingAttributeName();
			// final String newMappingName = mapping.toString() + "-" +
			// ctrAttrName;
			// final Map saved = discMapBuffer.get(newMappingName);
			//
			// if (saved == null) {
			// factory.createVisualMappingFunction(ctrAttrName, mapping., vp);
			// discMapBuffer.put(curMappingName, ((DiscreteMapping)
			// mapping).getAll());
			// mapping.(ctrAttrName);
			// } else if (saved != null) {
			// // Mapping exists
			// discMapBuffer.put(curMappingName,
			// ((DiscreteMapping) mapping).getAll());
			// mapping.setControllingAttributeName(ctrAttrName);
			// ((DiscreteMapping) mapping).putAll(saved);
			// }
		}

		// Remove old property
		propertySheetPanel.removeProperty(prop);

		// Create new one.

		logger.debug("Creating new prop sheet objects for "
				+ newMapping.getMappingAttributeName() + ", "
				+ vp.getDisplayName());

		final VisualProperty<Visualizable> category = util.getCategory((Class<? extends CyTableEntry>) vp.getTargetDataType());
		VizMapperProperty<VisualProperty<V>, String, ?> newRootProp = vizMapPropertySheetBuilder
				.getPropertyBuilder().buildProperty(newMapping,
						category.getDisplayName(), propertySheetPanel, factory);

		logger.debug("!!!!!!! Removing Prop: " + prop);
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

	private void switchMappingType(final VizMapperProperty<?, ?, ?> prop,
			final VisualProperty<?> vp,
			final VisualMappingFunctionFactory factory,
			final String controllingAttrName) {

		// This is the currently selected Visual Style.
		final VisualStyle style = manager.getCurrentVisualStyle();
		
		logger.debug("Mapping combo box clicked: " + style.getTitle());
		logger.debug("Mapping Category: " + prop.getCategory());
		
		
		final VisualProperty<Visualizable> startVP = util.getCategory((Class<? extends CyTableEntry>) vp.getTargetDataType());
		final VisualMappingFunction<?, ?> currentMapping = style.getVisualMappingFunction(vp);
		
		logger.debug("Current Mapping for " + vp.getDisplayName() + " is: " + currentMapping);
		
		final VisualMappingFunction<?, ?> newMapping;
		logger.debug("!! New factory Category: " + factory.getMappingFunctionType());
		logger.debug("!! Current Mapping type: " + currentMapping);
		
		if(currentMapping == null || currentMapping.getClass() != factory.getMappingFunctionType()) {
			// Mapping does not exist.
			final AttributeSet attrSet = attrManager.getAttributeSet(applicationManager.getCurrentNetwork(), (Class<? extends CyTableEntry>) vp.getTargetDataType());
			final Class<?> attributeDataType = attrSet.getAttrMap().get(controllingAttrName);
			newMapping = factory.createVisualMappingFunction(controllingAttrName, attributeDataType,vp);
	
			style.addVisualMappingFunction(newMapping);
		} else {
			newMapping = currentMapping;
		}

		logger.debug("New VisualMappingFunction Created: Mapping Type = "
				+ style.getVisualMappingFunction(vp).toString());
		logger.debug("New VisualMappingFunction Created: Controlling attr = "
				+ style.getVisualMappingFunction(vp).getMappingAttributeName());

		// First, remove current property
		Property parent = prop.getParentProperty();
		propertySheetPanel.removeProperty(parent);

		final VizMapperProperty<?, ?, VisualMappingFunctionFactory> newRootProp;
		
		newRootProp = vizMapPropertySheetBuilder.getPropertyBuilder()
				.buildProperty(newMapping, startVP.getDisplayName(),
						propertySheetPanel, factory);
		

		vizMapPropertySheetBuilder.expandLastSelectedItem(vp.getDisplayName());
		vizMapPropertySheetBuilder.removeProperty(parent, style);

		final List<Property> propList = vizMapPropertySheetBuilder.getPropertyList(style);
		propList.add(newRootProp);

		parent = null;
		
		final VisualStyle currentStyle = manager.getCurrentVisualStyle();
		currentStyle.apply(applicationManager.getCurrentNetworkView());
		applicationManager.getCurrentNetworkView().updateView();
	}

	// private <K, V> VisualMappingFunction<K, V> getNewMappingFunction(
	// final VisualProperty<V> type, final String newMappingName,
	// final String newCalcName) {
	//
	// System.out.println("Mapper = " + newMappingName);
	//
	// Class mapperClass = catalog.getMapping(newMappingName);
	//
	// if (mapperClass == null) {
	// return null;
	// }
	//
	// // create the selected mapper
	// Class[] conTypes = { Object.class, byte.class };
	// Constructor mapperCon;
	//
	// try {
	// mapperCon = mapperClass.getConstructor(conTypes);
	// } catch (NoSuchMethodException exc) {
	// // Should not happen...
	// System.err.println("Invalid mapper " + mapperClass.getName());
	//
	// return null;
	// }
	//
	// final Object defaultObj = type.getDefault(vmm.getVisualStyle());
	//
	// System.out.println("defobj = " + defaultObj.getClass() + ", Type = "
	// + type.getName());
	//
	// final Object[] invokeArgs = { defaultObj };
	// VisualMappingFunction mapper = null;
	//
	// try {
	// mapper = (VisualMappingFunction) mapperCon.newInstance(invokeArgs);
	// } catch (Exception exc) {
	// System.err.println("Error creating mapping");
	//
	// return null;
	// }
	//
	// return new BasicCalculator(newCalcName, mapper, type);
	// return;
	// }

	// /**
	// * Switching between mapppings. Each calcs has 3 mappings. The first one
	// * (getMapping(0)) is the current mapping used by calculator.
	// *
	// */
	// private void switchMapping(VizMapperProperty<?> prop, String newMapName,
	// Object attrName) {
	// if (attrName == null)
	// return;
	// }

	// final VisualProperty<?> type = (VisualProperty) ((VizMapperProperty<?>)
	// prop
	// .getParentProperty()).getHiddenObject();
	//
	// final VisualStyle style =
	// vmm.getVisualStyle(applicationManager.getCurrentNetworkView());
	//
	// final String newCalcName = vmm.getVisualStyle().getName() + "-"
	// + type.getName() + "-" + newMapName;
	//
	// // Extract target calculator
	// Calculator newCalc = vmm.getCalculatorCatalog().getCalculator(type,
	// newCalcName);
	//
	// Calculator oldCalc = null;
	//
	// if (type.getObjectType().equals(VisualProperty.NODE))
	// oldCalc = vmm.getVisualStyle().getNodeAppearanceCalculator()
	// .getCalculator(type);
	// else
	// oldCalc = vmm.getVisualStyle().getEdgeAppearanceCalculator()
	// .getCalculator(type);
	//
	// /*
	// * If not exist, create new one.
	// */
	// if (newCalc == null) {
	// newCalc = getNewCalculator(type, newMapName, newCalcName);
	// newCalc.getMapping(0)
	// .setControllingAttributeName((String) attrName);
	// vmm.getCalculatorCatalog().addCalculator(newCalc);
	// }
	//
	// newCalc.getMapping(0).setControllingAttributeName((String) attrName);
	//
	// if (type.getObjectType().equals(VisualProperty.NODE)) {
	// vmm.getVisualStyle().getNodeAppearanceCalculator().setCalculator(
	// newCalc);
	// } else
	// vmm.getVisualStyle().getEdgeAppearanceCalculator().setCalculator(
	// newCalc);
	//
	// /*
	// * If old calc is not standard name, rename it.
	// */
	// if (oldCalc != null) {
	// final String oldMappingTypeName;
	//
	// if (oldCalc.getMapping(0) instanceof DiscreteMapping)
	// oldMappingTypeName = "Discrete Mapper";
	// else if (oldCalc.getMapping(0) instanceof ContinuousMapping)
	// oldMappingTypeName = "Continuous Mapper";
	// else if (oldCalc.getMapping(0) instanceof PassthroughMappingCalculator)
	// oldMappingTypeName = "Passthrough Mapper";
	// else
	// oldMappingTypeName = null;
	//
	// final String oldCalcName = type.getName() + "-"
	// + oldMappingTypeName;
	//
	// if (vmm.getCalculatorCatalog().getCalculator(type, oldCalcName) == null)
	// {
	// final Calculator newC = getNewCalculator(type,
	// oldMappingTypeName, oldCalcName);
	// newC.getMapping(0).setControllingAttributeName(
	// (String) attrName);
	// vmm.getCalculatorCatalog().addCalculator(newC);
	// }
	// }
	//
	// Property parent = prop.getParentProperty();
	// propertySheetPanel.removeProperty(parent);
	//
	// final VizMapperProperty newRootProp = new VizMapperProperty();
	//
	// if (type.getObjectType().equals(VisualProperty.NODE))
	// vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(
	// vmm.getVisualStyle().getNodeAppearanceCalculator()
	// .getCalculator(type), newRootProp,
	// AbstractVizMapperPanel.NODE_VISUAL_MAPPING,
	// propertySheetPanel);
	// else
	// vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(
	// vmm.getVisualStyle().getEdgeAppearanceCalculator()
	// .getCalculator(type), newRootProp,
	// AbstractVizMapperPanel.EDGE_VISUAL_MAPPING,
	// propertySheetPanel);
	//
	// vizMapPropertySheetBuilder.expandLastSelectedItem(type.getName());
	//
	// vizMapPropertySheetBuilder.removeProperty(parent);
	//
	// if (vizMapPropertySheetBuilder.getPropertyMap().get(
	// vmm.getVisualStyle().getName()) != null) {
	// vizMapPropertySheetBuilder.getPropertyMap().get(
	// vmm.getVisualStyle().getName()).add(newRootProp);
	// }
	//
	// // vmm.getNetworkView().redrawGraph(false, true);
	// // Cytoscape.redrawGraph(applicationManager.getCurrentNetworkView());
	// parent = null;
	// }
	//
	// private <K, V> VisualMappingFunction<K, V> getNewMappingFunction(final
	// VisualProperty<V> type,
	// final String newMappingName, final String newCalcName) {
	//
	// System.out.println("Mapper = " + newMappingName);
	//
	// Class mapperClass = catalog.getMapping(newMappingName);
	//
	// if (mapperClass == null) {
	// return null;
	// }
	//
	// // create the selected mapper
	// Class[] conTypes = { Object.class, byte.class };
	// Constructor mapperCon;
	//
	// try {
	// mapperCon = mapperClass.getConstructor(conTypes);
	// } catch (NoSuchMethodException exc) {
	// // Should not happen...
	// System.err.println("Invalid mapper " + mapperClass.getName());
	//
	// return null;
	// }
	//
	// final Object defaultObj = type.getDefault(vmm.getVisualStyle());
	//
	// System.out.println("defobj = " + defaultObj.getClass() + ", Type = "
	// + type.getName());
	//
	// final Object[] invokeArgs = { defaultObj };
	// VisualMappingFunction mapper = null;
	//
	// try {
	// mapper = (VisualMappingFunction) mapperCon.newInstance(invokeArgs);
	// } catch (Exception exc) {
	// System.err.println("Error creating mapping");
	//
	// return null;
	// }
	//
	// return new BasicCalculator(newCalcName, mapper, type);
	// }
}
