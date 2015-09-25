package org.cytoscape.filter.internal.view;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.cytoscape.filter.internal.filters.composite.CompositeSeparator;
import org.cytoscape.util.swing.IconManager;

public class TransformerElementViewModel<V extends SelectPanelComponent> {
	
	public final JComponent view;
	public final JComponent handle;
	public final JComponent deleteButton;
	public final V parent;
	public final JComponent separator;
	
	public TransformerElementViewModel(final JComponent view, final AbstractPanelController<?, V> controller, final V parent) {
		this.view = view;
		this.parent  = parent;
		
		handle = new Handle<V>(controller.getIconManager(), parent, controller, view);
		separator = new CompositeSeparator();
		deleteButton = createDeleteButton(controller.getIconManager(), controller);

		new DropTarget(view, new DragHandler<V>(view, controller, parent, handle));
		new DropTarget(separator, new DragHandler<V>(separator, controller, parent, null));
		new DropTarget(handle, new DragHandler<V>(handle, controller, parent, null));
	}

	private JComponent createDeleteButton(IconManager iconManager, final AbstractPanelController<?, V> controller) {
		JButton button = new JButton(IconManager.ICON_REMOVE);
		button.setFont(iconManager.getIconFont(15));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleDelete(parent, view);
			}
		});
		return button;
	}
}