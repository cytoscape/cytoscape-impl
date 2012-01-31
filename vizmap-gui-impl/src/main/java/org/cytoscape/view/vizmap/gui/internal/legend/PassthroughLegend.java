package org.cytoscape.view.vizmap.gui.internal.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.jdesktop.swingx.border.DropShadowBorder;

public class PassthroughLegend extends JPanel {
	
	private static final long serialVersionUID = 5697219796514075908L;
	
	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
	private static final Color TITLE_COLOR = new Color(10, 200, 255);

	public PassthroughLegend(PassthroughMapping<?, ?> mapping) {
		super();
		setLayout(new BorderLayout());
		setBackground(Color.white);

		final VisualProperty<?> vp = mapping.getVisualProperty();
		final String columnName = mapping.getMappingColumnName();
		
		final JLabel title = new JLabel(vp.getDisplayName() + " is displayed as " + columnName);
		
		title.setFont(TITLE_FONT);
		title.setForeground(TITLE_COLOR);
		
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		title.setHorizontalTextPosition(SwingConstants.CENTER);
		title.setVerticalTextPosition(SwingConstants.CENTER);
		title.setPreferredSize(new Dimension(200, 50));
		title.setBorder(new DropShadowBorder());
		
		add(title, SwingConstants.CENTER);

	}
}
