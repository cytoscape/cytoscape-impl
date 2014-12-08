package org.cytoscape.work.internal.tunables.utils;

import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class TitledPanel extends SimplePanel {

	/**
	 * @param title Panel title
	 * @param axis {@link BoxLayout} axis
	 */
	public TitledPanel(final String title, int axis) {
		super(axis, title);
	}
	
	@Override
	protected void initComponents() {
		final Border border = createTitledBorder(title);
		setBorder(border);
		
		if (isAquaLAF() && !(title == null || title.trim().isEmpty()))
			addStrutToRoot(10);
		
		super.initComponents();
	}
	
	@Override
	protected void adjust(final Component c) {
		super.adjust(c);
		
		if (isAquaLAF() && c instanceof JPanel)
			((JPanel) c).setOpaque(false);
	}
}
