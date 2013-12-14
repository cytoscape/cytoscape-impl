package org.cytoscape.filter.internal.view;

import java.awt.dnd.DropTarget;

import javax.swing.JComponent;

import org.cytoscape.filter.internal.composite.CompositeSeparator;

public class TransformerElementViewModel<V extends SelectPanelComponent> {
	public final JComponent view;
	public final JComponent handle;
	public final V parent;
	public final JComponent separator;
	
	public TransformerElementViewModel(final JComponent view, final AbstractPanelController<?, V> controller, final V parent, IconManager iconManager) {
		this.view = view;
		this.parent  = parent;
		
		handle = new Handle<V>(iconManager, parent, controller, view);
		separator = new CompositeSeparator();
		
		new DropTarget(view, new DragHandler<V>(view, controller, parent, handle));
		new DropTarget(separator, new DragHandler<V>(separator, controller, parent, null));
		new DropTarget(handle, new DragHandler<V>(handle, controller, parent, null));
	}
}