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

import org.cytoscape.model.CyTableManager;


public class ModifyBrightnessAction  {
	private final static long serialVersionUID = 121374883775182L;
	protected static final int DARKER = 1;
	protected static final int BRIGHTER = 2;
	private final int functionType;
	private CyTableManager tableMgr;

	/**
	 * Creates a new BrightnessListener object.
	 */
	public ModifyBrightnessAction(final int type, CyTableManager tableMgr) {
		this.functionType = type;
		this.tableMgr = tableMgr;
	}

	/**
	 * User wants to Seed the Discrete Mapper with Random Color Values.
	 */
	public void actionPerformed(ActionEvent e) {
		//final CyNetwork targetNetwork = applicationManager.getCurrentNetwork();

		//FIXME
//		/*
//		 * Check Selected property
//		 */
//		final int selectedRow = propertySheetPanel.getTable().getSelectedRow();
//
//		if (selectedRow < 0)
//			return;
//
//		final Item item = (Item) propertySheetPanel.getTable().getValueAt(
//				selectedRow, 0);
//		final VizMapperProperty<?> prop = (VizMapperProperty<?>) item
//				.getProperty();
//		final Object hidden = prop.getHiddenObject();
//
//		if (hidden instanceof VisualProperty) {
//			// OK, this is a Visual Property. Check data type next.
//			Class<?> t = ((VisualProperty<?>) hidden).getType();
//
//			if (t.equals(Color.class) == false)
//				return;
//
//			// This cast is always OK because of the type check above.
//			final VisualProperty<Color> type = (VisualProperty<Color>) hidden;
//
//			final Map<Object, Color> valueMap = new HashMap<Object, Color>();
//
//			final VisualStyle vs = this.vizMapperMainPanel
//					.getSelectedVisualStyle();
//
//			final CyTable attr = tableMgr.getTableMap(type.getObjectType(), targetNetwork).get(CyNetwork.DEFAULT_ATTRS);
//			// If not discrete, return.
//			if ((vs.getVisualMappingFunction(type) instanceof DiscreteMapping) == false)
//				return;
//
//			DiscreteMapping<Object, Color> dm = (DiscreteMapping<Object, Color>) vs
//					.getVisualMappingFunction(type);
//
//			final String attrName = dm.getMappingAttributeName();
//			List<Object> attrVals = attr.getColumnValues(attrName, dm
//					.getMappingAttributeType());
//
//			final Set<Object> attrSet = new TreeSet<Object>(attrVals);
//
//			if (type.getType() == Color.class) {
//				Color c;
//
//				if (functionType == BRIGHTER) {
//					for (Object key : attrSet) {
//						c = dm.getMapValue(key);
//
//						if (c != null)
//							valueMap.put(key, c.brighter());
//					}
//				} else if (functionType == DARKER) {
//					for (Object key : attrSet) {
//						c = dm.getMapValue(key);
//
//						if (c != null)
//							valueMap.put(key, c.darker());
//					}
//				}
//			}
//
//			dm.putAll(valueMap);
//
//			propertySheetPanel.removeProperty(prop);
//
//			final VizMapperProperty<VisualProperty<Color>> newRootProp = vizMapPropertySheetBuilder
//					.getPropertyBuilder().buildProperty(dm,
//							type.getObjectType(), propertySheetPanel);
//
//			vizMapPropertySheetBuilder.removeProperty(prop);
//			vizMapPropertySheetBuilder.getPropertyMap().get(vs)
//					.add(newRootProp);
//
//			vizMapPropertySheetBuilder.expandLastSelectedItem(type
//					.getDisplayName());
//		} else {
//			throw new IllegalStateException(
//					"Hidden object is not Visual Property.");
//		}
	}
}
