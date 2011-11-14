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
import org.cytoscape.model.CyNetwork;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 *
 */
// TODO: this function is broken.
public class GenerateSeriesAction extends AbstractVizMapperAction {

	private final static long serialVersionUID = 121374883715581L;

	public GenerateSeriesAction(CyApplicationManager appManager, final PropertySheetPanel propertySheetPanel) {
		super("Generate Series", appManager, propertySheetPanel);
	}

	private <K, V extends Number> void generate(CyNetwork targetNetwork) {

//		VisualProperty<V> vp = (VisualProperty<V>) vizMapperUtil
//				.getSelectedVisualProperty(propertySheetPanel);
//		DiscreteMapping<K, V> oMap = (DiscreteMapping<K, V>) vizMapperUtil.getSelectedProperty(
//				this.vizMapperMainPanel.getDefaultVisualStyle(),
//				propertySheetPanel);
//
//		if (vp != null && oMap != null) {
//
//			final CyTable attr = tableMgr.getTableMap(vp.getObjectType(),targetNetwork).get(
//						CyNetwork.DEFAULT_ATTRS);
//
//			final Set<K> attrSet = new TreeSet<K>(
//					attr.getColumnValues(oMap.getMappingAttributeName(), attr
//							.getColumnTypeMap().get(
//									oMap.getMappingAttributeName())));
//
//			final String start = JOptionPane.showInputDialog(
//					propertySheetPanel,
//					"Please enter start value (1st number in the series)", "0");
//			final String increment = JOptionPane.showInputDialog(
//					propertySheetPanel, "Please enter increment", "1");
//
//			if ((increment == null) || (start == null))
//				return;
//
//			V inc;
//			Float st;
//
//			try {
//				inc = (V) Float.valueOf(increment);
//				st = Float.valueOf(start);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				inc = null;
//				st = null;
//			}
//
//			if ((inc == null) || (inc.floatValue() < 0) || (st == null) || (st == null)) {
//				return;
//			}
//
//			Map<K, V> valueMap = new HashMap<K, V>();
//			if (vp.getType() == Number.class) {
//				for (K key : attrSet) {
//					valueMap.put(key, (V) st);
//					st = st.floatValue() + inc.floatValue();
//				}
//			}
//
//			oMap.putAll(valueMap);
//
//			propertySheetPanel.removeProperty(prop);
//
//			final VizMapperProperty<?> newRootProp = new VizMapperProperty();
//
//			if (vp.getObjectType().equals(NODE))
//				vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(
//						oMap, newRootProp, vp.getObjectType(),
//						propertySheetPanel);
//			else
//				vizMapPropertySheetBuilder.getPropertyBuilder().buildProperty(
//						oMap, newRootProp,
//						VizMapperMainPanel.EDGE_VISUAL_MAPPING,
//						propertySheetPanel);
//
//			vizMapPropertySheetBuilder.removeProperty(prop);
//			vizMapPropertySheetBuilder.getPropertyMap().get(
//					vmm.getVisualStyle().getName()).add(newRootProp);
//
//			vizMapPropertySheetBuilder.expandLastSelectedItem(type.getName());
//		} else {
//			System.out.println("Invalid.");
//		}

	}

	/**
	 * User wants to Seed the Discrete Mapper with Random Color Values.
	 */
	public void actionPerformed(ActionEvent e) {
		final CyNetwork targetNetwork = applicationManager.getCurrentNetwork();
		generate(targetNetwork);
	}
}
