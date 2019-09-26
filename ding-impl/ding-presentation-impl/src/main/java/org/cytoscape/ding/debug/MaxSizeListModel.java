package org.cytoscape.ding.debug;

import java.util.LinkedList;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class MaxSizeListModel<T> extends AbstractListModel<T> {

	private final int maxSize;
	private LinkedList<T> list = new LinkedList<>();
	
	
	public MaxSizeListModel(int maxSize) {
		this.maxSize = maxSize;
	}
	
	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public T getElementAt(int index) {
		return list.get(index);
	}
	
	public void add(T item) {
		if(list.size() == maxSize) {
			list.removeFirst();
			fireIntervalRemoved(this, 0, 0);
		}
		list.addLast(item);
		int last = list.size() - 1;
		fireIntervalAdded(this, last, last);
	}

	public void clear() {
		if(list.isEmpty())
			return;
		int last = list.size() - 1;
		list.clear();
		fireIntervalRemoved(this, 0, last);
	}
}
