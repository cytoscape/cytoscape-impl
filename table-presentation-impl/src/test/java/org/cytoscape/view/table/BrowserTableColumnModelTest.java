package org.cytoscape.view.table;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.table.TableColumn;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.column.ColumnDataFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.table.CyTableViewFactoryImpl;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.table.internal.BrowserTableVisualLexicon;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableColumnModel;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.junit.Before;
import org.junit.Test;

public class BrowserTableColumnModelTest {
	
	static final String SHARED_NAME = "shared name";
	
	private CyTable table;
	private BrowserTable browserTable;
	private CyTableView tableView;

	@Before
	public void setUp() {
		CyEventHelper eventHelper = mock(CyEventHelper.class);
		
		table = new CyTableImpl("test table", CyNetwork.SUID, Long.class, true, true, SavePolicy.DO_NOT_SAVE,
				eventHelper, ColumnDataFactory.createDefaultFactory(), mock(Interpreter.class), 0);
		table.createColumn(CyNetwork.NAME, String.class, true);
		table.createColumn(CyNetwork.SELECTED, Boolean.class, true, false);

		Long id = (long) 1;

		CyRow row1 = table.getRow(id);
		row1.set(CyNetwork.NAME, "row1");

		CyRow row2 = table.getRow(id + 1);
		row2.set(CyNetwork.NAME, "row2");

		CyRow row3 = table.getRow(id + 2);
		row3.set(CyNetwork.NAME, "row3");
		
		CyServiceRegistrar registrar = mock(CyServiceRegistrar.class);
		EquationCompiler equationCompiler = mock(EquationCompiler.class);
		when(registrar.getService(IconManager.class)).thenReturn(mock(IconManager.class));
		
		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(registrar);
		
		browserTable = new BrowserTable(popupMenuHelper, registrar);
		
		CyTableViewFactoryImpl factory = new CyTableViewFactoryImpl(registrar, new BrowserTableVisualLexicon(), "id");
		tableView = factory.createTableView(table);

		BrowserTableModel model = new BrowserTableModel(tableView, equationCompiler);
		browserTable.setModel(model);
	}
	
	@Test
	public void testVisibilityAndGravity() {
		// Set Up
		BrowserTableColumnModel colModel = (BrowserTableColumnModel) browserTable.getColumnModel();
		View<CyColumn> col1 = tableView.getColumnView(CyNetwork.SUID);
		TableColumn tc1 = colModel.getTableColumn(col1.getSUID());
		View<CyColumn> col2 = tableView.getColumnView(CyNetwork.NAME);
		TableColumn tc2 = colModel.getTableColumn(col2.getSUID());
		View<CyColumn> col3 = tableView.getColumnView(CyNetwork.SELECTED);
		TableColumn tc3 = colModel.getTableColumn(col3.getSUID());
		
		// Test basic gravity setting
		assertEquals(3, colModel.getColumnCount());
		
		colModel.setColumnGravity(tc1, 1);
		colModel.setColumnGravity(tc2, 2);
		colModel.setColumnGravity(tc3, 3);
		colModel.reorderColumnsToRespectGravity();
		
		assertEquals(col1.getSUID(), colModel.getColumn(0).getIdentifier());
		assertEquals(col2.getSUID(), colModel.getColumn(1).getIdentifier());
		assertEquals(col3.getSUID(), colModel.getColumn(2).getIdentifier());
		
		colModel.setColumnGravity(tc1, 3);
		colModel.setColumnGravity(tc2, 2);
		colModel.setColumnGravity(tc3, 1);
		colModel.reorderColumnsToRespectGravity();
		
		assertEquals(col3.getSUID(), colModel.getColumn(0).getIdentifier());
		assertEquals(col2.getSUID(), colModel.getColumn(1).getIdentifier());
		assertEquals(col1.getSUID(), colModel.getColumn(2).getIdentifier());
		
		// Test basic visibility setting
		colModel.setColumnVisible(tc2, false);
		
		assertEquals(2, colModel.getColumnCount());
		assertEquals(col3.getSUID(), colModel.getColumn(0).getIdentifier());
		assertEquals(col1.getSUID(), colModel.getColumn(1).getIdentifier());
		
		colModel.setColumnVisible(tc2, true);
		
		assertEquals(3, colModel.getColumnCount());
		assertEquals(col3.getSUID(), colModel.getColumn(0).getIdentifier());
		assertEquals(col2.getSUID(), colModel.getColumn(1).getIdentifier());
		assertEquals(col1.getSUID(), colModel.getColumn(2).getIdentifier());
		
		// Test changing gravity while a column isn't visible
		colModel.setColumnGravity(tc1, 1);
		colModel.setColumnGravity(tc2, 2);
		colModel.setColumnGravity(tc3, 3);
		colModel.reorderColumnsToRespectGravity();
		colModel.setColumnVisible(tc1, false);
		colModel.moveColumn(1, 0);
		colModel.setColumnVisible(tc1, true);
		
		assertEquals(3, colModel.getColumnCount());
		assertEquals(col1.getSUID(), colModel.getColumn(0).getIdentifier());
		assertEquals(col3.getSUID(), colModel.getColumn(1).getIdentifier());
		assertEquals(col2.getSUID(), colModel.getColumn(2).getIdentifier());
	}
	

}
