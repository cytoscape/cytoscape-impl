package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;
import org.junit.Test;

public class BrowserTableTest {
	static final String SHARED_NAME  = "shared name";
	
	CyTable table;
	CyTable sharedTable;
	BrowserTable browserTable;
	BrowserTableModel btm;
	BrowserTableColumnModel btcm;
	CyTableManager tableManager;
	
	CyEventHelper eventHelper;
	
	public BrowserTableTest(){
		EquationCompiler equationCompiler = mock(EquationCompiler.class);
		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(mock(TaskManager.class), mock(OpenBrowser.class));
		tableManager = mock(CyTableManager.class);
		eventHelper = new DummyCyEventHelper();
		browserTable = new BrowserTable(equationCompiler, popupMenuHelper, mock(CyApplicationManager.class), eventHelper, tableManager );
		
		createTable();
		assertEquals(4, table.getColumns().size());
		
		btm = new BrowserTableModel(table, CyNode.class, equationCompiler, tableManager);
		browserTable.setModel(btm);
		btcm = (BrowserTableColumnModel) browserTable.getColumnModel();
		btcm.setAllColumnsVisible();
	}
	
	
	void createTable (){
		sharedTable = new CyTableImpl("shared table", CyNetwork.SUID, Long.class, false, true, SavePolicy.DO_NOT_SAVE, eventHelper, mock(Interpreter.class), 0);
		sharedTable.createColumn(SHARED_NAME, String.class, true);
		table = new CyTableImpl("test table", CyNetwork.SUID, Long.class, true, true, SavePolicy.DO_NOT_SAVE , eventHelper, mock(Interpreter.class), 0);
		table.createColumn(CyNetwork.NAME, String.class, true);
		table.createColumn(CyNetwork.SELECTED, Boolean.class, true, false);
		table.addVirtualColumn(SHARED_NAME, SHARED_NAME, sharedTable, CyNetwork.SUID, true);
		tableManager.addTable(table);

		Long id = (long) 1;
		
		CyRow row1 =  table.getRow(id);
		row1.set(CyNetwork.NAME, "row1");
		
		CyRow sharedRow1 = sharedTable.getRow(id);
		sharedRow1.set(SHARED_NAME,  "yek");
		
		CyRow row2 =  table.getRow(id+1);
		row2.set(CyNetwork.NAME, "row2");
	
		CyRow row3 =  table.getRow(id+2);
		row3.set(CyNetwork.NAME, "row3");
		
		CyRow sharedRow3 = sharedTable.getRow(id+2);
		sharedRow3.set(SHARED_NAME,  "se");
		
	}
	
	@Test
	public void testInitialization(){
		assertEquals(4 ,btm.getColumnCount());
		assertEquals(4 ,btcm.getColumnCount());
		assertEquals(4 , browserTable.getColumnCount());
		
		assertEquals(3, btm.getRowCount());
		assertEquals(3, browserTable.getRowCount());

	}
	
	@Test
	public void testColumnState(){
		//is primary key column non-editable

		int suidMIndex =  btm.mapColumnNameToColumnIndex(CyNetwork.SUID);
		int suidVIndex = browserTable.convertColumnIndexToView(suidMIndex);

		assertFalse(browserTable.isCellEditable(1,suidVIndex));


		int nameMIndex =  btm.mapColumnNameToColumnIndex(CyNetwork.NAME);
		int nameVIndex = browserTable.convertColumnIndexToView(nameMIndex);
		
		assertTrue(browserTable.isCellEditable(1, nameVIndex));
		
		//move columns and check it
		btcm.moveColumn(suidVIndex, 2); //2 is just a random number but we know it is less than number of columns 4
		suidVIndex = browserTable.convertColumnIndexToView(suidMIndex);
		assertFalse(browserTable.isCellEditable(1, suidVIndex));
		
		nameVIndex = browserTable.convertColumnIndexToView(nameMIndex);
		assertTrue(browserTable.isCellEditable(1, nameVIndex));
		
		
		//hide a column and check it
		btcm.setColumnVisible( btcm.getColumn(nameVIndex) , false); //hiding name column
		suidVIndex = browserTable.convertColumnIndexToView(suidMIndex);
		assertFalse(browserTable.isCellEditable(1, suidVIndex));
		
	}
	
	
	@Test
	public void testColumnPlacement(){
		
		btcm.setAllColumnsVisible();
		//to make sure the columns are not sorted based on model index move them around
		btcm.moveColumn(1,3);
		btcm.moveColumn(2, 0);
		
		int suidMIndex =  btm.mapColumnNameToColumnIndex(CyNetwork.SUID);
		int suidVIndex = browserTable.convertColumnIndexToView(suidMIndex);

		int nameMIndex =  btm.mapColumnNameToColumnIndex(CyNetwork.NAME);
		int nameVIndex = browserTable.convertColumnIndexToView(nameMIndex);
		

		int selectedMIndex =  btm.mapColumnNameToColumnIndex(CyNetwork.SELECTED);
		int selctedVIndex = browserTable.convertColumnIndexToView(selectedMIndex);
		

		int sharedNameMIndex =  btm.mapColumnNameToColumnIndex(SHARED_NAME);
		int sharedNameVIndex = browserTable.convertColumnIndexToView(sharedNameMIndex);
		
		//hide and show all the indexes should be same
		browserTable.setVisibleAttributeNames(new ArrayList<String>()); //hide all
		assertEquals(false, btcm.getColumns(true).hasMoreElements());
		assertEquals(-1,  browserTable.convertColumnIndexToView(suidMIndex));
		assertEquals(-1,  browserTable.convertColumnIndexToView(nameMIndex));
		assertEquals(-1,  browserTable.convertColumnIndexToView(selectedMIndex));
		assertEquals(-1,  browserTable.convertColumnIndexToView(sharedNameMIndex));

		btcm.setAllColumnsVisible();
		assertEquals(suidVIndex,  browserTable.convertColumnIndexToView(suidMIndex));
		assertEquals(nameVIndex,  browserTable.convertColumnIndexToView(nameMIndex));
		assertEquals(selctedVIndex,  browserTable.convertColumnIndexToView(selectedMIndex));
		assertEquals(sharedNameVIndex,  browserTable.convertColumnIndexToView(sharedNameMIndex));

		
		//hide column 2 and check if column 3 is now column 2
		
		String col3 = btm.getColumnName( browserTable.convertColumnIndexToModel(3));
		String col2 = btm.getColumnName( browserTable.convertColumnIndexToModel(2));
		btcm.setColumnVisible(btcm.getColumn(2), false);
		assertEquals(col3, btm.getColumnName( browserTable.convertColumnIndexToModel(2)));
		
		//now show back col2 and check if the index is 2 and if col3 is moved back to index 3
		btcm.setAllColumnsVisible();
		assertEquals(col2, btm.getColumnName( browserTable.convertColumnIndexToModel(2)));
		assertEquals(col3, btm.getColumnName( browserTable.convertColumnIndexToModel(3)));
	
	}
	
	
	@Test
	public void testTableModeAndSelection(){
		
		browserTable.setModel(btm); //to reset the columns and rows

		btm.setShowAll(true);
		//btm.fireTableDataChanged();

		assertEquals(3, browserTable.getRowCount());
		
		btm.setShowAll(false);
		//btm.fireTableDataChanged();

		assertEquals(0, browserTable.getRowCount());
		
		
		table.getRow((long)1).set(CyNetwork.SELECTED, true);
		assertEquals(1, browserTable.getRowCount());
		
		btm.setShowAll(true);
		RowSetRecord rsc = new RowSetRecord  (table.getRow((long)1), CyNetwork.SELECTED, (Object) true , (Object) true );
		List<RowSetRecord> rscs = new ArrayList<RowSetRecord>();
		rscs.add(rsc);
		
		browserTable.handleEvent(new RowsSetEvent(table, rscs));
		assertEquals(1, browserTable.getSelectedRowCount());

		table.getRow((long)2).set(CyNetwork.SELECTED, true);
		 rsc = new RowSetRecord  (table.getRow((long)2), CyNetwork.SELECTED, (Object) true , (Object) true );
		 rscs = new ArrayList<RowSetRecord>();
		rscs.add(rsc);
		
		browserTable.handleEvent(new RowsSetEvent(table, rscs));
		assertEquals(2, browserTable.getSelectedRowCount());
		
		table.getRow((long)2).set(CyNetwork.SELECTED, false);
		 rsc = new RowSetRecord  (table.getRow((long)2), CyNetwork.SELECTED, (Object) false , (Object) false );
		 rscs = new ArrayList<RowSetRecord>();
		rscs.add(rsc);
		
		browserTable.handleEvent(new RowsSetEvent(table, rscs));
		assertEquals(1, browserTable.getSelectedRowCount());
		
		browserTable.mouseReleased(new MouseEvent(browserTable, 0, 0, 0, browserTable.getCellRect(1, 0, true).x, browserTable.getCellRect(1, 0, true).y, Math.abs( browserTable.getCellRect(1, 0, true).x ), Math.abs( browserTable.getCellRect(1, 0, true).y ), 1, true, MouseEvent.BUTTON1));
		assertEquals(1, browserTable.getSelectedRowCount());

		
		browserTable.selectAll();
		assertEquals(3, browserTable.getSelectedRowCount());

	}
}
