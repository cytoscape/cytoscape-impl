package org.cytoscape.view.vizmap.internal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.TableViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.TableViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TableVisualMappingManagerImpl implements TableVisualMappingManager, TableViewAboutToBeDestroyedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	
	private final Map<View<CyColumn>, VisualStyle> column2VisualStyleMap;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	
	public TableVisualMappingManagerImpl(final VisualStyleFactory factory, final CyServiceRegistrar serviceRegistrar) {
		if (serviceRegistrar == null)
			throw new NullPointerException("'serviceRegistrar' cannot be null");

		this.column2VisualStyleMap = new WeakHashMap<>();
		this.serviceRegistrar = serviceRegistrar;
	}

	
	@Override
	public void handleEvent(TableViewAboutToBeDestroyedEvent e) {
		CyTableView tableView = e.getTableView();
		for(View<CyColumn> colView : tableView.getColumnViews()) {
			setVisualStyle(colView, null);
		}
	}
	
	
	/**
	 * Returns an associated Visual Style for the View Model.
	 */
	@Override
	public VisualStyle getVisualStyle(View<CyColumn> colView) {
		if (colView == null) {
			logger.warn("Attempting to get the visual style for a null column view.");
			return null;
		}

		synchronized (lock) {
			return column2VisualStyleMap.get(colView);
		}
	}

	
	@Override
	public void setVisualStyle(View<CyColumn> colView, VisualStyle vs) {
		if (colView == null)
			throw new NullPointerException("Column view is null.");

		boolean changed = false;

		synchronized (lock) {
			if (vs == null) {
				changed = column2VisualStyleMap.remove(colView) != null;
			} else {
				final VisualStyle previousStyle = column2VisualStyleMap.put(colView, vs);
				changed = !vs.equals(previousStyle);
			}
		}
		
		if (changed) {
			CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new ColumnVisualStyleSetEvent(this, vs, colView));
		}
	}

	@Override
	public Set<VisualStyle> getAllVisualStyles() {
		synchronized (lock) {
			return new HashSet<>(column2VisualStyleMap.values());
		}
	}

	@Override
	public Set<VisualLexicon> getAllVisualLexicon() {
		Set<VisualLexicon> set = new LinkedHashSet<>();
		CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		for(var renderer : appManager.getTableViewRendererSet()) {
			var factory = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			if(factory != null) {
				var lexicon = factory.getVisualLexicon();
				if(lexicon != null) {
					set.add(lexicon);
				}
			}
		}
		
		return set;
	}

}
