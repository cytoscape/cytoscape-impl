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
package org.cytoscape.view.vizmap.gui.internal.action;


import java.awt.event.ActionEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableManager;


public class ModifyBrightnessAction extends AbstractVizMapperAction {
	private final static long serialVersionUID = 121374883775182L;
	protected static final int DARKER = 1;
	protected static final int BRIGHTER = 2;
	private final int functionType;
	private CyTableManager tableMgr;

	/**
	 * Creates a new BrightnessListener object.
	 * 
	 * @param type
	 *            DOCUMENT ME!
	 */
	public ModifyBrightnessAction(final int type, CyTableManager tableMgr) {
		this.functionType = type;
		this.tableMgr = tableMgr;
	}

	/**
	 * User wants to Seed the Discrete Mapper with Random Color Values.
	 */
	public void actionPerformed(ActionEvent e) {
		final CyNetwork targetNetwork = applicationManager.getCurrentNetwork();

		//FIXME
//		/*
//		 * Check Selected poperty
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
