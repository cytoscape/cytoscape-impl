package org.cytoscape.browser.internal.view;

import javax.swing.JComponent;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.TableRenderingEngine;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

//MKTODO add more utility methods to this class
public class TableRenderer implements CyDisposable {

	private TableRenderingEngine renderingEngine;
	private JComponent component;
	
	public TableRenderer(TableRenderingEngine renderingEngine, JComponent component) {
		this.renderingEngine = renderingEngine;
		this.component = component;
	}

	public TableRenderingEngine getRenderingEngine() {
		return renderingEngine;
	}

	public JComponent getComponent() {
		return component;
	}
	
	public CyTableView getTableView() {
		return (CyTableView) renderingEngine.getViewModel();
	}

	public CyTable getDataTable() {
		return renderingEngine.getViewModel().getModel();
	}

	public void setColumnVisible(String name, boolean visible) {
		View<CyColumn> colView = getTableView().getColumnView(name);
		if(colView != null) {
			colView.setVisualProperty(BasicTableVisualLexicon.COLUMN_VISIBLE, visible);
		}
	}
	
	@Override
	public void dispose() {
		try {
			renderingEngine.dispose();
		} finally {
			component = null;
			renderingEngine = null;
		}
	}
}
