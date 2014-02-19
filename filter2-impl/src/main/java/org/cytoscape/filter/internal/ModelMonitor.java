package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JComponent;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.filter.internal.column.ColumnFilter;
import org.cytoscape.filter.internal.column.ColumnFilterController;
import org.cytoscape.filter.internal.column.ColumnFilterView;
import org.cytoscape.filter.internal.column.ColumnFilterView.ColumnComboBoxElement;
import org.cytoscape.filter.internal.degree.DegreeFilterController;
import org.cytoscape.filter.internal.degree.DegreeFilterView;
import org.cytoscape.filter.internal.view.RangeChooserController;
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
	int[] nodeDegreeRange;
	boolean enabled;
	
	Map<String, double[]> nodeColumnRanges;
	Map<String, double[]> edgeColumnRanges;
	List<ColumnComboBoxElement> columnNames;
	
	ReadWriteLock lock;
	private ColumnComboBoxElement defaultColumnName;
	
	Map<DegreeFilterView, DegreeFilterController> degreeViews;
	private Map<ColumnFilterView, ColumnFilterController> columnViews;
	private List<InteractivityChangedListener> interactivityChangedListeners;
	
	public ModelMonitor() {
		nodeDegreeRange = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };
		nodeColumnRanges = new HashMap<String, double[]>();
		edgeColumnRanges = new HashMap<String, double[]>();
		lock = new ReentrantReadWriteLock(true);
		
		columnNames = new ArrayList<ColumnComboBoxElement>();
		defaultColumnName = new ColumnComboBoxElement(null, "Choose column...");
		columnNames.add(defaultColumnName);
		
		interactivityChangedListeners = new CopyOnWriteArrayList<InteractivityChangedListener>();
		
		degreeViews = new WeakHashMap<DegreeFilterView, DegreeFilterController>();
		columnViews = new WeakHashMap<ColumnFilterView, ColumnFilterController>();
		
		enabled = true;
	}
	
	@Override
	public void handleEvent(RowsSetEvent event) {
		CyTable table = event.getSource();
		lock.readLock().lock();
		try {
			if (!enabled) {
				return;
			}
			
			if (network == null || table != network.getDefaultNodeTable() && table != network.getDefaultEdgeTable()) {
				return;
			}
		} finally {
			lock.readLock().unlock();
		}
		
		lock.writeLock().lock();
		try {
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
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent event) {
		CyNetwork model = event.getNetwork();

		lock.readLock().lock();
		try {
			if (model == network) {
				return;
			}
		} finally {
			lock.readLock().unlock();
		}
		
		lock.writeLock().lock();
		try {
			if (model == null) {
				network = null;
				return;
			}
			
			// We may not be the first writer, so check again.
			if (model == network) {
				return;
			}
			network = model;
			
			int totalObjects = network.getNodeCount() + network.getEdgeCount();
			boolean isInteractive = totalObjects < INTERACTIVITY_THRESHOLD;
			setInteractive(isInteractive);
			notifyInteractivityListeners(isInteractive);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void setInteractive(boolean isInteractive) {
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

	private void updateRange(double[] range, Number value) {
		if (value == null) {
			return;
		}
		double number = ((Number) value).doubleValue();
		range[0] = Math.min(range[0], number);
		range[1] = Math.max(range[1], number);
	}

	private void updateRange(double[] range, List<?> list) {
		if (list == null) {
			return;
		}
		for (Object value : list) {
			if (value == null) {
				continue;
			}
			double number = ((Number) value).doubleValue();
			range[0] = Math.min(range[0], number);
			range[1] = Math.max(range[1], number);
		}
	}

	private void updateColumnSliders() {
		for (ColumnFilterController controller : columnViews.values()) {
			RangeChooserController chooserController = controller.getRangeChooserController();
			ColumnFilter filter = controller.getFilter();
			
			Class<? extends CyIdentifiable> columnType = filter.getColumnType();
			double[] range;
			String name = filter.getColumnName();
			if (name == null) {
				continue;
			}
			CyTable table;
			if (CyNode.class.equals(columnType)) {
				range = nodeColumnRanges.get(name);
				table = network.getDefaultNodeTable();
			} else if (CyEdge.class.equals(columnType)) {
				range = edgeColumnRanges.get(name);
				table = network.getDefaultEdgeTable();
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
				chooserController.setBounds((long) range[0], (long) range[1]);
			} else {
				chooserController.setBounds(range[0], range[1]);
			}
		}
	}

	@Override
	public void handleEvent(AddedEdgesEvent event) {
		if (network != event.getSource()) {
			return;
		}
		
		lock.writeLock().lock();
		try {
			for (CyEdge edge : event.getPayloadCollection()) {
				int degree = computeDegree(edge.getSource());
				nodeDegreeRange[0] = Math.min(nodeDegreeRange[0], degree);
				nodeDegreeRange[1] = Math.max(nodeDegreeRange[1], degree);
				
				degree = computeDegree(edge.getTarget());
				nodeDegreeRange[0] = Math.min(nodeDegreeRange[0], degree);
				nodeDegreeRange[1] = Math.max(nodeDegreeRange[1], degree);
			}
			updateDegreeSliders();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void handleEvent(AboutToRemoveEdgesEvent event) {
		if (network != event.getSource()) {
			return;
		}
		
		lock.writeLock().lock();
		try {
			clearDegreeData();
			computeNodeDegreeRange();
			updateDegreeSliders();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	private void updateDegreeSliders() {
		Integer minimum = nodeDegreeRange[0];
		Integer maximum = nodeDegreeRange[1];
		for (DegreeFilterController controller : degreeViews.values()) {
			RangeChooserController chooserController = controller.getRangeChooserController();
			chooserController.setBounds(minimum, maximum);
		}
	}

	private void updateColumnNames(CyNetwork network) {
		lock.writeLock().lock();
		try {
			columnNames.clear();
			columnNames.add(defaultColumnName);
			if (network != null) {
				addFilterElements(network.getDefaultNodeTable(), CyNode.class);
				addFilterElements(network.getDefaultEdgeTable(), CyEdge.class);
				Collections.sort(columnNames);
				updateColumnViews();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void updateColumnViews() {
		for (Entry<ColumnFilterView, ColumnFilterController> entry : columnViews.entrySet()) {
			ColumnFilterController controller = entry.getValue();
			controller.synchronize(entry.getKey());
		}
	}
	
	private void addFilterElements(CyTable table, Class<? extends CyIdentifiable> type) {
		if (table == null) {
			return;
		}
		for (CyColumn column : table.getColumns()) {
			Class<?> elementType = column.getType();
			Class<?> listElementType = column.getListElementType();
			
			if (List.class.equals(elementType) && (String.class.equals(listElementType) || Number.class.isAssignableFrom(listElementType))) {
				columnNames.add(new ColumnComboBoxElement(type, column.getName()));
			} else if (String.class.equals(elementType) || Number.class.isAssignableFrom(elementType)) {
				columnNames.add(new ColumnComboBoxElement(type, column.getName()));
			}
		}
	}
	
	@Override
	public void handleEvent(ColumnCreatedEvent event) {
		lock.writeLock().lock();
		try {
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
			columnNames.add(new ColumnComboBoxElement(type, event.getColumnName()));
			Collections.sort(columnNames);
			updateColumnViews();
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent event) {
		lock.writeLock().lock();
		try {
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
				if (element.name.equals(event.getColumnName()) && type.equals(element.columnType)) {
					columnNames.remove(i);
					return;
				}
			}
			updateColumnViews();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void handleEvent(ColumnNameChangedEvent event) {
		lock.writeLock().lock();
		try {
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
				if (element.name.equals(event.getOldColumnName()) && type.equals(element.columnType)) {
					columnNames.remove(i);
					columnNames.add(new ColumnComboBoxElement(element.columnType, event.getNewColumnName()));
					break;
				}
			}
			Collections.sort(columnNames);
			updateColumnViews();
		} finally {
			lock.writeLock().unlock();
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
		nodeDegreeRange[0] = Integer.MAX_VALUE;
		nodeDegreeRange[1] = Integer.MIN_VALUE;
	}
	
	public int getMinimumDegree() {
		lock.readLock().lock();
		try {
			if (nodeDegreeRange[0] == Integer.MAX_VALUE) {
				// Release read lock so we can get the write lock
				lock.readLock().unlock();
				// Someone else could've jumped the line here so we need to
				// check later if we're the first writer.
				lock.writeLock().lock();
				try {
					// Prepare to downgrade to read lock 
					lock.readLock().lock();
					// Check that we're the first writer
					if (nodeDegreeRange[0] == Integer.MAX_VALUE) {
						computeNodeDegreeRange();
					}
				} finally {
					// Downgrade to read lock
					lock.writeLock().unlock();
				}
			}
			return nodeDegreeRange[0];
		} finally {
			lock.readLock().unlock();
		}
	}

	private void computeNodeDegreeRange() {
		if (network == null) {
			return;
		}
		
		lock.writeLock().lock();
		try {
			for (CyNode node : network.getNodeList()) {
				int degree = computeDegree(node);
				nodeDegreeRange[0] = Math.min(nodeDegreeRange[0], degree);
				nodeDegreeRange[1] = Math.max(nodeDegreeRange[1], degree);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@SuppressWarnings("unused")
	private int computeDegree(CyNode node) {
		int degree = 0;
		for (CyEdge edge : network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
			degree++;
		}
		return degree;
	}
	
	public int getMaximumDegree() {
		lock.readLock().lock();
		try {
			if (nodeDegreeRange[1] == Integer.MIN_VALUE) {
				// Release read lock so we can get the write lock
				lock.readLock().unlock();
				// Someone else could've jumped the line here so we need to
				// check later if we're the first writer.
				lock.writeLock().lock();
				try {
					// Prepare to downgrade to read lock 
					lock.readLock().lock();
					// Check that we're the first writer
					if (nodeDegreeRange[1] == Integer.MIN_VALUE) {
						computeNodeDegreeRange();
					}
				} finally {
					// Downgrade to read lock
					lock.writeLock().unlock();
				}
			}
			return nodeDegreeRange[1];
		} finally {
			lock.readLock().unlock();
		}
	}
	
	public List<ColumnComboBoxElement> getColumnComboBoxModel() {
		return columnNames;
	}

	public boolean isString(String name, Class<?> columnType) {
		lock.readLock().lock();
		try {
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
				return String.class.equals(column.getListElementType());
			}
			return String.class.equals(type);
		} finally {
			lock.readLock().unlock();
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
		lock.writeLock().lock();
		try {
			degreeViews.put(view, controller);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public void registerColumnFilterView(ColumnFilterView view, ColumnFilterController controller) {
		lock.writeLock().lock();
		try {
			columnViews.put(view, controller);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public Number[] getColumnRange(String name, Class<? extends CyIdentifiable> type) {
		lock.readLock().lock();
		try {
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
		} finally {
			lock.readLock().unlock();
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
		Class<?> type = column.getType();
		if (List.class.equals(type)) {
			Class<?> elementType = column.getListElementType();
			if (Number.class.isAssignableFrom(elementType)) {
				for (CyRow row : table.getAllRows()) {
					List<?> list = row.getList(name, elementType);
					updateRange(range, list);
				}
			}
		} else if (Number.class.isAssignableFrom(type)) {
			for (CyRow row : table.getAllRows()) {
				Object value = row.get(name, type);
				updateRange(range, (Number) value);
			}
		}
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

	public void recomputeColumnRange(String name, Class<? extends CyIdentifiable> type) {
		lock.writeLock().lock();
		try {
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
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void unregisterView(JComponent elementView) {
		degreeViews.remove(elementView);
		columnViews.remove(elementView);
	}
}
