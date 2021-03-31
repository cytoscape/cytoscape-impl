package org.cytoscape.view.model.internal.table;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_GRAVITY;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_VISIBLE;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.events.TableViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.TableViewAddedEvent;
import org.cytoscape.view.model.events.TableViewDestroyedEvent;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;


public class CyTableViewManagerImpl implements CyTableViewManager, TableAboutToBeDeletedListener {

	private final CyServiceRegistrar registrar;
	
	private final Map<CyTable,CyTableView> tableViewMap;
	private final Set<CyTableView> viewsAboutToBeDestroyed;
	private final Object lock = new Object();
	
	
	public CyTableViewManagerImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.tableViewMap = new WeakHashMap<>();
		this.viewsAboutToBeDestroyed = new HashSet<>();
	}
	
	
	@Override
	public void reset() {
		synchronized (lock) {
			tableViewMap.clear();
		}
	}
	
	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		CyTable table = e.getTable();
		synchronized (lock) {
			CyTableView view = getTableView(table);
			destroyTableView(view);
		}
	}
	
	
	@Override
	public Set<CyTableView> getTableViewSet() {
		Set<CyTableView> views = new LinkedHashSet<>();
		synchronized (lock) {
			views.addAll(tableViewMap.values());
			views.removeAll(viewsAboutToBeDestroyed);
		}
		return views;
	}

	
	@Override
	public CyTableView getTableView(CyTable table) {
		synchronized (lock) {
			return tableViewMap.get(table);
		}
	}

	
	@Override
	public void setTableView(CyTableView view) {
		if(view == null)
			return;
		
		CyTable table = view.getModel();
		CyTableManager tableManager = registrar.getService(CyTableManager.class);
		
		synchronized (lock) {
			if(tableManager.getTable(table.getSUID()) == null) {
				throw new IllegalArgumentException("Table view cannot be added, because its table (" + table + ") is not registered");
			}
			
			CyTableView existingView = getTableView(table);
			if(existingView != null) {
				destroyTableView(existingView);
			}
			
			var suidCol = view.getColumnView(CyNetwork.SUID);
			var selectedCol = view.getColumnView(CyNetwork.SELECTED);
			if(suidCol != null)
				suidCol.setVisualProperty(COLUMN_VISIBLE, false);
			if(selectedCol != null)
				selectedCol.setVisualProperty(COLUMN_VISIBLE, false);
			
			boolean gravitySet = view.getColumnViews().stream()
					.anyMatch(c -> !COLUMN_GRAVITY.getDefault().equals(c.getVisualProperty(COLUMN_GRAVITY)));
			
			// If the gravities were not set before the table view is registered then initialize them here.
			if(!gravitySet) {
				if(suidCol != null)
					suidCol.setVisualProperty(COLUMN_GRAVITY, COLUMN_GRAVITY.getDefault() - 2.0);
				if(selectedCol != null)
					selectedCol.setVisualProperty(COLUMN_GRAVITY, COLUMN_GRAVITY.getDefault() - 1.0);
				
				// The CyTableView.getColumnViews() method does not maintain the same order as in the underlying model.
				// Initialize the gravities to be the same order that the columns were created, that way existing
				// app code that creates tables doesn't have the columns show up in an unexpected order.
				double grav = COLUMN_GRAVITY.getDefault();
				for(var colView : view.getColumnViews()) {
					if(colView != null && colView != suidCol && colView != selectedCol) {
						colView.setLockedValue(COLUMN_GRAVITY, grav);
						grav += 1.0;
					}
				}
			}
			
			tableViewMap.put(table, view);
		}
		
		fireEvent(new TableViewAddedEvent(this, view));
	}

	
	@Override
	public void destroyTableView(CyTableView view) {
		if(view == null) {
			return;
		}
		
		CyTable table = view.getModel();
		
		synchronized (lock) {
			if(!tableViewMap.containsKey(table))
				throw new IllegalArgumentException("table view is not recognized");
			viewsAboutToBeDestroyed.add(view);
		}
		
		// let everyone know!
		fireEvent(new TableViewAboutToBeDestroyedEvent(this, view));

		synchronized (lock) {
			// do this again within the lock to be safe
			if(!tableViewMap.containsKey(table))
				throw new IllegalArgumentException("table view is not recognized");

			tableViewMap.remove(table);
			viewsAboutToBeDestroyed.remove(view);
			view.dispose();
		}
		
		fireEvent(new TableViewDestroyedEvent(this));
		view = null;
	}

	
	private void fireEvent(final CyEvent<?> event) {
		registrar.getService(CyEventHelper.class).fireEvent(event);
	}
	
	

}
