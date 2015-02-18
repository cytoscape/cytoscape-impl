package org.cytoscape.filter.internal.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class Handle<V extends SelectPanelComponent> extends JLabel {
	JComponent sibling;
	
	public Handle(IconManager iconManager, final V parent, final AbstractPanelController<?, V> controller, JComponent sibling) {
		super(IconManager.ICON_REORDER + IconManager.ICON_REORDER);
		this.sibling = sibling;
		
		setToolTipText(controller.getHandleToolTip());
		setFont(iconManager.getIconFont(14));
		setForeground(Color.lightGray);
		setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
	}
	
	public JComponent getSiblingView() {
		return sibling;
	}
}
