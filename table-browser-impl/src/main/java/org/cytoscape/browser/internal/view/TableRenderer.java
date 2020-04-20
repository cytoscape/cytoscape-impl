package org.cytoscape.browser.internal.view;

import javax.swing.JComponent;

import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.TableRenderingEngine;

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

	@Override
	public void dispose() {
		try {
			renderingEngine.dispose();
		} finally {
			component = null;
			renderingEngine = null;
		}
	}

	public CyTable getDataTable() {
		return renderingEngine.getViewModel().getModel();
	}
}
