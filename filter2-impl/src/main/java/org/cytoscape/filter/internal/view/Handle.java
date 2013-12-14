package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Handle<V extends SelectPanelComponent> extends JLabel {
	JComponent sibling;
	
	public Handle(IconManager iconManager, final V parent, final AbstractPanelController<?, V> controller, JComponent sibling) {
		super(IconManager.ICON_REORDER + IconManager.ICON_REORDER);
		this.sibling = sibling;
		
		setFont(iconManager.getIconFont(13));
		setForeground(Color.lightGray);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!SwingUtilities.isRightMouseButton(e)) {
					return;
				}
				controller.setLastSelectedComponent(parent, Handle.this);
				parent.showHandleContextMenu(Handle.this);
			}
		});
	}
	
	public JComponent getSiblingView() {
		return sibling;
	}
}
