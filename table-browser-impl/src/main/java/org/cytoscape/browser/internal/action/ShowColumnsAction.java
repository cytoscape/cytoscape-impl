package org.cytoscape.browser.internal.action;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.browser.internal.view.TableBrowserMediator;
import org.cytoscape.browser.internal.view.TableRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

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
public class ShowColumnsAction extends AbstractCyAction {

	private static String TITLE = "Show Columns...";
	
	private CyColumnSelector columnSelector;
	
	private final TableBrowserMediator mediator;
	private final CyServiceRegistrar serviceRegistrar;

	public ShowColumnsAction(
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
		insertSeparatorAfter = true;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var source = evt.getSource();
		
		if (source instanceof Component)
			showColumnSelectorPopup((Component) source);
	}
	
	private void showColumnSelectorPopup(Component invoker) {
		// Update column list
		var renderer = mediator.getCurrentTableRenderer();
		
		if (renderer == null)
			return;
		
		var tableView = renderer.getTableView();
		var columnViews = tableView.getColumnViews();
		var columns = new ArrayList<CyColumn>();
		var visibleColumns = new ArrayList<String>();

		for (var view : columnViews) {
			columns.add(view.getModel());

			if (Boolean.TRUE.equals(view.getVisualProperty(COLUMN_VISIBLE)))
				visibleColumns.add(view.getModel().getName());
		}

		getColumnSelector().update(columns, visibleColumns);

		// Create popup
		var popup = new JPopupMenu();
		
		// The default border (specially on Nimbus) has an ugly top/bottom gap
		popup.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		
		popup.add(getColumnSelector());
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// Update actual table
				try {
					var visibleAttributes = getColumnSelector().getSelectedColumnNames();
					
					if (renderer != null) {
						var columnViews = renderer.getTableView().getColumnViews();
						
						for (var columnView : columnViews) {
							boolean visible = visibleAttributes.contains(columnView.getModel().getName());
							TableRenderer.setColumnVisible(columnView, visible);
						}
// TODO
//						var tableBrowser = mediator.getTableBrowser(renderer);
//						
//						if (tableBrowser != null)
//							tableBrowser.updateToolBarEnableState();
					}
				} catch (Exception ex) { }
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// Ignore...
			}
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// Ignore...
			}
		});
		popup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e))
					popup.setVisible(false);
			}
		});
		
		// Show popup
		popup.pack();
		popup.show(invoker, 0, invoker.getHeight());
	}
	
	private CyColumnSelector getColumnSelector() {
		if (columnSelector == null) {
			var iconManager = serviceRegistrar.getService(IconManager.class);
			var presetationManager = serviceRegistrar.getService(CyColumnPresentationManager.class);
			columnSelector = new CyColumnSelector(iconManager, presetationManager);
		}
		
		return columnSelector;
	}
}
