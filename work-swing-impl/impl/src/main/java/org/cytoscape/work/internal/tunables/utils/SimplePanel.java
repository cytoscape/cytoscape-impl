package org.cytoscape.work.internal.tunables.utils;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SimplePanel extends JPanel {

	protected final boolean vertical;
	protected final String title;
	
	protected JPanel contentPane;
	
	public SimplePanel(final boolean vertical) {
		this(vertical, null);
	}
	
	protected SimplePanel(final boolean vertical, final String title) {
		this.vertical = vertical;
		this.title = title;
		init();
		initComponents();
	}
	
	protected void init() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}
	
	protected void initComponents() {
		addToRoot(getContentPane());
	}
	
	@Override
	public Component add(Component c) {
		adjust(c);
		return getContentPane().add(c);
	}

	@Override
	public void add(Component c, Object constraint) {
		adjust(c);
		getContentPane().add(c, constraint);
	}
	
	protected Component addToRoot(final Component c) {
		adjust(c);
		return super.add(c);
	}
	
	protected void addToRoot(final Component c, final Object constraint) {
		adjust(c);
		super.add(c, constraint);
	}
	
	protected void addStrutToRoot(int size) {
		addToRoot(Box.createVerticalStrut(size));
	}
	
	protected void adjust(final Component c) {
		if (c instanceof JPanel) {
			((JPanel) c).setAlignmentX(Component.CENTER_ALIGNMENT);
			
			if (!vertical)
				((JPanel) c).setAlignmentY(Component.TOP_ALIGNMENT);
		}
	}
	
	protected JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, vertical ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS));
		}
		
		return contentPane;
	}
}
