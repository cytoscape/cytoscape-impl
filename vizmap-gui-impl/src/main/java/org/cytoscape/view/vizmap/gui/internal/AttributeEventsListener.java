package org.cytoscape.view.vizmap.gui.internal;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;


public class AttributeEventsListener  implements RowsSetListener {
	private CyComboBoxPropertyEditor propEditor;
	private Class<?> filter;
	private final CyTable attr;
	private CyApplicationManager applicationManager;
	
	private static final String NAME = "name";

	/**
	 * Constructor.
	 * 
	 * @param cyAttributes
	 *            CyTable
	 */
	public AttributeEventsListener(CyComboBoxPropertyEditor propEditor, Class<?> filter, 
			CyTable attributes, CyApplicationManager applicationManager)
	{
		this.attr = attributes;
		this.filter = filter;
		this.propEditor = propEditor;
		this.applicationManager = applicationManager;

		// populate our lists
		updateAttrList();
	}

	/**
	 * Our implementation of MultiHashMapListener.attributeValueAssigned().
	 * 
	 * @param objectKey
	 *            String
	 * @param attributeName
	 *            String
	 * @param keyIntoValue
	 *            Object[]
	 * @param oldAttributeValue
	 *            Object
	 * @param newAttributeValue
	 *            Object
	 */

	public Object getEventSource() {
		return attr; 
	}

	public void handleRowSet(final CyRow row, final String attributeName, final Object newValue,
				 final Object newRawValue)
	{
//		// conditional repaint container
//		boolean repaint = false;
//
//		// this code gets called a lot
//		// so i've decided to keep the next two if statements as is,
//		// rather than create a shared general routine to call
//
//		// if attribute is not in attrEditorNames, add it if we support its
//		// type
//		if (!attrEditorNames.contains(attributeName)) {
//			attrEditorNames.add(attributeName);
//			Collections.sort(attrEditorNames);
//			// attrEditor.setAvailableValues(attrEditorNames.toArray());
//			spcs.firePropertyChange("UPDATE_AVAILABLE_VAL", "attrEditor",
//					attrEditorNames.toArray());
//			repaint = true;
//		}
//
//		// if attribute is not contained in numericalAttrEditorNames, add it
//		// if we support its class
//		if (!numericalAttrEditorNames.contains(attributeName)) {
//			Class<?> dataClass = attr.getColumnTypeMap().get(attributeName);
//
//			if ((dataClass == Integer.class) || (dataClass == Double.class)) {
//				numericalAttrEditorNames.add(attributeName);
//				Collections.sort(numericalAttrEditorNames);
//				// numericalAttrEditor.setAvailableValues(numericalAttrEditorNames.toArray());
//				spcs.firePropertyChange("UPDATE_AVAILABLE_VAL",
//						"numericalAttrEditorNames", numericalAttrEditorNames
//								.toArray());
//				repaint = true;
//			}
//		}
//
//		if (repaint)
//			targetComponent.repaint();
	}

	/**
	 * Our implementation of MultiHashMapListener.allAttributeValuesRemoved()
	 * 
	 * @param objectKey
	 *            String
	 * @param attributeName
	 *            String
	 */
	public void handleEvent(ColumnDeletedEvent e) {
		String attributeName = e.getColumnName();

//		// we do not process network attributes
//		if (attr == applicationManager.getCurrentNetwork()
//				.getNetworkCyDataTables().get(CyNetwork.DEFAULT_ATTRS))
//			return;
//
//		// conditional repaint container
//		boolean repaint = false;
//
//		// this code gets called a lot
//		// so i've decided to keep the next two if statements as is,
//		// rather than create a shared general routine to call
//
//		// if attribute is in attrEditorNames, remove it
//		if (attrEditorNames.contains(attributeName)) {
//			attrEditorNames.remove(attributeName);
//			Collections.sort(attrEditorNames);
//			// attrEditor.setAvailableValues(attrEditorNames.toArray());
//			spcs.firePropertyChange("UPDATE_AVAILABLE_VAL", "attrEditor",
//					attrEditorNames.toArray());
//			repaint = true;
//		}
//
//		// if attribute is in numericalAttrEditorNames, remove it
//		if (numericalAttrEditorNames.contains(attributeName)) {
//			numericalAttrEditorNames.remove(attributeName);
//			Collections.sort(numericalAttrEditorNames);
//			// numericalAttrEditor.setAvailableValues(numericalAttrEditorNames.toArray());
//			spcs.firePropertyChange("UPDATE_AVAILABLE_VAL",
//					"numericalAttrEditor", numericalAttrEditorNames.toArray());
//			repaint = true;
//		}
//
//		if (repaint)
//			targetComponent.repaint();
	}

	/**
	 * Method to populate attrEditorNames & numericalAttrEditorNames on object
	 * instantiation.
	 */
	private void updateAttrList() {

		// Attribute Names
		if(attr== null) {
			
		}
		
		final List<String> names = new ArrayList<String>(CyTableUtil.getColumnNames(attr));
		Collections.sort(names);
		
//		attrEditorNames.add(NAME);
//
//		byte type;
//		Class<?> dataClass;
//
//		for (String name : names) {
//			attrEditorNames.add(name);
//			dataClass = attr.getColumnTypeMap().get(name);
//
//			if ((dataClass == Integer.class) || (dataClass == Double.class)) {
//				numericalAttrEditorNames.add(name);
//			}
//		}
	}

	public void handleEvent(ColumnCreatedEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		// TODO Auto-generated method stub
		
	}

}
