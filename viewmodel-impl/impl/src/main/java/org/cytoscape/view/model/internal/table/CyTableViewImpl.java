package org.cytoscape.view.model.internal.table;

import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.table.CyTableView;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CyTableViewImpl extends CyViewBase<CyTable> implements CyTableView {

	private final CyEventHelper eventHelper;
	private final String rendererId;
	private final VisualLexicon visualLexicon;
	
	private Map<Long,CyColumnViewImpl> dataSuidToCol = HashMap.empty();
	private Map<Long,CyColumnViewImpl> viewSuidToCol = HashMap.empty();
	
	protected final ViewLock tableLock;
	protected final ViewLock columnLock;
	
	protected final VPStore tableVPs;
	protected final VPStore columnVPs;
	
	private CopyOnWriteArrayList<Runnable> disposeListeners = new CopyOnWriteArrayList<>(); 
	
	
	public CyTableViewImpl(CyServiceRegistrar registrar, CyTable model, VisualLexicon visualLexicon, String rendererId) {
		super(model);
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		
		this.tableLock  = new ViewLock();
		this.columnLock = new ViewLock(tableLock);
		
		this.tableVPs  = new VPStore(CyTable.class,  visualLexicon, null);
		this.columnVPs = new VPStore(CyColumn.class, visualLexicon, null);
	}

	@Override
	public String getRendererId() {
		return rendererId;
	}

	@Override
	public View<?> getParentViewModel() {
		return this;
	}
	
	@Override
	public CyEventHelper getEventHelper() {
		return eventHelper;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return visualLexicon;
	}
	
	@Override
	public VPStore getVPStore() {
		return tableVPs;
	}

	@Override
	public ViewLock getLock() {
		return tableLock;
	}
	
	public void addDisposeListener(Runnable runnable) {
		disposeListeners.add(runnable);
	}
	
	@Override
	public void dispose() {
		for(Runnable runnable : disposeListeners) {
			runnable.run();
		}
	}
	
	public View<CyColumn> addColumn(CyColumn model) {
		if(dataSuidToCol.containsKey(model.getSUID()))
			return null;
		
		CyColumnViewImpl view = new CyColumnViewImpl(this, model);
		synchronized (columnLock) {
			dataSuidToCol = dataSuidToCol.put(model.getSUID(), view);
			viewSuidToCol = viewSuidToCol.put(view.getSUID(),  view);
		}
		
//		eventHelper.addEventPayload(this, view, AddedNodeViewsEvent.class);
		return view;
	}
	
	
	public View<CyColumn> removeColumn(CyColumn model) {
		synchronized (columnLock) {
			View<CyColumn> colView = dataSuidToCol.getOrElse(model.getSUID(), null);
			if(colView != null) {
				dataSuidToCol = dataSuidToCol.remove(model.getSUID());
				viewSuidToCol = viewSuidToCol.remove(colView.getSUID());
				columnVPs.remove(colView.getSUID());
			}
			return colView;
		}
	}
	
	
	@Override
	public View<CyColumn> getColumnView(CyColumn column) {
		return dataSuidToCol.getOrElse(column.getSUID(), null);
	}

	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		if(vp.shouldIgnoreDefault())
			return;
		
		if(vp.getTargetDataType().equals(CyColumn.class)) {
			synchronized(columnLock) {
				columnVPs.setViewDefault(vp, defaultValue);
			}
		} else if(vp.getTargetDataType().equals(CyTable.class)) {
			synchronized(tableLock) {
				tableVPs.setViewDefault(vp, defaultValue);
			}
		}
	}

	

	
	

}
