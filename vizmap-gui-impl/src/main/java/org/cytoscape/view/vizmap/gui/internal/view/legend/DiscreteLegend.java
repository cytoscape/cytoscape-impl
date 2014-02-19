package org.cytoscape.view.vizmap.gui.internal.view.legend;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

public class DiscreteLegend extends JPanel {

	private static final long serialVersionUID = -1111346616155939909L;

	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
	private static final Color TITLE_COLOR = new Color(10, 200, 255);
	private static final Border BORDER = new MatteBorder(0, 6, 3, 0, Color.DARK_GRAY);

	
	@SuppressWarnings("unchecked")
	public DiscreteLegend(DiscreteMapping<?, ?> discMapping, final ServicesUtil servicesUtil) {
		final String columnName = discMapping.getMappingColumnName();
		final VisualProperty<Object> vp = (VisualProperty<Object>) discMapping.getVisualProperty();
		setLayout(new BorderLayout());
		setBackground(Color.white);
		setBorder(BORDER);

		final JLabel title = new JLabel(" " + vp.getDisplayName() + " Mapping");
		title.setFont(TITLE_FONT);
		title.setForeground(TITLE_COLOR);
		title.setBorder(new MatteBorder(0, 10, 1, 0, TITLE_COLOR));
		// title.setHorizontalAlignment(SwingConstants.CENTER);
		// title.setVerticalAlignment(SwingConstants.CENTER);
		title.setHorizontalTextPosition(SwingConstants.LEADING);
		// title.setVerticalTextPosition(SwingConstants.CENTER);

		title.setPreferredSize(new Dimension(1, 50));
		add(title, BorderLayout.NORTH);

		final Map<?, ?> legendMap = new TreeMap<Object, Object>(discMapping.getAll());
		
		/*
		 * Build Key array.
		 */
		final Object[][] data = new Object[legendMap.keySet().size()][2];
		final Iterator<?> it = legendMap.keySet().iterator();

		for (int i = 0; i < legendMap.keySet().size(); i++) {
			Object key = it.next();
			data[i][0] = legendMap.get(key);
			data[i][1] = key;
		}

		add(LegendTable.getHeader(columnName, vp), BorderLayout.CENTER);
		add(new LegendTable(data, vp, servicesUtil), BorderLayout.SOUTH);
	}
}
