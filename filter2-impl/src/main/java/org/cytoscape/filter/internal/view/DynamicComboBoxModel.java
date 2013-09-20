package org.cytoscape.filter.internal.view;

import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;


public class DynamicComboBoxModel<T> extends AbstractComboBoxModel implements Iterable<T> {
	List<T> items;
	
	int selectedIndex;
	
	public DynamicComboBoxModel(List<T> items) {
		this.items = items;
		if (items.size() > 0) {
			selectedIndex = 0;
		} else {
			selectedIndex = -1;
		}
	}
	
	@Override
	public Object getElementAt(int index) {
		return items.get(index);
	}

	@Override
	public int getSize() {
		return items.size();
	}


	@Override
	public Object getSelectedItem() {
		if (selectedIndex < 0 || selectedIndex >= items.size()) {
			return null;
		}
		return items.get(selectedIndex);
	}

	@Override
	public void setSelectedItem(Object item) {
		selectedIndex = items.indexOf(item);
		notifyChanged(0, items.size() - 1);
	}
	
	public void add(T item) {
		if (selectedIndex == -1) {
			selectedIndex = 0;
		}
		
		int index = items.size();
		items.add(item);
		notifyAdded(index, index);
	}
	
	public void insert(int index, T item) {
		if (selectedIndex == -1) {
			selectedIndex = 0;
		}
		
		items.add(index, item);
		notifyAdded(index, index);
	}
	
	public void remove(T item) {
		int index = items.indexOf(item);
		items.remove(index);
		
		if (items.size() == 0) {
			selectedIndex = -1;
		}
		notifyRemoved(index, index);
	}
	
	public T remove(int index) {
		T removed = items.remove(index);

		int size = items.size();
		if (size == 0) {
			selectedIndex = -1;
		}
		
		if (size == index) {
			selectedIndex = size - 1;
		}
		
		notifyRemoved(index, index);
		return removed;
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}
	
	public int find(Matcher<T> matcher) {
		int index = 0;
		for (T item : this) {
			if (matcher.matches(item)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void select(JComboBox comboBox, int defaultIndex, Matcher<T> matcher) {
		DynamicComboBoxModel<T> model = (DynamicComboBoxModel<T>) comboBox.getModel();
		int index = model.find(matcher);
		if (index == -1) {
			index = defaultIndex;
		}
		comboBox.setSelectedIndex(index);
	}
}
