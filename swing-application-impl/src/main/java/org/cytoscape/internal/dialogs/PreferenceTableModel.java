package org.cytoscape.internal.dialogs;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;


/**
 *
 */
public class PreferenceTableModel extends AbstractTableModel {
	private final static long serialVersionUID = 1202339873369342L;
	static int[] columnWidth = new int[] { 150, 250 };
	static int[] alignment = new int[] { JLabel.LEFT, JLabel.LEFT };
	private Properties properties;
	Vector<String[]> propertiesList = new Vector<>();
	static String[] columnHeader = new String[] { "Property Name", "Value" };

	/**
	 * Creates a new PreferenceTableModel object.
	 */
	public PreferenceTableModel(Properties props) {
		super();
		properties = props;
		loadProperties();
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void loadProperties() {
		clearVector();

		for (Enumeration names = properties.propertyNames(); names.hasMoreElements();) {
			String name = (String) names.nextElement();
			addProperty(new String[] { name, properties.getProperty(name) });
		}

		//this.fireTableDataChanged();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param key DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	public void setProperty(String key, String value) {
		// update property object
		properties.setProperty(key, value);

		// update table model (propertiesList)
		
		for (Iterator it = propertiesList.iterator(); it.hasNext();) {
			String[] prop = (String[]) it.next();

			if (prop[0].equals(key)) {
				prop[1] = value;
			}
		}
		this.fireTableDataChanged();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param key DOCUMENT ME!
	 */
	public void deleteProperty(String key) {
		// remove property from property object
		properties.remove(key);

		// remove property from table model (propertiesList)
		for (int i=this.propertiesList.size()-1; i>=0;i--){
			String[] prop = (String[])this.propertiesList.get(i);
			if (prop[0].equals(key)) {
				propertiesList.remove(prop);
			}			
		}
		
		this.fireTableDataChanged();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param val DOCUMENT ME!
	 */
	public void addProperty(String[] val) {
		
		if ((val.length < 0) || (val.length > columnHeader.length))
			return;

		// add to table model (propertiesList vector) if not present,
		// otherwise replace existing entry
		boolean found = false;

		for (Iterator it = propertiesList.iterator(); it.hasNext();) {
			String[] prop = (String[]) it.next();

			if (prop[0].equals(val[0])) {
				prop[1] = val[1];
				found = true;
			}
		}

		if (!found)
			propertiesList.add(val);

		sort();
		// also add to local properties object for saving 
		properties.setProperty(val[0], val[1]);
		
		this.fireTableDataChanged();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param col DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getColumnName(int col) {
		return columnHeader[col];
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void clearVector() {
		propertiesList.clear();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param saveToProps DOCUMENT ME!
	 */
	public void save(Properties saveToProps) {
		// save local property values to passed-in Properties
		saveToProps.putAll(properties);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param restoreFromProps DOCUMENT ME!
	 */
	public void restore(Properties restoreFromProps) {
		properties.clear();
		properties.putAll(restoreFromProps);
		loadProperties();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getColumnCount() {
		return columnHeader.length;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param row DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Object getValueAt(int row, int col) {		
		String[] rowData = (String[]) propertiesList.get(row);
		return rowData[col];
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getRowCount() {
		return propertiesList.size();
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void sort() {
		Collections.sort(propertiesList, new StringComparator());
	}
	
}


class StringComparator implements Comparator<String[]> {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param str1 DOCUMENT ME!
	 * @param str2 DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int compare(String[] str1, String[] str2) {
		int result = 0;

		for (int i = 0; i < str1.length; i++) {
			result = str1[i].compareTo(str2[i]);

			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
