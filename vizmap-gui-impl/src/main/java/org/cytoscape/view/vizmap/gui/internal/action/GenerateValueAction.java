/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * 
 * 
 * @param <T>
 *            Type of attribute values.
 */
public class GenerateValueAction<K, V> extends AbstractVizMapperAction {

	private CyTableManager tableMgr;
	/**
	 * Creates a new GenerateValueAction object.
	 */
	public GenerateValueAction(CyApplicationManager applicationManager, final PropertySheetPanel propertySheetPanel) {
		super("generate", applicationManager, propertySheetPanel);
		this.tableMgr = tableMgr;
	}

	private final static long serialVersionUID = 1213748836986412L;
	private DiscreteMappingGenerator<K> generator;
	private DiscreteMapping<K, V> dm;

	/**
	 * User wants to Seed the Discrete Mapper with Random Color Values.
	 */
	public void actionPerformed(ActionEvent e) {
		// Check Selected property
		final int selectedRow = propertySheetPanel.getTable().getSelectedRow();
// FIXME
//		if (selectedRow < 0)
//			return;
//
//		final Item item = (Item) propertySheetPanel.getTable().getValueAt(
//				selectedRow, 0);
//		final VizMapperProperty<?> prop = (VizMapperProperty<?>) item
//				.getProperty();
//		Object hidden = prop.getHiddenObject();
//
//		if (hidden instanceof VisualProperty) {
//			final CyNetworkView targetNetworkView = cyNetworkManager
//					.getCurrentNetworkView();
//
//			final VisualProperty<?> type = (VisualProperty<?>) hidden;
//
//			Map valueMap = new HashMap();
//			final long seed = System.currentTimeMillis();
//			final Random rand = new Random(seed);
//
//			final CyTable attr;
//
//			attr = tableMgr.getTableMap(type.getObjectType(), cyNetworkManager.getCurrentNetwork()).get(CyNetwork.DEFAULT_ATTRS);
//
//			final VisualMappingFunction oMap = vmm.getVisualStyle(
//					targetNetworkView).getVisualMappingFunction(type);
//			// This function is for discrete mapping only.
//			if ((oMap instanceof DiscreteMapping) == false)
//				return;
//
//			dm = (DiscreteMapping) oMap;
//
//			final Set<Object> attrSet = new TreeSet<Object>(attr
//					.getColumnValues(oMap.getMappingAttributeName(), attr
//							.getColumnTypeMap().get(
//									oMap.getMappingAttributeName())));
//
//			// Show error if there is no attribute value.
//			if (attrSet.size() == 0) {
//				JOptionPane.showMessageDialog(vizMapperMainPanel,
//						"No attribute value is available.",
//						"Cannot generate values", JOptionPane.ERROR_MESSAGE);
//			}
//
//			// /*
//			// * Create random colors
//			// */
//			// final float increment = 1f / ((Number)
//			// attrSet.size()).floatValue();
//			//
//			// float hue = 0;
//			// float sat = 0;
//			// float br = 0;
//			//
//			// if (type.getType() == Color.class) {
//			// int i = 0;
//			//
//			// if (functionType == RAINBOW1) {
//			// for (Object key : attrSet) {
//			// hue = hue + increment;
//			// valueMap.put(key,
//			// new Color(Color.HSBtoRGB(hue, 1f, 1f)));
//			// }
//			// } else if (functionType == RAINBOW2) {
//			// for (Object key : attrSet) {
//			// hue = hue + increment;
//			// sat = (Math.abs(((Number) Math.cos((8 * i)
//			// / (2 * Math.PI))).floatValue()) * 0.7f) + 0.3f;
//			// br = (Math.abs(((Number) Math.sin(((i) / (2 * Math.PI))
//			// + (Math.PI / 2))).floatValue()) * 0.7f) + 0.3f;
//			// valueMap.put(key, new Color(Color
//			// .HSBtoRGB(hue, sat, br)));
//			// i++;
//			// }
//			// } else {
//			// for (Object key : attrSet)
//			// valueMap.put(key, new Color(
//			// ((Number) (rand.nextFloat() * MAX_COLOR))
//			// .intValue()));
//			// }
//			// } else if ((type.getType() == Number.class)
//			// && (functionType == RANDOM)) {
//			// final String range = JOptionPane.showInputDialog(
//			// visualPropertySheetPanel,
//			// "Please enter the value range (example: 30-100)",
//			// "Assign Random Numbers", JOptionPane.PLAIN_MESSAGE);
//			//
//			// String[] rangeVals = range.split("-");
//			//
//			// if (rangeVals.length != 2)
//			// return;
//			//
//			// Float min = Float.valueOf(rangeVals[0]);
//			// Float max = Float.valueOf(rangeVals[1]);
//			// Float valueRange = max - min;
//			//
//			// for (Object key : attrSet)
//			// valueMap.put(key, (rand.nextFloat() * valueRange) + min);
//			// }
//			valueMap = generator.generateMap(attrSet);
//
//			dm.putAll(valueMap);
//			// vmm.setNetworkView(targetNetworkView);
//			// Cytoscape.redrawGraph(targetNetworkView);
//			propertySheetPanel.removeProperty(prop);
//
//			// final VizMapperProperty newRootProp = new VizMapperProperty();
//			//
//			// if (type.getObjectType().equals(VisualProperty.NODE))
//			// buildProperty(visualMappingManager.getVisualStyle()
//			// .getNodeAppearanceCalculator().getCalculator(type),
//			// newRootProp, NODE_VISUAL_MAPPING);
//			// else
//			// buildProperty(vmm.getVisualStyle()
//			// .getEdgeAppearanceCalculator().getCalculator(type),
//			// newRootProp, EDGE_VISUAL_MAPPING);
//			//
//			// removeProperty(prop);
//			// System.out.println("asdf pre vs name");
//			// System.out.println("asdf vs name" +
//			// vmm.getVisualStyle().getName());
//			// propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
//			//
//			// expandLastSelectedItem(type.getName());
//			// } else {
//			// System.out.println("Invalid.");
//			// }
//			//
//			// return;
//		}
	}
}
