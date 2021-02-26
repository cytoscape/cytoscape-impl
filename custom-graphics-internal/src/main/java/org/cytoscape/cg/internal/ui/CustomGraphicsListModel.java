package org.cytoscape.cg.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;

import org.cytoscape.cg.model.CGComparator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Extend the default list model by insuring that the items in the list
 * are sorted by type, then by displayName....
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CustomGraphicsListModel extends AbstractListModel<CyCustomGraphics> {
	
	private List<CyCustomGraphics> graphics;
	private Comparator<CyCustomGraphics> comp;

	public CustomGraphicsListModel() {
		graphics = new ArrayList<>();
		comp = new CGComparator();
	}

	public void addElement(CyCustomGraphics cg) {
		graphics.add(cg);
		Collections.sort(graphics, comp);
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

	@Override
	public int getSize() {
		return graphics.size();
	}

	@Override
	public CyCustomGraphics getElementAt(int index) {
		return graphics.get(index);
	}
}
