package org.cytoscape.model;

/*
 * #%L
 * Cytoscape Model Impl Table Performance Debug (model-impl-table-performance-debug)
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



import java.util.Random;


public class PerfTest {

	private String[] colNames = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n"};
	private Class[] colTypes = new Class[] {String.class, Integer.class, Long.class, Double.class, Boolean.class};
	private Object[] colDefaults = new Object[] {"default", Integer.valueOf(0),Long.valueOf(0), Double.valueOf(0.0), Boolean.FALSE};
	private final int numRows = 100000;
	private final Random rand = new Random(numRows);
	private final int matching = 100;
	                                        

	private final CyTableFactory tableFactory;

	public static void main(String[] args) {
		new PerfTest().testTableUse();
	}

	public PerfTest() {
		TableTestSupport testSupport = new TableTestSupport();
		tableFactory = testSupport.getTableFactory();
	}

	private void testTableUse() {
		CyTable table = tableFactory.createTable("homer","primaryKey",Integer.class,true,true);

		System.out.println(" create columns");
		for (int i = 0; i<colNames.length; i++) 
			table.createColumn(colNames[i], colTypes[i%colTypes.length], false, colDefaults[i%colDefaults.length]);

		System.out.println(" set a bunch of rows");
		for (int i = 0; i < numRows; i++) { 
			CyRow row = table.getRow(Integer.valueOf(i));
			for ( int j = 0; j<colNames.length; j++)
				row.set(colNames[j], getRandomValue(colTypes[j%colTypes.length]));
		}

		System.out.println(" get a bunch of rows");
		for (int i = 0; i < numRows; i++) {
			CyRow row = table.getRow(Integer.valueOf(i));
			for ( int j = 0; j<colNames.length; j++)
				row.get(colNames[j], colTypes[j%colTypes.length]);
		}

		System.out.println(" get a bunch of rows in a different way");
		for ( CyRow row : table.getAllRows() ) 
			for ( int j = 0; j<colNames.length; j++)
				row.get(colNames[j], colTypes[j%colTypes.length]);

		System.out.println(" get all matching rows ");
		for ( int j = 0; j<colNames.length; j++)
			if ( colTypes[j % colTypes.length] == Integer.class )
				for ( int x = 0; x < matching; x++ )
					table.getMatchingRows(colNames[j],Integer.valueOf(x));
	}

	private Object getRandomValue(Class c) {
		if ( c == Integer.class )
			return Integer.valueOf( rand.nextInt(matching) );
		else if ( c == Long.class )
			return Long.valueOf( rand.nextLong() );
		else if ( c == Double.class )
			return Double.valueOf( rand.nextDouble() );
		else if ( c == Boolean.class )
			return Boolean.valueOf( rand.nextBoolean() );
		else if ( c == String.class )
			return Integer.toString(rand.nextInt());
		else 
			return null;
	}
}
