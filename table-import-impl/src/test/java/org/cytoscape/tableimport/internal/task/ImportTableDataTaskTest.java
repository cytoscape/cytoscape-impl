package org.cytoscape.tableimport.internal.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.task.ImportTableDataTask.TableType;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2026 The Cytoscape Consortium
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

/**
 * CYTOSCAPE-13174: a failed table import (no rows copied) must not add columns to the target network.
 */
public class ImportTableDataTaskTest {

	private static final String NEW_COL = "type";

	private NetworkTestSupport netSupport;
	private TableTestSupport tableSupport;
	private CyServiceRegistrar serviceRegistrar;
	private CyApplicationManager appMgr;
	private TaskMonitor tm;

	@Before
	public void setUp() {
		netSupport = new NetworkTestSupport();
		tableSupport = new TableTestSupport();
		serviceRegistrar = mock(CyServiceRegistrar.class);
		appMgr = mock(CyApplicationManager.class);
		tm = mock(TaskMonitor.class);

		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netSupport.getNetworkManager());
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(netSupport.getRootNetworkFactory());
		when(serviceRegistrar.getService(CyNetworkTableManager.class)).thenReturn(netSupport.getNetworkTableManager());
		when(serviceRegistrar.getService(CyTableManager.class)).thenReturn(mock(CyTableManager.class));
	}

	@Test
	public void testEmptyNetworkCollectionImportAddsNoColumns() throws Exception {
		var net = createNetwork();

		runImport(ImportTableDataTask.NETWORK_COLLECTION);

		assertColumnAbsent(net);
	}

	@Test
	public void testEmptyNetworkSelectionImportAddsNoColumns() throws Exception {
		var net = createNetwork();

		runImport(ImportTableDataTask.NETWORK_SELECTION);

		assertColumnAbsent(net);
	}

	@Test
	public void testNoMatchingKeysCollectionImportAddsNoColumns() throws Exception {
		var net = createNetwork("Unrelated 1", "Unrelated 2");

		runImport(ImportTableDataTask.NETWORK_COLLECTION);

		assertColumnAbsent(net);
	}

	@Test
	public void testNoMatchingKeysSelectionImportAddsNoColumns() throws Exception {
		var net = createNetwork("Unrelated 1", "Unrelated 2");

		runImport(ImportTableDataTask.NETWORK_SELECTION);

		assertColumnAbsent(net);
	}

	@Test
	public void testSuccessfulCollectionImportAddsColumn() throws Exception {
		var net = createNetwork("File A", "Unrelated");

		runImport(ImportTableDataTask.NETWORK_COLLECTION);

		var nodeTable = net.getDefaultNodeTable();
		assertNotNull(nodeTable.getColumn(NEW_COL));
		var root = netSupport.getRootNetworkFactory().getRootNetwork(net);
		assertNotNull(root.getTable(CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS).getColumn(NEW_COL));
		assertEquals("summary", getNodeValue(net, "File A"));
		assertNull(getNodeValue(net, "Unrelated"));
	}

	@Test
	public void testSuccessfulSelectionImportAddsColumn() throws Exception {
		var net = createNetwork("File B");

		runImport(ImportTableDataTask.NETWORK_SELECTION);

		assertNotNull(net.getDefaultNodeTable().getColumn(NEW_COL));
		assertEquals("summary", getNodeValue(net, "File B"));
	}

	@Test
	public void testExistingColumnIsKeptWhenNothingImported() throws Exception {
		var net = createNetwork();
		net.getDefaultNodeTable().createColumn(NEW_COL, String.class, false);

		runImport(ImportTableDataTask.NETWORK_COLLECTION);

		// The column was there before the import, so the rollback must not remove it
		assertNotNull(net.getDefaultNodeTable().getColumn(NEW_COL));
	}

	@Test
	public void testExceptionDuringCollectionImportRemovesColumns() {
		assertExceptionRollsBack(ImportTableDataTask.NETWORK_COLLECTION);
	}

	@Test
	public void testExceptionDuringSelectionImportRemovesColumns() {
		assertExceptionRollsBack(ImportTableDataTask.NETWORK_SELECTION);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFailedImportReportsNoMappedTables() throws Exception {
		createNetwork();

		for (var where : List.of(ImportTableDataTask.NETWORK_COLLECTION, ImportTableDataTask.NETWORK_SELECTION)) {
			var task = runImport(where);

			assertTrue(where, ((List<CyTable>) task.getResults(List.class)).isEmpty());
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSuccessfulImportReportsMappedTables() throws Exception {
		createNetwork("File A");

		for (var where : List.of(ImportTableDataTask.NETWORK_COLLECTION, ImportTableDataTask.NETWORK_SELECTION)) {
			var task = runImport(where);

			assertFalse(where, ((List<CyTable>) task.getResults(List.class)).isEmpty());
		}
	}

	// ---- helpers ----

	private CyNetwork createNetwork(String... nodeNames) {
		var net = netSupport.getNetwork();
		net.getRow(net).set(CyNetwork.NAME, "Network");
		var root = netSupport.getRootNetworkFactory().getRootNetwork(net);
		root.getRow(root).set(CyNetwork.NAME, "Network Collection");

		for (var name : nodeNames) {
			var node = net.addNode();
			net.getRow(node).set(CyNetwork.NAME, name);
			net.getRow(node).set(CyRootNetwork.SHARED_NAME, name);
		}

		netSupport.getNetworkManager().addNetwork(net, false);
		when(appMgr.getCurrentNetwork()).thenReturn(net);

		return net;
	}

	/** Same content as the test04.csv file attached to CYTOSCAPE-13174. */
	private CyTable createSourceTable() {
		var table = tableSupport.getTableFactory().createTable("test04.csv", CyRootNetwork.SHARED_NAME, String.class,
				true, true);
		table.createColumn(NEW_COL, String.class, false);

		for (var key : List.of("File A", "File B", "File C", "File D", "File E", "File F", "File G"))
			table.getRow(key).set(NEW_COL, "summary");

		return table;
	}

	private ImportTableDataTask runImport(String where) throws Exception {
		return runImport(where, createSourceTable());
	}

	private ImportTableDataTask runImport(String where, CyTable sourceTable) throws Exception {
		var reader = mock(CyTableReader.class);
		when(reader.getTables()).thenReturn(new CyTable[] { sourceTable });

		var context = new TableImportContext();
		context.setTableType(TableType.NODE_ATTR);

		var task = new ImportTableDataTask(reader, context, serviceRegistrar);
		task.getWhereImportTable().setSelectedValue(where);
		task.run(tm);

		return task;
	}

	/**
	 * Wraps the test04 table so that reading a row fails after the new columns have been created,
	 * so the rollback in the exception path can be tested.
	 */
	private CyTable createFailingSourceTable() {
		var realTable = createSourceTable();
		var table = mock(CyTable.class, delegatesTo(realTable));
		doThrow(new RuntimeException("Simulated failure")).when(table).getRow(any());

		return table;
	}

	private void assertExceptionRollsBack(String where) {
		var net = createNetwork("File A", "File B");

		try {
			runImport(where, createFailingSourceTable());
			fail("Expected the simulated failure to be thrown");
		} catch (Exception e) {
			assertEquals("Simulated failure", e.getMessage());
		}

		assertColumnAbsent(net);
	}

	private void assertColumnAbsent(CyNetwork net) {
		var root = netSupport.getRootNetworkFactory().getRootNetwork(net);

		assertNull(net.getDefaultNodeTable().getColumn(NEW_COL));
		assertNull(net.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(NEW_COL));
		assertNull(root.getTable(CyNode.class, CyRootNetwork.SHARED_DEFAULT_ATTRS).getColumn(NEW_COL));
		assertNull(root.getSharedNodeTable().getColumn(NEW_COL));
		assertNull(root.getDefaultNodeTable().getColumn(NEW_COL));
	}

	private String getNodeValue(CyNetwork net, String nodeName) {
		for (var row : net.getDefaultNodeTable().getAllRows()) {
			if (nodeName.equals(row.get(CyNetwork.NAME, String.class)))
				return row.get(NEW_COL, String.class);
		}

		return null;
	}
}
