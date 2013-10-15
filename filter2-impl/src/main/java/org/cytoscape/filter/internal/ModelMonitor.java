package org.cytoscape.filter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.filter.internal.attribute.AttributeFilter;
import org.cytoscape.filter.internal.attribute.AttributeFilterController;
import org.cytoscape.filter.internal.attribute.AttributeFilterView;
import org.cytoscape.filter.internal.attribute.AttributeFilterView.AttributeComboBoxElement;
import org.cytoscape.filter.internal.degree.DegreeFilterController;
import org.cytoscape.filter.internal.degree.DegreeFilterView;
import org.cytoscape.filter.internal.prefuse.NumberRangeModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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
									 AddedEdgesListener {
	CyNetwork network;
	int[] nodeDegreeRange;
	boolean enabled;
	
	Map<String, double[]> nodeAttributeRanges;
	Map<String, double[]> edgeAttributeRanges;
	List<AttributeComboBoxElement> attributeNames;
	
	ReadWriteLock lock;
	private AttributeComboBoxElement defaultAttributeName;
	
	Map<DegreeFilterView, DegreeFilterController> degreeViews;
	private Map<AttributeFilterView, AttributeFilterController> attributeViews;
	
	public ModelMonitor() {
		nodeDegreeRange = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };
		nodeAttributeRanges = new HashMap<String, double[]>();
		edgeAttributeRanges = new HashMap<String, double[]>();
		lock = new ReentrantReadWriteLock(true);
		
		attributeNames = new ArrayList<AttributeComboBoxElement>();
		defaultAttributeName = new AttributeComboBoxElement(null, "Choose attribute...");
		attributeNames.add(defaultAttributeName);
		
		degreeViews = new WeakHashMap<DegreeFilterView, DegreeFilterController>();
		attributeViews = new WeakHashMap<AttributeFilterView, AttributeFilterController>();
		
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
				ranges = nodeAttributeRanges;
			} else if (table == network.getDefaultEdgeTable()) {
				ranges = edgeAttributeRanges;
			} else {
				return;
			}
			
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
			}
			updateAttributeSliders();
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
			// We may not be the first writer, so check again.
			if (model == network) {
				return;
			}
			network = model;
			clearNumericData();
			computeAttributeRanges();
			computeNodeDegreeRange();
			updateDegreeSliders();
			updateAttributeNames(network);
			updateAttributeSliders();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	private void computeAttributeRanges() {
		lock.writeLock().lock();
		try {
			if (network == null) return;
			computeAttributeRanges(network.getDefaultNodeTable(), nodeAttributeRanges);
			computeAttributeRanges(network.getDefaultEdgeTable(), edgeAttributeRanges);
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void computeAttributeRanges(CyTable table, Map<String, double[]> ranges) {
		for (CyColumn column : table.getColumns()) {
			String name = column.getName();
			Class<?> type = column.getType();
			double[] range = null;
			
			if (List.class.equals(type)) {
				Class<?> elementType = column.getListElementType();
				if (Number.class.isAssignableFrom(elementType)) {
					range = ranges.get(name);
					if (range == null) {
						range = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
						ranges.put(name, range);
					}
					for (CyRow row : table.getAllRows()) {
						List<?> list = row.getList(name, elementType);
						updateRange(range, list);
					}
				}
			} else if (Number.class.isAssignableFrom(type)) {
				range = ranges.get(name);
				if (range == null) {
					range = new double[] { Double.MAX_VALUE, Double.MIN_VALUE };
					ranges.put(name, range);
				}
				for (CyRow row : table.getAllRows()) {
					Object value = row.get(name, type);
					updateRange(range, (Number) value);
				}
			}
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

	private void updateAttributeSliders() {
		for (AttributeFilterController controller : attributeViews.values()) {
			NumberRangeModel model = controller.getSliderModel();
			AttributeFilter filter = controller.getFilter();
			
			Class<? extends CyIdentifiable> attributeType = filter.getAttributeType();
			double[] range;
			CyTable table;
			if (CyNode.class.equals(attributeType)) {
				range = nodeAttributeRanges.get(filter.getAttributeName());
				table = network.getDefaultNodeTable();
			} else if (CyEdge.class.equals(attributeType)) {
				range = edgeAttributeRanges.get(filter.getAttributeName());
				table = network.getDefaultEdgeTable();
			} else {
				continue;
			}
			
			CyColumn column = table.getColumn(filter.getAttributeName());
			if (column == null) {
				continue;
			}
			Class<?> type = column.getType();
			if (List.class.equals(type)) {
				type = column.getListElementType();
			}
			if (Integer.class.equals(type) || Long.class.equals(type)) {
				model.setMinValue((int) range[0]);
				model.setMaxValue((int) range[1]);
			} else {
				model.setMinValue(range[0]);
				model.setMaxValue(range[1]);
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
	
	private void updateDegreeSliders() {
		Integer minimum = nodeDegreeRange[0];
		Integer maximum = nodeDegreeRange[1];
		for (DegreeFilterController controller : degreeViews.values()) {
			NumberRangeModel model = controller.getSliderModel();
			model.setMinValue(minimum);
			model.setMaxValue(maximum);
		}
	}

	private void updateAttributeNames(CyNetwork network) {
		lock.writeLock().lock();
		try {
			attributeNames.clear();
			attributeNames.add(defaultAttributeName);
			if (network != null) {
				addFilterElements(network.getDefaultNodeTable(), CyNode.class);
				addFilterElements(network.getDefaultEdgeTable(), CyEdge.class);
				Collections.sort(attributeNames);
				
				for (Entry<AttributeFilterView, AttributeFilterController> entry : attributeViews.entrySet()) {
					AttributeFilterController controller = entry.getValue();
					controller.synchronize(entry.getKey());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private void addFilterElements(CyTable table, Class<? extends CyIdentifiable> type) {
		for (CyColumn column : table.getColumns()) {
			Class<?> elementType = column.getType();
			Class<?> listElementType = column.getListElementType();
			
			if (List.class.equals(elementType) && (String.class.equals(listElementType) || Number.class.isAssignableFrom(listElementType))) {
				attributeNames.add(new AttributeComboBoxElement(type, column.getName()));
			} else if (String.class.equals(elementType) || Number.class.isAssignableFrom(elementType)) {
				attributeNames.add(new AttributeComboBoxElement(type, column.getName()));
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
			attributeNames.add(new AttributeComboBoxElement(type, event.getColumnName()));
			Collections.sort(attributeNames);
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
			for (int i = 0; i < attributeNames.size(); i++) {
				AttributeComboBoxElement element = attributeNames.get(i);
				if (element.name.equals(event.getColumnName()) && type.equals(element.attributeType)) {
					attributeNames.remove(i);
					return;
				}
			}
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
			for (int i = 0; i < attributeNames.size(); i++) {
				AttributeComboBoxElement element = attributeNames.get(i);
				if (element.name.equals(event.getOldColumnName()) && type.equals(element.attributeType)) {
					attributeNames.remove(i);
					attributeNames.add(new AttributeComboBoxElement(element.attributeType, event.getNewColumnName()));
					return;
				}
			}
			Collections.sort(attributeNames);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	private void clearNumericData() {
		lock.writeLock().lock();
		try {
			nodeDegreeRange[0] = Integer.MAX_VALUE;
			nodeDegreeRange[1] = Integer.MIN_VALUE;
		} finally {
			lock.writeLock().unlock();
		}
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
	
	boolean getEnabled() {
		return enabled;
	}
	
	void setEnabled(boolean enabled) {
		lock.writeLock().lock();
		try {
			this.enabled = enabled;
			if (!enabled) {
				clearNumericData();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	public List<AttributeComboBoxElement> getAttributeComboBoxModel() {
		return attributeNames;
	}

	public boolean isString(String name, Class<?> attributeType) {
		lock.readLock().lock();
		try {
			if (network == null) {
				return false;
			}
			CyTable table = getTable(attributeType);
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

	private CyTable getTable(Class<?> attributeType) {
		if (CyNode.class.equals(attributeType)) {
			return network.getDefaultNodeTable();
		}
		if (CyEdge.class.equals(attributeType)) {
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
	
	public void registerAttributeFilterView(AttributeFilterView view, AttributeFilterController controller) {
		lock.writeLock().lock();
		try {
			attributeViews.put(view, controller);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public Number[] getAttributeRange(String name, Class<? extends CyIdentifiable> type) {
		lock.readLock().lock();
		try {
			if (network == null) {
				return null;
			}
			
			if (CyNode.class.equals(type)) {
				CyColumn column = network.getDefaultNodeTable().getColumn(name);
				return getAttributeRange(column, nodeAttributeRanges);
			} else {
				CyColumn column = network.getDefaultEdgeTable().getColumn(name);
				return getAttributeRange(column, edgeAttributeRanges);
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	private Number[] getAttributeRange(CyColumn column, Map<String, double[]> ranges) {
		double[] range = ranges.get(column.getName());
		Class<?> type = column.getType();
		if (List.class.equals(type)) {
			type = column.getListElementType();
		}
		if (Integer.class.equals(type) || Long.class.equals(type)) {
			return new Number[] { (long) range[0], (long) range[1] };
		}
		return new Number[] { range[0], range[1] };
	}
}
