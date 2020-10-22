 package org.cytoscape.view.model.internal.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewAddedEvent;
import org.cytoscape.view.model.events.TableViewAddedListener;
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.table.CyTableView;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CyTableViewImpl extends CyViewBase<CyTable> implements CyTableView, TableViewAddedListener {

	private final CyServiceRegistrar registrar;
	private final CyEventHelper eventHelper;
	
	private final String rendererId;
	private final VisualLexicon visualLexicon;
	private final Class<? extends CyIdentifiable> tableType; // may be null
	
	// MKTODO do we need to look up by view suid? because if not we can get ride of viewSuidToXXX maps.
	// MKTODO not all these maps may actually be needed
	private Map<Long,CyColumnViewImpl> dataSuidToCol = HashMap.empty();
	private Map<Long,CyColumnViewImpl> viewSuidToCol = HashMap.empty();
	private Map<Long,CyRowViewImpl>    dataSuidToRow = HashMap.empty();
	private Map<Long,CyRowViewImpl>    viewSuidToRow = HashMap.empty();
	// the RowsDeletedEvent only gives us the primary key of the row
	private Map<Object,CyRowViewImpl>  pkToRow = HashMap.empty();
	
	protected final ViewLock tableLock;
	protected final ViewLock columnLock;
	protected final ViewLock rowLock;
	
	protected final VPStore tableVPs;
	protected final VPStore columnVPs;
	protected final VPStore rowVPs;
	
	private boolean fireEvents = false;
	
	private CopyOnWriteArrayList<Runnable> disposeListeners = new CopyOnWriteArrayList<>(); 
	
	
	public CyTableViewImpl(
			CyServiceRegistrar registrar, 
			CyTable model, 
			VisualLexicon visualLexicon, 
			String rendererId, 
			Class<? extends CyIdentifiable> tableType
	) {
		super(model);
		this.registrar = registrar;
		this.eventHelper = registrar.getService(CyEventHelper.class);
		this.rendererId = rendererId;
		this.visualLexicon = visualLexicon;
		this.tableType = tableType;
		
		this.tableLock  = new ViewLock();
		this.columnLock = new ViewLock(tableLock);
		this.rowLock    = new ViewLock(tableLock);
		
		this.tableVPs  = new VPStore(CyTable.class,  visualLexicon, null);
		this.columnVPs = new VPStore(CyColumn.class, visualLexicon, null);
		this.rowVPs    = new VPStore(CyRow.class,    visualLexicon, null);
		
		registrar.registerService(this, TableViewAddedListener.class);
	}

	@Override
	public void handleEvent(TableViewAddedEvent e) {
		if(e.getTableView() == this) {
			fireEvents = true;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void addEventPayload(ViewChangeRecord<?> record) {
		if(fireEvents) {
			eventHelper.addEventPayload(this, record, TableViewChangedEvent.class);
		}
	}
	
	@Override
	public void dispose() {
		registrar.unregisterService(this, TableViewAddedListener.class);
		for(Runnable runnable : disposeListeners) {
			runnable.run();
		}
	}
	
	@Override
	public String getRendererId() {
		return rendererId;
	}
	
	@Override
	public Class<? extends CyIdentifiable> getTableType() {
		return tableType;
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
	
	public int getColumnCount() {
		return viewSuidToCol.size();
	}
	
	public View<CyColumn> addColumn(CyColumn model) {
		if(dataSuidToCol.containsKey(model.getSUID()))
			return null;
		
		CyColumnViewImpl view = new CyColumnViewImpl(this, model);
		synchronized (columnLock) {
			dataSuidToCol = dataSuidToCol.put(model.getSUID(), view);
			viewSuidToCol = viewSuidToCol.put(view.getSUID(),  view);
		}
		
		return view;
	}
	
	public View<CyColumn> removeColumn(Long dataSuid) {
		synchronized (columnLock) {
			View<CyColumn> colView = dataSuidToCol.getOrElse(dataSuid, null);
			if(colView != null) {
				dataSuidToCol = dataSuidToCol.remove(dataSuid);
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
	
	public View<CyColumn> getColumnViewByDataSuid(Long suid) {
		return dataSuidToCol.getOrElse(suid, null);
	}
	
	public View<CyColumn> getColumnViewByName(String name) {
		// can't use CyTable.getColumn(name) because the column may already have been deleted
		for(Tuple2<Long,CyColumnViewImpl> entry : dataSuidToCol) {
			CyColumnViewImpl col = entry._2();
			String colName = col.getModel().getName();
			if(colName != null && colName.equals(name)) {
				return col;
			}
		}
		return null;
	}
	
	/**
	 * This should return columns in the same order as the underlying table model. 
	 * It avoids strange errors where the columns are returned in an unexpected order.
	 */
	@Override
	public java.util.List<View<CyColumn>> getColumnViews() {
		// The asJava() method returns a collection that is unbearably slow, so we create our own collection instead.
		java.util.List<View<CyColumn>> colList = new ArrayList<>();
		for(var col : dataSuidToCol.values()) {
			colList.add(col);
		}
				
		// assume this collection is in a well defined order that we want to preserve
		Collection<CyColumn> modelCols = getModel().getColumns(); 
		
		// Sort the column views by their index in the table model
		java.util.Map<Long,Integer> indexMap = new java.util.HashMap<>();
		int i = 0;
		for(CyColumn col : modelCols) {
			indexMap.put(col.getSUID(), i++);
		}
		
		colList.sort((cv1, cv2) -> {
			var index1 = indexMap.get(cv1.getModel().getSUID());
			var index2 = indexMap.get(cv2.getModel().getSUID());
			if(index1 == null) return -1;
            if(index2 == null) return  1;
			return Integer.compare(index1, index2);
		});
		
		return colList;
	}
	
	
	@Override
	public Collection<View<CyRow>> getRowViews() {
		// The asJava() method returns a collection that is unbearably slow, so we create our own collection instead.
		java.util.List<View<CyRow>> rowList = new ArrayList<>();
		for(var row : dataSuidToRow.values()) {
			rowList.add(row);
		}
		return rowList;
	}
	
	public View<CyRow> addRow(CyRow model) {
		if(dataSuidToRow.containsKey(model.getSUID()))
			return null;
		
		CyColumn pkCol = getModel().getPrimaryKey();
		Object pkValue = model.get(pkCol.getName(), pkCol.getType());
		if(pkValue == null)
			return null;
		
		CyRowViewImpl rowView = new CyRowViewImpl(this, model);
		synchronized (rowLock) {
				dataSuidToRow = dataSuidToRow.put(model.getSUID(), rowView);
				viewSuidToRow = viewSuidToRow.put(rowView.getSUID(), rowView);
				pkToRow = pkToRow.put(pkValue, rowView);
		}
		return rowView;
	}
	
	public View<CyRow> removeRow(Object pkValue) {
		synchronized (rowLock) {
			View<CyRow> rowView = pkToRow.getOrElse(pkValue, null);
			if(rowView != null) {
				dataSuidToRow = dataSuidToRow.remove(rowView.getModel().getSUID());
				viewSuidToRow = viewSuidToRow.remove(rowView.getSUID());
				pkToRow = pkToRow.remove(pkValue);
				columnVPs.remove(rowView.getSUID());
			}
			return rowView;
		}
	}
	
	@Override
	public View<CyRow> getRowView(CyRow row) {
		return dataSuidToRow.getOrElse(row.getSUID(), null);
	}
	
	public View<CyRow> getRowViewByPk(Object pkValue) {
		return pkToRow.getOrElse(pkValue, null);
	}
	
	
	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		if(vp.shouldIgnoreDefault())
			return;
		
		var type = vp.getTargetDataType();
		if(type.equals(CyColumn.class)) {
			synchronized(columnLock) {
				columnVPs.setViewDefault(vp, defaultValue);
			}
		} else if(vp.getTargetDataType().equals(CyRow.class)) {
			synchronized(rowLock) {
				rowVPs.setViewDefault(vp, defaultValue);
			}
		} else if(vp.getTargetDataType().equals(CyTable.class)) {
			synchronized(tableLock) {
				tableVPs.setViewDefault(vp, defaultValue);
			}
		}
	}
	
	
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		addEventPayload(new ViewChangeRecord<>(this, vp, value, lockedValue));
	}

}
