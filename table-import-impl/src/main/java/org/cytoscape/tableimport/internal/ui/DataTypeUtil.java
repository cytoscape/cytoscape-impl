
/*
 Copyright (c) 2006, 2007, 2009, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.tableimport.internal.ui;

//import cytoscape.Cytoscape;
import org.cytoscape.tableimport.internal.util.AttributeTypes;

import java.util.Map; 
import java.util.regex.Pattern; 
import javax.swing.table.TableModel;

/**
 *
 */
class DataTypeUtil {

	private static Pattern truePattern = Pattern.compile("^true$", Pattern.CASE_INSENSITIVE);
	private static Pattern falsePattern = Pattern.compile("^false$", Pattern.CASE_INSENSITIVE);
	
	private DataTypeUtil() {}

	static void guessTypes(final TableModel model, final String tableName, 
	                       Map<String,Byte[]> dataTypeMap) {

		//System.out.println("model row count: " + model.getRowCount() );
		//System.out.println("model column count: " + model.getColumnCount() );

		// 0 = Boolean,  1 = Integer,  2 = Double,  3 = String
		final Integer[][] typeChecker = new Integer[4][model.getColumnCount()];

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				typeChecker[i][j] = 0;
			}
		}

		String cell = null;

		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				cell = (String) model.getValueAt(i, j);
				//System.out.print( i + " " + j + " " + cell + " ");

				boolean found = false;

				if (cell != null) { 
					// boolean
					if ( truePattern.matcher(cell).matches() ||
					     falsePattern.matcher(cell).matches() ) {
						typeChecker[0][j]++;
						found = true;
						//System.out.println("boolean");
					} else {

						// integers
						try {
							Integer.valueOf(cell);
							typeChecker[1][j]++;
							found = true;
						//System.out.println("integer");
						} catch (NumberFormatException e) {
						}
			

						// floats
						try {
							Double.valueOf(cell);
							typeChecker[2][j]++;
							found = true;
						//System.out.println("float");
						} catch (NumberFormatException e) {
						}
					}
				}
				
				// default to string
				if (found == false) {
					typeChecker[3][j]++;
						//System.out.println("string");
				}
			}
		}

		Byte[] dataType = dataTypeMap.get(tableName);

		if ((dataType == null) || (dataType.length != model.getColumnCount())) {
			dataType = new Byte[model.getColumnCount()];
		}

		for (int i = 0; i < dataType.length; i++) {
			int maxVal = 0;
			int maxIndex = 0;

			for (int j = 0; j < 4; j++) {
				//System.out.println("col: " + i + " byte: " + j  + " count: " + typeChecker[j][i]);
				if (maxVal < typeChecker[j][i]) {
					maxVal = typeChecker[j][i];
					maxIndex = j;
				}
			}
	
			//System.out.println("  max index: " + maxIndex);

			if (maxIndex == 0)
				dataType[i] = AttributeTypes.TYPE_BOOLEAN;
			else if (maxIndex == 1)
				dataType[i] = AttributeTypes.TYPE_INTEGER;
			else if (maxIndex == 2)
				dataType[i] = AttributeTypes.TYPE_FLOATING;
			else
				dataType[i] = AttributeTypes.TYPE_STRING;

			//System.out.println("  resulting data type: " + dataType[i]);
		}

		dataTypeMap.put(tableName, dataType);
	}
}
