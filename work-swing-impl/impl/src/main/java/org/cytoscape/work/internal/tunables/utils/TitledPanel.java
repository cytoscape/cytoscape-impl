package org.cytoscape.work.internal.tunables.utils;

import static org.cytoscape.util.swing.LookAndFeelUtil.createTitledBorder;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class TitledPanel extends SimplePanel {

	public TitledPanel(final String title, boolean vertical) {
		super(vertical, title);
	}
	
	@Override
	protected void initComponents() {
		setBorder(createTitledBorder(title));
		
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
