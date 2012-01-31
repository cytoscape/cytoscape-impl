package org.cytoscape.view.vizmap.gui.internal.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

public class DiscreteLegend extends JPanel {

	private static final long serialVersionUID = -1111346616155939909L;

	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
	private static final Font TITLE_FONT2 = new Font("SansSerif", Font.BOLD, 18);
	private static final Color TITLE_COLOR = new Color(10, 200, 255);
	private static final Border BORDER = new MatteBorder(0, 6, 3, 0, Color.DARK_GRAY);

	
	public DiscreteLegend(DiscreteMapping<?, ?> discMapping, final CyApplicationManager appManager) {
		super();

		final String columnName = discMapping.getMappingColumnName();
		final VisualProperty<Object> vp = (VisualProperty<Object>) discMapping.getVisualProperty();
		setLayout(new BorderLayout());
		setBackground(Color.white);
		setBorder(BORDER);

		final JLabel title = new JLabel(" " + vp.getDisplayName() + " Mapping");
		title.setFont(TITLE_FONT2);
		title.setForeground(TITLE_COLOR);
		title.setBorder(new MatteBorder(0, 10, 1, 0, TITLE_COLOR));
		// title.setHorizontalAlignment(SwingConstants.CENTER);
		// title.setVerticalAlignment(SwingConstants.CENTER);
		title.setHorizontalTextPosition(SwingConstants.LEADING);
		// title.setVerticalTextPosition(SwingConstants.CENTER);

		title.setPreferredSize(new Dimension(1, 50));
		add(title, BorderLayout.NORTH);

		final Map<?, ?> legendMap = discMapping.getAll();
		/*
		 * Build Key array.
		 */
		final Object[][] data = new Object[legendMap.keySet().size()][2];
		final Iterator it = legendMap.keySet().iterator();

		for (int i = 0; i < legendMap.keySet().size(); i++) {
			Object key = it.next();
			data[i][0] = legendMap.get(key);
			data[i][1] = key;
		}

		add(LegendTable.getHeader(columnName, vp), BorderLayout.CENTER);
		add(new LegendTable(appManager, data, vp), BorderLayout.SOUTH);
	}
}
