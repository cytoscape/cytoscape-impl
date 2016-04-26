package org.cytoscape.filter.internal.view;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class AbstractComboBoxModel implements ComboBoxModel {
	private List<ListDataListener> listeners;

	public AbstractComboBoxModel() {
		listeners = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void addListDataListener(ListDataListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListDataListener(ListDataListener listener) {
		listeners.removeAll(Collections.singleton(listener));
	}

	void notifyAdded(int startIndex, int endIndex) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIndex, endIndex);
		for (ListDataListener listener : listeners) {
			listener.contentsChanged(event);
		}
	}
	
	void notifyRemoved(int startIndex, int endIndex) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, startIndex, endIndex);
		for (ListDataListener listener : listeners) {
			listener.contentsChanged(event);
		}
	}
	
	public void notifyChanged(int start, int end) {
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, start, end);
		for (ListDataListener listener : listeners) {
			listener.contentsChanged(event);
		}
	}
}
