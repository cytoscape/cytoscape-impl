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
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

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
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.util.MathUtil;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.AttributeComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

// TODO: Should be refactored for readability!!
/**
 *
 */
public final class CellEditorEventHandler implements VizMapEventHandler {

	private final AttributeSetProxy attrProxy;
	private final ServicesUtil servicesUtil;
	private final VizMapperMediator vizMapperMediator;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;

	/**
	 * Creates a new CellEditorEventHandler object.
	 */
	public CellEditorEventHandler(final AttributeSetProxy attrProxy,
								  final ServicesUtil servicesUtil,
								  final VizMapPropertyBuilder vizMapPropertyBuilder,
								  final VizMapperMediator vizMapperMediator) {
		this.attrProxy = attrProxy;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processEvent(final PropertyChangeEvent e) {
		final Object newVal = e.getNewValue();
		final Object oldVal = e.getOldValue();

		// Check update is necessary or not.
		if (newVal == null && oldVal == null)
			return;

		// Same value. No change required.
		if (newVal != null && newVal.equals(oldVal))
			return;

		final VisualPropertySheetItem<?> vpSheetItem = vizMapperMediator.getCurrentVisualPropertySheetItem();
		final PropertySheetPanel propSheetPnl = vpSheetItem != null ? vpSheetItem.getPropSheetPnl() : null;
		
		if (propSheetPnl == null)
			return;
		
		final VizMapperProperty<?, ?, ?> prop = vizMapperMediator.getCurrentVizMapperProperty();

		if (prop == null)
			return;
		
		final VisualProperty<?> vp = vpSheetItem.getModel().getVisualProperty();
		final VisualMappingFunction mapping = vpSheetItem.getModel().getVisualMappingFunction();
		
		if (prop.getCellType() == CellType.DISCRETE && mapping instanceof DiscreteMapping) {
			// Discrete mapping value changed:
			// -------------------------------
			// Create new map entry and register it.
			final DiscreteMapping<Object, Object> discMap = (DiscreteMapping<Object, Object>) mapping;
			setDiscreteMappingEntry(prop.getKey(), oldVal, newVal, discMap);
		} else {
			VisualMappingFunction newMapping = mapping;
			String undoName = null;
			
			if (prop.getCellType() == CellType.VISUAL_PROPERTY_TYPE) {
				// Attribute type changed:
				// -----------------------
				if (newVal != null && e.getSource() instanceof AttributeComboBoxPropertyEditor) {
					final AttributeComboBoxPropertyEditor editor = (AttributeComboBoxPropertyEditor) e.getSource();
					final VisualMappingFunctionFactory factory = 
							(VisualMappingFunctionFactory) propSheetPnl.getTable().getValueAt(1, 1);
					
					newMapping = switchColumn(factory, editor, prop, newVal.toString(), propSheetPnl);
					vpSheetItem.getModel().setVisualMappingFunction(newMapping);
					
					if (newMapping == null)
						vpSheetItem.getModel().setMappingColumnName(
								prop.getValue() != null ? prop.getValue().toString() : null);
					
					undoName = "Set Mapping Column";
				}
			} else if (prop.getCellType() == CellType.MAPPING_TYPE) {
				// Mapping type changed:
				// -----------------------
				// Parent is always root.
				// TODO: refactor--this class should not have to know the row/column where the value is
				Object controllingAttrName = propSheetPnl.getTable().getValueAt(0, 1);
	
				if (vp != null && controllingAttrName != null 
						&& (newVal == null || newVal instanceof VisualMappingFunctionFactory)) {
					newMapping = switchMappingType(prop, vp, (VisualMappingFunctionFactory) oldVal, 
							(VisualMappingFunctionFactory) newVal, controllingAttrName.toString(), propSheetPnl);
					vpSheetItem.getModel().setVisualMappingFunction(newMapping);
					
					undoName = "Set Mapping Type";
				}
			}
			
			if (newMapping != mapping && undoName != null) {
				// Add undo support
				final VisualMappingFunction myNewMapping = newMapping;
				final UndoSupport undo = servicesUtil.get(UndoSupport.class);
				undo.postEdit(new AbstractCyEdit(undoName) {
					@Override
					public void undo() {
						vpSheetItem.getModel().setVisualMappingFunction(mapping);
					}
					@Override
					public void redo() {
						vpSheetItem.getModel().setVisualMappingFunction(myNewMapping);
					}
				});
			}
		}
	}

	@SuppressWarnings("unchecked")
	private VisualMappingFunction<?, ?> switchColumn(final VisualMappingFunctionFactory factory,
													 final AttributeComboBoxPropertyEditor editor,
													 final VizMapperProperty<?, ?, ?> prop,
													 final String columnName,
													 final PropertySheetPanel propertySheetPanel) {
		final VisualStyle currentStyle = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		final VisualProperty<?> vp = (VisualProperty<?>) prop.getKey();
		VisualMappingFunction<?, ?> mapping = currentStyle.getVisualMappingFunction(vp);

		// Ignore if not compatible.
		final CyNetworkTableManager netTblMgr = servicesUtil.get(CyNetworkTableManager.class);
		final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) editor.getTargetObjectType();
		final CyTable table = netTblMgr.getTable(appMgr.getCurrentNetwork(), type, CyNetwork.DEFAULT_ATTRS);

		final CyColumn column = table.getColumn(columnName);
		
		if (column == null) {
			JOptionPane.showMessageDialog(null, "The current table does not have the selected column (\""
					+ columnName + "\").\nPlease select another column.", "Invalid Column.",
					JOptionPane.WARNING_MESSAGE);
			prop.setValue(mapping != null ? mapping.getMappingColumnName() : null);
			
			return mapping;
		}

		final Class<?> dataType = column.getType();

		if (factory != null && (mapping == null || !columnName.equals(mapping.getMappingColumnName()))) {
			// Need to create new mapping function
			if (ContinuousMapping.class.isAssignableFrom(factory.getMappingFunctionType()) &&
					!Number.class.isAssignableFrom(dataType)) {
				JOptionPane.showMessageDialog(null,
						"Continuous Mapper can be used with numbers only.\nPlease select a numerical column type.",
						"Incompatible Mapping Type.", JOptionPane.INFORMATION_MESSAGE);
				prop.setValue(mapping != null ? mapping.getMappingColumnName() : null);
				
				return mapping;
			}
			
			return switchMappingType(prop, vp, factory, factory, columnName, propertySheetPanel);
		}
		
		return mapping;
	}

	private VisualMappingFunction<?, ?> switchMappingType(final VizMapperProperty<?, ?, ?> prop,
														  final VisualProperty<?> vp,
														  final VisualMappingFunctionFactory oldFactory,
														  final VisualMappingFunctionFactory newFactory,
														  final String controllingAttrName,
														  final PropertySheetPanel propertySheetPanel) {
		// This is the currently selected Visual Style.
		final VisualStyle style = servicesUtil.get(VisualMappingManager.class).getCurrentVisualStyle();
		final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
		VisualMappingFunction<?, ?> newMapping = mapping;
		final Class<?> newMappingType = newFactory.getMappingFunctionType();
		
		if (mapping == null || mapping.getClass() != newMappingType 
				|| !mapping.getMappingColumnName().equals(controllingAttrName)) {
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			final CyNetwork currentNet = appMgr.getCurrentNetwork();
			
			if (currentNet == null)
				return newMapping;
				
			// Mapping does not exist. Need to create new one.
			final AttributeSet attrSet = attrProxy.getAttributeSet(currentNet, vp.getTargetDataType());
			Class<?> attributeDataType = attrSet.getAttrMap().get(controllingAttrName);

			if (attributeDataType == null) {
				JOptionPane.showMessageDialog(null, "The current table does not have the selected column (\""
						+ controllingAttrName + "\").\nPlease select another column.", "Invalid Column.",
						JOptionPane.WARNING_MESSAGE);
				prop.setValue(oldFactory);
				
				return newMapping;
			}
			
			if (newMappingType == ContinuousMapping.class) {
				if (!Number.class.isAssignableFrom(attributeDataType)) {
					JOptionPane.showMessageDialog(null,
							"Selected column data type is not Number.\nPlease select a numerical column type.",
							"Incompatible Column Type.", JOptionPane.WARNING_MESSAGE);
					prop.setValue(oldFactory);
					
					return newMapping;
				}
			} else if (newMappingType == DiscreteMapping.class) {
				if (attributeDataType == List.class)
					attributeDataType = String.class;
			}

			// Create new mapping
			newMapping = newFactory.createVisualMappingFunction(controllingAttrName, attributeDataType, vp);
			
			// Keep old mapping values if the new mapping has the same type
			if (oldFactory != null && oldFactory.getMappingFunctionType() == newMappingType)
				copyMappingValues(mapping, newMapping);
		}

		// Disable listeners to avoid unnecessary updates
		final PropertySheetTableModel model = (PropertySheetTableModel) propertySheetPanel.getTable().getModel();
		final TableModelListener[] modelListeners = model.getTableModelListeners();
		
		for (final TableModelListener tm : modelListeners)
			model.removeTableModelListener(tm);

		vizMapPropertyBuilder.createMappingProperties(newMapping, propertySheetPanel, newFactory);
		
		// Restore listeners
		for (final TableModelListener tm : modelListeners)
			model.addTableModelListener(tm);
		
		return newMapping;
	}

	private void setDiscreteMappingEntry(final Object key, final Object oldVal, final Object newVal,
			final DiscreteMapping<Object, Object> mapping) {
		final VisualProperty<?> vp = mapping.getVisualProperty();
		
		if (newVal == null || vp.getRange().getType().isAssignableFrom(newVal.getClass())) {
			mapping.putMapValue(key, newVal);
			
			// Undo support
			if ((oldVal != null && newVal == null) || (newVal != null && !newVal.equals(oldVal))) {
				final UndoSupport undo = servicesUtil.get(UndoSupport.class);
				undo.postEdit(new AbstractCyEdit("Set Discrete Mapping Value") {
					@Override
					public void undo() {
						mapping.putMapValue(key, oldVal);
					}
					@Override
					public void redo() {
						mapping.putMapValue(key, newVal);
					}
				});
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void copyMappingValues(final VisualMappingFunction<?, ?> source, final VisualMappingFunction<?, ?> target) {
		if (source instanceof ContinuousMapping && target instanceof ContinuousMapping) {
			final CyNetwork curNet = servicesUtil.get(CyApplicationManager.class).getCurrentNetwork();
			
			if (curNet != null) {
				final VisualProperty<?> vp = source.getVisualProperty();
				final CyTable dataTable = curNet.getTable(vp.getTargetDataType(), CyNetwork.DEFAULT_ATTRS);
				final CyColumn col = dataTable.getColumn(target.getMappingColumnName());

				if (col == null)
					return;
				
				// Get new column's min/max values
				double minTgtVal = Double.POSITIVE_INFINITY;
				double maxTgtVal = Double.NEGATIVE_INFINITY;
				final List<?> valueList = col.getValues(col.getType());

				for (final Object o : valueList) {
					if (o instanceof Number) {
						double val = ((Number) o).doubleValue();
						maxTgtVal = Math.max(maxTgtVal, val);
						minTgtVal = Math.min(minTgtVal, val);
					}
				}
				
				final ContinuousMapping cm1 = (ContinuousMapping<?, ?>) source;
				final ContinuousMapping cm2 = (ContinuousMapping<?, ?>) target;
				final List<ContinuousMappingPoint<?, ?>> points1 = cm1.getAllPoints();
				
				if (points1 == null || points1.isEmpty())
					return;
				
				// Make sure the source points are sorted by their values
				final TreeSet<ContinuousMappingPoint<?, ?>> srcPoints = new TreeSet<ContinuousMappingPoint<?, ?>>(
						new Comparator<ContinuousMappingPoint<?, ?>>() {
							@Override
							public int compare(
									final ContinuousMappingPoint<?, ?> o1,
									final ContinuousMappingPoint<?, ?> o2) {
								final BigDecimal v1 = new BigDecimal(((Number)o1.getValue()).doubleValue());
								final BigDecimal v2 = new BigDecimal(((Number)o2.getValue()).doubleValue());
								return v1.compareTo(v2);
							}
						});
				srcPoints.addAll(points1);
				
				// Now that the source points are sorted, we can get the min/max source values
				final double minSrcVal = ((Number)srcPoints.first().getValue()).doubleValue();
				final double maxSrcVal = ((Number)srcPoints.last().getValue()).doubleValue();
				
				// Make sure the target mapping has no points, so delete any existing one
				int tgtPointsSize = cm2.getPointCount();
				
				for (int i = 0; i < tgtPointsSize; i++)
					cm2.removePoint(i);
				
				// Convert the source points and copy them to the target mapping
				int srcPointsSize = srcPoints.size();
				
				for (int i = 0; i < srcPointsSize; i++) {
					final ContinuousMappingPoint<?, ?> mp = cm1.getPoint(i);
					final double srcVal = ((Number)cm1.getPoint(i).getValue()).doubleValue();
					
					// Linearly interpolate the new value
					final double f = MathUtil.invLinearInterp(srcVal, minSrcVal, maxSrcVal);
					final double tgtVal = MathUtil.linearInterp(f, minTgtVal, maxTgtVal);
					
					cm2.addPoint(tgtVal, mp.getRange());
				}
			}
		} else if (source instanceof DiscreteMapping && target instanceof DiscreteMapping) {
			// TODO The problem here is that the new mapping entries haven't been created yet
//			final DiscreteMapping dm1 = (DiscreteMapping<?, ?>) source;
//			final DiscreteMapping dm2 = (DiscreteMapping<?, ?>) target;
//			final Map map1 = dm1.getAll();
//			final Map map2 = dm2.getAll();
//			System.out.println("MAP 1: " + map1.size());
//			System.out.println("MAP 2: " + map2.size());
//			
//			if (map1 == null || map1.isEmpty() || map2 == null || map2.isEmpty())
//				return;
//			
//			final Iterator<?> tgtKeyIter = map2.keySet().iterator();
//			
//			for (final Object srcVal : map1.values()) {
//				if (tgtKeyIter.hasNext()) {
//					final Object tgtKey = tgtKeyIter.next();
//					dm2.putMapValue(tgtKey, srcVal);
//				} else {
//					break;
//				}
//			}
		}
	}
}
