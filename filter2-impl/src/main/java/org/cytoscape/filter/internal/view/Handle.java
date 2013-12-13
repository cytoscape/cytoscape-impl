package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Handle<V extends SelectPanelComponent> extends JLabel {

	public Handle(IconManager iconManager, final V view, final AbstractPanelController<?, V> controller) {
		super(IconManager.ICON_REORDER + IconManager.ICON_REORDER);
		setFont(iconManager.getIconFont(13));
		setForeground(Color.lightGray);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) {
					return;
				}
				controller.setLastSelectedComponent(view, Handle.this);
				view.showHandleContextMenu(Handle.this);
			}
		});
	}
}
