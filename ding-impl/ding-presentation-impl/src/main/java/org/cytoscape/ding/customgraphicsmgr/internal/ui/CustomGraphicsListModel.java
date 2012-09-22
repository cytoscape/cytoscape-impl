package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import javax.swing.AbstractListModel;

import org.cytoscape.ding.customgraphicsmgr.internal.CGComparator;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Extend the default list model by insuring that the items in the list
 * are sorted by type, then by displayName....
 * 
 */
public class CustomGraphicsListModel extends AbstractListModel {
	private List<String> classList;
	private List<CyCustomGraphics> graphics;
	private Comparator comp;

	public CustomGraphicsListModel() {
		super();
		graphics = new ArrayList<CyCustomGraphics>();
		comp = new CGComparator();
	}

	public void addElement(CyCustomGraphics cg) {
		graphics.add(cg);
		Collections.sort(graphics, comp);
		// printList();
		fireContentsChanged(this, 0, graphics.size());
	}

	public void removeElement(CyCustomGraphics cg) {
		graphics.remove(cg);
		fireContentsChanged(this, 0, graphics.size());
	}

	public void removeAllElements() {
		graphics.clear();
		fireContentsChanged(this, 0, graphics.size());
		
	}

	public void clear() {
		graphics.clear();
		fireContentsChanged(this, 0, graphics.size());
	}

	public int getSize() {
		return graphics.size();
	}

	public Object getElementAt(int index) {
		return graphics.get(index);
	}

	private void printList() {
		for (CyCustomGraphics cg: graphics) {
			System.out.println(cg.getClass().getCanonicalName()+": "+cg.getDisplayName());
		}
	}
}
