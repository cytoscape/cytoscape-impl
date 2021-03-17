package org.cytoscape.view.table.internal;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.ColumnResizer;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewChangedListener;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.cytoscape.view.table.internal.impl.icon.VisualPropertyIconFactory;

public class TableRenderingEngineImpl implements RenderingEngine<CyTable> {
	
	private final CyTableView tableView;
	private final VisualLexicon lexicon;
	private final PopupMenuHelper popupMenuHelper;
	private final CyServiceRegistrar registrar;
	
	private BrowserTable browserTable;
	private VisualPropertyChangeListener vpChangeListener;
	
	
	public TableRenderingEngineImpl(CyTableView tableView, VisualLexicon lexicon, PopupMenuHelper popupMenuHelper, CyServiceRegistrar registrar) {
		this.tableView = tableView;
		this.lexicon = lexicon;
		this.popupMenuHelper = popupMenuHelper;
		this.registrar = registrar;
	}
	
	private BrowserTable createBrowserTable() {
		var compiler = registrar.getService(EquationCompiler.class);
		var browserTable = new BrowserTable(popupMenuHelper, registrar);
		var model = new BrowserTableModel(tableView, compiler); // why does it need the element type? 
		
		browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		browserTable.setModel(model);
		
		// So the drop event can go straight through the table to the drop target associated with this panel
		if (browserTable.getDropTarget() != null)
			browserTable.getDropTarget().setActive(false);
		
		ColumnResizer.adjustColumnPreferredWidths(browserTable, false);
		
		return browserTable;
	}
	
	
	public void install(JComponent component) {
		// MKTODO there's more to it than this, there's a bunch of swing listeners to register and stuff
		this.browserTable = createBrowserTable();
		this.vpChangeListener = new VisualPropertyChangeListener(browserTable, tableView);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(browserTable);
		
		component.setLayout(new BorderLayout());
		component.add(scrollPane);
		
		registerServices();
	}
	
	public BrowserTable getBrowserTable() {
		return this.browserTable;
	}
	
	
	@Override
	public void dispose() {
		unregisterServices();
	}
	
	
	private void registerServices() {
		registrar.registerAllServices(browserTable, new Properties());
		registrar.registerAllServices(browserTable.getModel(), new Properties());
		registrar.registerService(vpChangeListener, TableViewChangedListener.class, new Properties());
	}
	
	private void unregisterServices() {
		registrar.unregisterAllServices(browserTable);
		registrar.unregisterAllServices(browserTable.getModel());
		registrar.unregisterService(vpChangeListener, TableViewChangedListener.class);
	}
	
	// MKTODO is this needed?
	public Collection<View<CyRow>> getSelectedRows() {
		int selectedRow = browserTable.getSelectedRow();
		if(selectedRow >= 0) {
			TableModel model = browserTable.getModel();
			if(model instanceof BrowserTableModel) {
				CyRow row = ((BrowserTableModel)model).getCyRow(selectedRow);
				View<CyRow> rowView = tableView.getRowView(row);
				if(rowView != null) {
					return Collections.singletonList(rowView);
				}
			}
		}
		return Collections.emptyList();
	}

	// MKTODO is this needed?
	public View<CyColumn> getSelectedColumn() {
		int selectedColumn = browserTable.getSelectedColumn();
		if(selectedColumn >= 0) {
			int cellColum = browserTable.convertColumnIndexToModel(selectedColumn);
			String colName = browserTable.getColumnName(cellColum);
			CyColumn column = tableView.getModel().getColumn(colName);
			return tableView.getColumnView(column);
		}
		return null;
	}
	
	@Override
	public CyTableView getViewModel() {
		return tableView;
	}
	
	@Override
	public String getRendererId() {
		return TableViewRendererImpl.ID;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return lexicon;
	}

	@Override
	public Properties getProperties() {
		return null;
	}

	@Override
	public Printable createPrintable() {
		return null;
	}

	@Override
	public Image createImage(int width, int height) {
		return null;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return VisualPropertyIconFactory.createIcon(value, width, height);
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
	}
}
