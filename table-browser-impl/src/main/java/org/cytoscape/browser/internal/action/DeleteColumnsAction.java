package org.cytoscape.browser.internal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.browser.internal.view.DeletionDialog;
import org.cytoscape.browser.internal.view.TableBrowserMediator;

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
public class DeleteColumnsAction extends AbstractCyAction {

	private static String TITLE = "Delete Columns...";
	
	private final TableBrowserMediator mediator;

	public DeleteColumnsAction(Icon icon, float toolbarGravity, TableBrowserMediator mediator) {
		super(TITLE);
		this.mediator = mediator;
		
		putValue(SHORT_DESCRIPTION, TITLE);
		putValue(LARGE_ICON_KEY, icon);
		setIsInNodeTableToolBar(true);
		setIsInEdgeTableToolBar(true);
		setIsInNetworkTableToolBar(true);
		setIsInUnassignedTableToolBar(true);
		setToolbarGravity(toolbarGravity);
		insertSeparatorAfter = true;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var source = evt.getSource();
		
		if (source instanceof Component)
			showColumnDeletionDialog((Component) source);
	}
	
	private void showColumnDeletionDialog(Component invoker) {
		var frame = (JFrame) SwingUtilities.getRoot(invoker);
		var renderer = mediator.getCurrentTableRenderer();
		
		if (renderer != null) {
			var dialog = new DeletionDialog(frame, renderer.getDataTable());
	
			dialog.pack();
			dialog.setLocationRelativeTo(invoker);
			dialog.setVisible(true);
		}
	}
	
	@Override
	public void updateEnableState() {
		var renderer = mediator.getCurrentTableRenderer();
		
		setEnabled(
				renderer != null &&
				renderer.getDataTable().getColumns().stream().anyMatch(col -> !col.isImmutable())
		);
	}
}
