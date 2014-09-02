package org.cytoscape.ding.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * From: http://www.oracle.com/technetwork/articles/javase/sorted-jlist-136883.html
 *
 * @param <E>
 */
public class SortedListModel<E> extends AbstractListModel<E> {

	private static final long serialVersionUID = 717773095606487505L;

	public enum SortOrder {
		UNORDERED, ASCENDING, DESCENDING;
	} 
	
	private final ListModel<E> unsortedModel;
	private final Comparator<E> comparator;
	private final List<SortedListEntry> sortedModel;
	private SortOrder sortOrder;

	public SortedListModel(final ListModel<E> model, final SortOrder sortOrder, final Comparator<E> comp) {
		unsortedModel = model;
		unsortedModel.addListDataListener(new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
				unsortedIntervalAdded(e);
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
				unsortedIntervalRemoved(e);
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				unsortedContentsChanged(e);
			}
		});

		this.sortOrder = sortOrder;
		this.comparator = comp != null ? comp : null;

		// Get base model info.
		int size = model.getSize();
		sortedModel = new ArrayList<SortedListEntry>(size);
		
		for (int x = 0; x < size; ++x) {
			SortedListEntry entry = new SortedListEntry(x);
			int insertionPoint = findInsertionPoint(entry);
			sortedModel.add(insertionPoint, entry);
		}
	}
	
	@Override
	public E getElementAt(final int index) throws IndexOutOfBoundsException {
		int modelIndex = toUnsortedModelIndex(index);
		E element = unsortedModel.getElementAt(modelIndex);
		
		return element;
	}
	
	@Override
	public int getSize() {
		return sortedModel.size();
	}
	
	public void setSortOrder(final SortOrder sortOrder) {
		this.sortOrder = sortOrder;
		Collections.sort((List)sortedModel);
		fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, sortedModel.size() - 1);
	}
	
	public ListModel<E> getUnsortedModel() {
		return unsortedModel;
	}
	
	/**
	 * Convert sorted-model index to an unsorted-model index.
	 * 
	 * @param index an index in the sorted model
	 * @return modelIndex an index in the unsorted model
	 */
	public int toUnsortedModelIndex(final int index) throws IndexOutOfBoundsException {
		int modelIndex = -1;
		SortedListEntry entry = sortedModel.get(index);
		modelIndex = entry.getIndex();
		
		return modelIndex;
	}
	
	/**
	 * Internal helper method to find the insertion point for a new entry in the
	 * sorted model
	 */
	private int findInsertionPoint(final SortedListEntry entry) {
		int insertionPoint = sortedModel.size();
		
		if (sortOrder != SortOrder.UNORDERED) {
			insertionPoint = Collections.binarySearch((List)sortedModel, entry);
			
			if (insertionPoint < 0)
				insertionPoint = -(insertionPoint + 1);
		}
		
		return insertionPoint;
	}
	
	private void unsortedContentsChanged(final ListDataEvent e) {
		Collections.sort((List)sortedModel);
		fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, sortedModel.size() - 1);
	}
	
	private void unsortedIntervalAdded(final ListDataEvent e) {
		int begin = e.getIndex0();
		int end = e.getIndex1();
		int nElementsAdded = end - begin + 1;

		/*
		 * Items in the decorated model have shifted position. Increment model
		 * pointers into the decorated model. Increment indices that intersect
		 * with the insertion point in the decorated model.
		 */
		for (SortedListEntry entry : sortedModel) {
			int index = entry.getIndex();
			// If the model points to a model index >= to where
			// new model entries are added, increment their index.
			if (index >= begin) {
				entry.setIndex(index + nElementsAdded);
			}
		}

		// Now add the new items from the decorated model and notify listeners.
		for (int x = begin; x <= end; ++x) {
			SortedListEntry newentry = new SortedListEntry(x);
			int insertionpoint = findInsertionPoint(newentry);
			sortedModel.add(insertionpoint, newentry);
			fireIntervalAdded(ListDataEvent.INTERVAL_ADDED, insertionpoint, insertionpoint);
		}
	}
	
	/**
	 * Update this model when items are removed from the original/decorated.
	 * Also, let listeners know that items have been removed.
	 */
	private void unsortedIntervalRemoved(final ListDataEvent e) {
		int begin = e.getIndex0();
		int end = e.getIndex1();
		int nElementsRemoved = end - begin + 1;

		/*
		 * Move from end to beginning of our sorted model, updating element
		 * indices into the decorated model or removing elements as necessary.
		 */
		int sortedSize = sortedModel.size();
		boolean[] bElementRemoved = new boolean[sortedSize];
		
		for (int x = sortedSize - 1; x >= 0; --x) {
			SortedListEntry entry = sortedModel.get(x);
			int index = entry.getIndex();
			
			if (index > end) {
				entry.setIndex(index - nElementsRemoved);
			} else if (index >= begin) {
				sortedModel.remove(x);
				bElementRemoved[x] = true;
			}
		}
		
		// Let listeners know about removed items.
		for (int x = bElementRemoved.length - 1; x >= 0; --x) {
			if (bElementRemoved[x]) {
				fireIntervalRemoved(ListDataEvent.INTERVAL_REMOVED, x, x);
			}
		}
	}
	
	private class SortedListEntry implements Comparable<SortedListEntry> {

		private int index;
		
		public SortedListEntry(final int index) {
			this.index = index;
		}

		@Override
		public int compareTo(final SortedListEntry thatEntry) {
			// Retrieve the element that this entry points to in the original model.
			final E thisElement = unsortedModel.getElementAt(index);
			// Retrieve the element that thatEntry points to in the original model.
			final E thatElement = unsortedModel.getElementAt(thatEntry.getIndex());
			
			// Compare the base model's elements using the provided comparator.
			int comparison = comparator.compare(thisElement, thatElement);
			
			// Convert to descending order as necessary.
			if (sortOrder == SortOrder.DESCENDING) {
				comparison = -comparison;
			}
			
			return comparison;
		}
		
		public int getIndex() {
			return index;
		}
		
		public void setIndex(int index) {
			this.index = index;
		}
	}
}
