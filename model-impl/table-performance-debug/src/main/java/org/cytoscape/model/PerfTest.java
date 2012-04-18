package org.cytoscape.model; 



import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;


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
