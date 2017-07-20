package org.cytoscape.browser.internal.task;

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

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.view.AbstractTableBrowser;
import org.cytoscape.browser.internal.view.BrowserTable;/*

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
import org.cytoscape.browser.internal.view.SetDecimalPlacesDialog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;

public class SetDecimalPlacesTask extends AbstractTableColumnTask {
	
	private CyServiceRegistrar serviceRegistrar;
	
	public SetDecimalPlacesTask(final CyColumn column, final CyServiceRegistrar serviceRegistrar) {
		super(column);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final CyTable table = column.getTable();
				final String name = column.getName();
				final BrowserTable browserTable = getBrowserTable(table, serviceRegistrar);
				final JFrame frame = (JFrame)SwingUtilities.getRoot(browserTable);
				
				// Set format
				SetDecimalPlacesDialog dialog = new SetDecimalPlacesDialog(browserTable, frame, name, serviceRegistrar);

				dialog.pack();
				dialog.setLocationRelativeTo(frame);
				dialog.setVisible(true);
				
			}
			
		});
	}
	

	public static BrowserTable getBrowserTable(final CyTable table, final CyServiceRegistrar serviceRegistrar) {
		final CySwingApplication swingAppManager = serviceRegistrar.getService(CySwingApplication.class);
		final CytoPanel cytoPanel = swingAppManager.getCytoPanel(CytoPanelName.SOUTH);
		
		if (cytoPanel != null) {
			final int count = cytoPanel.getCytoPanelComponentCount();
			
			for (int i = 0; i < count; i++) {
				final Component c = cytoPanel.getComponentAt(i);
				
				if (c instanceof AbstractTableBrowser) {
					final AbstractTableBrowser tableBrowser = (AbstractTableBrowser) c;
					final BrowserTable browserTable = tableBrowser.getBrowserTable(table);
					
					if (browserTable != null)
						return browserTable;
				}
			}
		}
		
		return null;
	}
}

class SetDecimalPlacesEdit extends AbstractCyEdit {


	public SetDecimalPlacesEdit(final String columnName) {
		super("Set Decimal Places in column \"" + columnName + "\"");
	}

	@Override
	public void undo() {
		
	}

	@Override
	public void redo() {
		
	}
}
