package org.cytoscape.browser.internal.action;

import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.browser.internal.view.TableBrowserMediator;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class DeleteTableAction extends AbstractCyAction {

	private static String TITLE = "Delete Table...";
	
	private final TableBrowserMediator mediator;
	private final CyServiceRegistrar serviceRegistrar;

	public DeleteTableAction(
			Icon icon,
			float toolbarGravity,
			TableBrowserMediator mediator,
			CyServiceRegistrar serviceRegistrar
	) {
		super(TITLE);
		this.mediator = mediator;
		this.serviceRegistrar = serviceRegistrar;
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(LARGE_ICON_KEY, icon);
		setIsInNodeTableToolBar(true);
		setIsInEdgeTableToolBar(true);
		setIsInNetworkTableToolBar(true);
		setIsInUnassignedTableToolBar(true);
		setToolbarGravity(toolbarGravity);
		insertSeparatorBefore = true;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var renderer = mediator.getCurrentTableRenderer();
		
		if (renderer == null)
			return;
		
		var tableBrowser = mediator.getTableBrowser(renderer);
		var table = renderer.getDataTable();

		if (table.getMutability() == Mutability.MUTABLE) {
			var title = "Dalete Table";
			var msg = "Are you sure you want to delete this table?";
			int confirmValue = JOptionPane.showConfirmDialog(tableBrowser, msg, title, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			// if user selects yes delete the table
			if (confirmValue == JOptionPane.OK_OPTION) {
				var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
				var deleteTableTaskFactory = serviceRegistrar.getService(DeleteTableTaskFactory.class);
				
				taskMgr.execute(deleteTableTaskFactory.createTaskIterator(table));
			}
		} else if (table.getMutability() == Mutability.PERMANENTLY_IMMUTABLE) {
			var title = "Error";
			var msg = "Can not delete this table, it is PERMANENTLY_IMMUTABLE";
			JOptionPane.showMessageDialog(tableBrowser, msg, title, JOptionPane.ERROR_MESSAGE);
		} else if (table.getMutability() == Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES) {
			var title = "Error";
			var msg = "Can not delete this table, it is IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES";
			JOptionPane.showMessageDialog(tableBrowser, msg, title, JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void updateEnableState() {
		var table = mediator.getCurrentTable();
		setEnabled(table != null && table.getMutability() == Mutability.MUTABLE);
	}
}
