package org.cytoscape.internal.view;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

@SuppressWarnings("serial")
public class ExpandCollapseButton extends JButton {
	
	private static final float FONT_SIZE = 17.0f;
	private static final int WIDTH = 24;
	private static final int HEIGHT = 24;
	
	public ExpandCollapseButton(final boolean selected, final ActionListener al,
    		final CyServiceRegistrar serviceRegistrar) {
        setRequestFocusEnabled(true);
        setBorderPainted(false);
		setContentAreaFilled(false);
		setOpaque(false);
		setFocusPainted(false);
		
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
		setFont(iconManager.getIconFont(FONT_SIZE));
		
		final Dimension d = new Dimension(WIDTH, HEIGHT);
		setMinimumSize(d);
		setPreferredSize(d);
		setMaximumSize(d);
		setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		
		addActionListener(al);
		setSelected(selected);
    }
    
    @Override
    public void setSelected(final boolean b) {
    	setText(b ? IconManager.ICON_CARET_DOWN : IconManager.ICON_CARET_RIGHT);
    	super.setSelected(b);
    }
}
