package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.filter.internal.filters.column.ColumnComboBoxElement;
import org.cytoscape.filter.internal.filters.column.ColumnFilter;
import org.cytoscape.filter.internal.filters.column.ColumnFilterController;
import org.cytoscape.filter.internal.filters.column.ColumnFilterView;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterController;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterView;
import org.cytoscape.filter.internal.filters.degree.DegreeRange;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;

public class ModelMonitor implements SetCurrentNetworkListener,
									 RowsSetListener, ColumnCreatedListener, ColumnDeletedListener, ColumnNameChangedListener,
									 AddedEdgesListener, AboutToRemoveEdgesListener {
	
	private static final int INTERACTIVITY_THRESHOLD = 100000;
	
	CyNetwork network;
	DegreeRange nodeDegreeRange;
	boolean enabled;
	
	Map<String, double[]> nodeColumnRanges;
	Map<String, double[]> edgeColumnRanges;
	List<ColumnComboBoxElement> columnNames;
	
	private final Object lock = new Object();
	private ColumnComboBoxElement defaultColumnName;
	
	private Map<DegreeFilterView, DegreeFilterController> degreeViews;
	private Map<ColumnFilterView, ColumnFilterController> columnViews;
	
	private List<InteractivityChangedListener> interactivityChangedListeners;
	private Map<CyNetwork,Boolean> interactivityState;
	
	public ModelMonitor() {
		nodeDegreeRange = new DegreeRange();
		nodeColumnRanges = new HashMap<>();
		edgeColumnRanges = new HashMap<>();
		
		columnNames = new ArrayList<ColumnComboBoxElement>();
		defaultColumnName = new ColumnComboBoxElement("Choose column...");
		columnNames.add(defaultColumnName);
		
		interactivityChangedListeners = new CopyOnWriteArrayList<>();
		interactivityState = new WeakHashMap<>();
		
		degreeViews = new WeakHashMap<>();
		columnViews = new WeakHashMap<>();
		
		enabled = true;
	}
	
	@Override
	public void handleEvent(RowsSetEvent event) {
		CyTable table = event.getSource();
		synchronized (lock) {
			if (!enabled) {
				return;
			}
			
			if (network == null || table != network.getDefaultNodeTable() && table != network.getDefaultEdgeTable()) {
				return;
			}

			Map<String, double[]> ranges;
			// We may not be the first writer, so check again.
			if (table == network.getDefaultNodeTable()) { 
				ranges = nodeColumnRanges;
			} else if (table == network.getDefaultEdgeTable()) {
				ranges = edgeColumnRanges;
			} else {
				return;
			}
			
			boolean changed = false;
			for (RowSetRecord record : event.getPayloadCollection()) {
				String name = record.getColumn();
				double[] range = ranges.get(name);
				if (range == null) {
					continue;
				}
				Object value = record.getValue();
				if (value == null) {
					continue;
				}
				CyColumn column = table.getColumn(name);
				Class<?> type = column.getType();
				if (List.class.equals(type) && Number.class.isAssignableFrom(column.getListElementType())) {
					updateRange(range, (List<?>) value);
				} else if (Number.class.isAssignableFrom(type)) {
					updateRange(range, (Number) value);
				}
				changed = true;
			}
			if (changed) {
				updateColumnSliders();
			}
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent event) {
		CyNetwork model = event.getNetwork();

		synchronized (lock) {
			if (model == network) {
				return;
			}

			if (model == null) {
				network = null;
				return;
			}
			
			// We may not be the first writer, so check again.
			if (model == network) {
				return;
			}
			network = model;
			
			
			boolean isInteractive;
			if(interactivityState.containsKey(network)) {
				isInteractive = interactivityState.get(network);
			}
			else {
				int totalObjects = network.getNodeCount() + network.getEdgeCount();
				isInteractive = totalObjects < INTERACTIVITY_THRESHOLD;
			}
			
			setInteractive(isInteractive);
			notifyInteractivityListeners(isInteractive);
		}
	}
	
	public void setInteractive(boolean isInteractive) {
		interactivityState.put(network, isInteractive);
		clearNumericData();
		updateColumnNames(network);
		if (isInteractive) {
			computeNodeDegreeRange();
			updateDegreeSliders();
			updateColumnSliders();
		}
	}
	
	private void notifyInteractivityListeners(boolean isInteractive) {
		for (InteractivityChangedListener listener : interactivityChangedListeners) {
			listener.handleInteractivityChanged(isInteractive);
		}
	}

	private boolean updateRange(double[] range, Number value) {
		if (value == null) {
			return false;
		}
		double number = ((Number) value).doubleValue();
		if(!Double.isFinite(number)) // ignore NaN and infinity
			return false;
		
		range[0] = Math.min(range[0], number);
		range[1] = Math.max(range[1], number);
		return true;
	}

	private boolean updateRange(double[] range, List<?> list) {
		if (list == null) {
			return false;
		}
		boolean changed = false;
		for (Object value : list) {
			if (value == null) {
				continue;
			}
			double number = ((Number) value).doubleValue();
			if(Double.isFinite(number)) {
				range[0] = Math.min(range[0], number);
				range[1] = Math.max(range[1], number);
				changed = true;
			}
		}
		return changed;
	}

	private void updateColumnSliders() {
		for (ColumnFilterController controller : columnViews.values()) {
			ColumnFilter filter = controller.getFilter();
			
			Class<? extends CyIdentifiable> columnType = filter.getColumnType();
			Number[] range;
			String name = filter.getColumnName();
			if (name == null) {
				continue;
			}
			CyTable table;
			if (CyNode.class.equals(columnType)) {
				table = network.getDefaultNodeTable();
				range = getColumnRange(table, name, nodeColumnRanges);
			} else if (CyEdge.class.equals(columnType)) {
				table = network.getDefaultEdgeTable();
				range = getColumnRange(table, name, edgeColumnRanges);
			} else {
				continue;
			}
			
			CyColumn column = table.getColumn(name);
			if (column == null) {
				continue;
			}
			Class<?> type = column.getType();
			if (List.class.equals(type)) {
				type = column.getListElementType();
			}
			if (Integer.class.equals(type) || Long.class.equals(type)) {
				controller.setSliderBounds(range[0].longValue(), range[1].longValue());
			} else {
				controller.setSliderBounds(range[0].doubleValue(), range[1].doubleValue());
			}
		}
	}

	@Override
	public void handleEvent(AddedEdgesEvent event) {
		if (network != event.getSource()) {
			return;
		}
		
		synchronized (lock) {
			for (CyEdge edge : event.getPayloadCollection()) {
				nodeDegreeRange.update(network, edge);
			}
			updateDegreeSliders();
		}
	}
	
	@Override
	public void handleEvent(AboutToRemoveEdgesEvent event) {
		if (network != event.getSource()) {
			return;
		}
		
		synchronized (lock) {
			clearDegreeData();
			computeNodeDegreeRange();
			updateDegreeSliders();
		}
	}
	
	private void updateDegreeSliders() {
		for (DegreeFilterController controller : degreeViews.values()) {
			controller.setDegreeBounds(nodeDegreeRange);
		}
	}

	private void updateColumnNames(CyNetwork network) {
		synchronized (lock) {
			columnNames.clear();
			columnNames.add(defaultColumnName);
			if (network != null) {
				addFilterElements(network.getDefaultNodeTable(), CyNode.class);
				addFilterElements(network.getDefaultEdgeTable(), CyEdge.class);
				Collections.sort(columnNames);
				updateColumnViews();
			}
		}
	}

	private void updateColumnViews() {
		for (Entry<ColumnFilterView, ColumnFilterController> entry : columnViews.entrySet()) {
			ColumnFilterController controller = entry.getValue();
			controller.columnsChanged(entry.getKey());
		}
	}
	
	private void addFilterElements(CyTable table, Class<? extends CyIdentifiable> type) {
		if (table == null) {
			return;
		}
		for (CyColumn column : table.getColumns()) {
			Class<?> elementType = column.getType();
			Class<?> listElementType = column.getListElementType();
			
			if (List.class.equals(elementType) && (String.class.equals(listElementType) || Number.class.isAssignableFrom(listElementType) || Boolean.class.equals(listElementType))) {
				columnNames.add(new ColumnComboBoxElement(type, column));
			} else if (String.class.equals(elementType) || Number.class.isAssignableFrom(elementType) || Boolean.class.equals(elementType)) {
				columnNames.add(new ColumnComboBoxElement(type, column));
			}
		}
	}
	
	@Override
	public void handleEvent(ColumnCreatedEvent event) {
		synchronized (lock) {
			if (network == null) {
				return;
			}
			
			CyTable nodeTable = network.getDefaultNodeTable();
			CyTable edgeTable = network.getDefaultEdgeTable();
			CyTable table = event.getSource();
			
			Class<?> type;
			if (table == nodeTable) {
				type = CyNode.class;
			} else if (table == edgeTable) {
				type = CyEdge.class;
			} else {
				return;
			}
			
			CyColumn column = table.getColumn(event.getColumnName());
			
			columnNames.add(new ColumnComboBoxElement(type, column));
			Collections.sort(columnNames);
			updateColumnViews();
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent event) {
		synchronized (lock) {
			if (network == null) {
				return;
			}
			
			CyTable nodeTable = network.getDefaultNodeTable();
			CyTable edgeTable = network.getDefaultEdgeTable();
			CyTable table = event.getSource();
			
			Class<?> type;
			if (table == nodeTable) {
				type = CyNode.class;
			} else if (table == edgeTable) {
				type = CyEdge.class;
			} else {
				return;
			}
			for (int i = 0; i < columnNames.size(); i++) {
				ColumnComboBoxElement element = columnNames.get(i);
				if (element.getName().equals(event.getColumnName()) && type.equals(element.getTableType())) {
					columnNames.remove(i);
					break;
				}
			}
			updateColumnViews();
		}
	}
	
	@Override
	public void handleEvent(ColumnNameChangedEvent event) {
		synchronized (lock) {
			if (network == null) {
				return;
			}
			
			CyTable nodeTable = network.getDefaultNodeTable();
			CyTable edgeTable = network.getDefaultEdgeTable();
			CyTable table = event.getSource();
			CyColumn column = table.getColumn(event.getNewColumnName());
			
			Class<?> type;
			if (table == nodeTable) {
				type = CyNode.class;
			} else if (table == edgeTable) {
				type = CyEdge.class;
			} else {
				return;
			}
			for (int i = 0; i < columnNames.size(); i++) {
				ColumnComboBoxElement element = columnNames.get(i);
				if (element.getName().equals(event.getOldColumnName()) && type.equals(element.getTableType())) {
					columnNames.remove(i);
					columnNames.add(new ColumnComboBoxElement(element.getTableType(), column));
					break;
				}
			}
			Collections.sort(columnNames);
			updateColumnViews();
		}
	}
	
	private void clearNumericData() {
		// Assume the caller has write lock.
		clearDegreeData();
		nodeColumnRanges.clear();
		edgeColumnRanges.clear();
	}

	private void clearDegreeData() {
		// Assume the caller has write lock.
		nodeDegreeRange = new DegreeRange();
	}
	
	public DegreeRange getDegreeRange() {
		synchronized (lock) {
			if (!nodeDegreeRange.isUpdated()) {
				computeNodeDegreeRange();
			}
			return nodeDegreeRange;
		}
	}

	private void computeNodeDegreeRange() {
		if (network == null) {
			return;
		}
		
		synchronized (lock) {
			nodeDegreeRange.update(network);
		}
	}

	public List<ColumnComboBoxElement> getColumnComboBoxModel() {
		return columnNames;
	}

	public boolean checkType(String name, Class<?> columnType, Class<?> targetType) {
		synchronized (lock) {
			if (network == null) {
				return false;
			}
			CyTable table = getTable(columnType);
			if (table == null) {
				return false;
			}
			CyColumn column = table.getColumn(name);
			if (column == null) {
				return false;
			}
			Class<?> type = column.getType();
			if (List.class.equals(type)) {
				return targetType.equals(column.getListElementType());
			}
			return targetType.equals(type);
		}
	}

	private CyTable getTable(Class<?> columnType) {
		if (CyNode.class.equals(columnType)) {
			return network.getDefaultNodeTable();
		}
		if (CyEdge.class.equals(columnType)) {
			return network.getDefaultEdgeTable();
		}
		return null;
	}
	
	public void registerDegreeFilterView(DegreeFilterView view, DegreeFilterController controller) {
		synchronized (lock) {
			degreeViews.put(view, controller);
		}
	}
	
	public void registerColumnFilterView(ColumnFilterView view, ColumnFilterController controller) {
		synchronized (lock) {
			columnViews.put(view, controller);
		}
	}
	
	public Number[] getColumnRange(String name, Class<? extends CyIdentifiable> type) {
		synchronized (lock) {
			if (network == null) {
				return null;
			}
			
			if (CyNode.class.equals(type)) {
				CyTable table = network.getDefaultNodeTable();
				return getColumnRange(table, name, nodeColumnRanges);
			} else {
				CyTable table = network.getDefaultEdgeTable();
				return getColumnRange(table, name, edgeColumnRanges);
			}
		}
	}

	private Number[] getColumnRange(CyTable table, String name, Map<String, double[]> ranges) {
		double[] range = ranges.get(name);
		CyColumn column = table.getColumn(name);
		if (range == null) {
			range = computeRange(table, name, column);
			ranges.put(name, range);
		}
		
		Class<?> type = column.getType();
		if (List.class.equals(type)) {
			type = column.getListElementType();
		}
		if (Integer.class.equals(type) || Long.class.equals(type)) {
			return new Number[] { (long) range[0], (long) range[1] };
		}
		return new Number[] { range[0], range[1] };
	}
	
	private double[] computeRange(CyTable table, String name, CyColumn column) {
		double[] range = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
		boolean changed = false;
		Class<?> type = column.getType();
		if (List.class.equals(type)) {
			Class<?> elementType = column.getListElementType();
			if (Number.class.isAssignableFrom(elementType)) {
				for (CyRow row : table.getAllRows()) {
					List<?> list = row.getList(name, elementType);
					changed |= updateRange(range, list);
				}
			}
		} else if (Number.class.isAssignableFrom(type)) {
			for (CyRow row : table.getAllRows()) {
				Object value = row.get(name, type);
				changed |= updateRange(range, (Number) value);
			}
		}
		
		if(!changed)
			return new double[] { 0.0, 0.0 };
		return range;
	}

	public void addInteractivityChangedListener(InteractivityChangedListener listener) {
		if (interactivityChangedListeners.contains(listener)) {
			return;
		}
		
		interactivityChangedListeners.add(listener);
	}
	
	public void removeInteractivityChangedListener(InteractivityChangedListener listener) {
		interactivityChangedListeners.remove(listener);
	}

	public void recomputeColumnRange(String name, Class<?> type) {
		synchronized (lock) {
			if (network == null) {
				return;
			}
			Map<String, double[]> ranges;
			if (type.equals(CyNode.class)) {
				ranges = nodeColumnRanges;
			} else {
				ranges = edgeColumnRanges;
			}
			ranges.remove(name);
		}
	}

	public void unregisterView(JComponent elementView) {
		degreeViews.remove(elementView);
		columnViews.remove(elementView);
	}
}
