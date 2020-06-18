package org.cytoscape.view.model.table;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.table.CyTableViewImpl;
import org.junit.Test;
/**
 * Add TableViewTestSupport
 * - test basic visual properties
 * - test parallel access to vps
 * - test events
 * - test function visual property
 * 
 * @author mkucera
 *
 */
public class TableViewImplTest {

	private TableTestSupport tableSupport = new TableTestSupport();

	private CyTableViewImpl createBasicTestTableView() {
		CyTable table = tableSupport.getTableFactory().createTable("test", "SUID", Long.class, true, true);
		table.createColumn("i", Integer.class, false);
		table.createColumn("f", Double.class, false);
		table.createColumn("s", String.class, false);
		table.createListColumn("ls", String.class, false);
		CyRow row = table.getRow(99L);
		row.set("i", 99);
		row.set("f", 99.0);
		row.set("s", "99");
		row.set("ls", Arrays.asList("99"));
		return TableViewTestUtils.createTableView(table);
	}
	
	private static void assertColExists(CyTableView tableView, String name) {
		View<CyColumn> col = tableView.getColumnView("SUID");
		assertNotNull(col);
		assertTrue(col instanceof CyColumnView);
	}
	
	private static void assertRowExists(CyTableViewImpl tableView, Object pk) {
		View<CyRow> row = tableView.getRowViewByPk(pk);
		assertNotNull(row);
	}
	
	
	@Test
	public void testTableViewCreation() {
		CyTableViewImpl tableView = createBasicTestTableView();
		
		assertEquals(5, tableView.getColumnViews().size());
		assertColExists(tableView, "SUID");
		assertColExists(tableView, "i");
		assertColExists(tableView, "f");
		assertColExists(tableView, "s");
		assertColExists(tableView, "ls");
		
		assertEquals(1, tableView.getRowViews().size());
		assertRowExists(tableView, 99L);
	}
	
	@Test
	public void testVisualProperties() {
		CyTableViewImpl tableView = createBasicTestTableView();
		View<CyColumn> colInt = tableView.getColumnView("i");
		View<CyColumn> colFlt = tableView.getColumnView("f");
		View<CyColumn> colStr = tableView.getColumnView("s");
		View<CyColumn> colLst = tableView.getColumnView("ls");
		
		tableView.setViewDefault(CELL_FORMAT, "asdf");
		colInt.setVisualProperty(CELL_FORMAT, "qwerty");
		colFlt.setVisualProperty(CELL_FORMAT, "qwerty");
		colStr.setVisualProperty(CELL_FORMAT, "qwerty");
		colStr.setLockedValue(CELL_FORMAT, "zxcv");
		
		assertEquals(colLst.getVisualProperty(CELL_FORMAT), "asdf");
		assertEquals(colInt.getVisualProperty(CELL_FORMAT), "qwerty");
		assertEquals(colFlt.getVisualProperty(CELL_FORMAT), "qwerty");
		assertEquals(colStr.getVisualProperty(CELL_FORMAT), "zxcv");
	}
	
	
}
