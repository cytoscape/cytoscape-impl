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
import java.awt.Paint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.C2CMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.C2DMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.GradientEditorPanel;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

@SuppressWarnings("serial")
public class ContinuousLegendPanel extends AbstractMappingLegendPanel {

	private JLabel legend;

	private final VisualStyle style;
	private final ContinuousMapping<?, ?> mapping;
	private final CyTable table;
	
	public ContinuousLegendPanel(final VisualStyle style, final ContinuousMapping<?, ?> mapping,
			final CyTable table, final ServicesUtil servicesUtil) {
		super(mapping, servicesUtil);
		
		this.style = style;
		this.mapping = mapping;
		this.table = table;

		// Resize it when window size changed.
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setLegend(e);
			}
		});

		add(getTitleLabel(), BorderLayout.NORTH);

		setLegend(null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setLegend(ComponentEvent e) {
		if (legend != null)
			remove(legend);

		Integer trackW;

		if (getParent() == null) {
			trackW = 600;
		} else {
			trackW = ((Number) (this.getParent().getParent().getParent().getWidth() * 0.82)).intValue();
			
			if (trackW < 200)
				trackW = 200;
		}

		if (Paint.class.isAssignableFrom(visualProperty.getRange().getType())) {
			final GradientEditorPanel gPanel = new GradientEditorPanel(style,
					(ContinuousMapping<Double, Color>) mapping, table, null, servicesUtil);
			legend = new JLabel(gPanel.getLegend(trackW, 100));
		} else if (Number.class.isAssignableFrom(visualProperty.getRange().getType())) {
			final C2CMappingEditorPanel numberPanel = new C2CMappingEditorPanel(style, mapping, table, servicesUtil);
			legend = new JLabel(numberPanel.getLegend(trackW, 150));
		} else {
			try {
				C2DMappingEditorPanel discretePanel = new C2DMappingEditorPanel(style, mapping, table, null, 
						servicesUtil);
				legend = new JLabel(discretePanel.getLegend(trackW, 150));
			} catch (Exception ex) {
				legend = new JLabel("Legend Generator not available");
			}
		}

		legend.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(legend, BorderLayout.CENTER);
		repaint();
	}
}
