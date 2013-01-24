package org.cytoscape.view.vizmap.gui.internal.legend;

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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.C2CMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.C2DMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.GradientEditorPanel;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;

public class ContinuousMappingLegendPanel extends JPanel {

	private static final Font TITLE_FONT2 = new Font("SansSerif", Font.BOLD, 18);
	private static final Color TITLE_COLOR = new Color(10, 200, 255);
	private static final Border BORDER = new MatteBorder(0, 6, 3, 0, Color.DARK_GRAY);

	private List points;
	private VisualProperty<?> vp;

	private JLabel legend = null;

	final VisualStyle style;
	final ContinuousMapping<?, ?> mapping;
	final CyTable table;
	final CyApplicationManager appManager;
	final VisualMappingManager vmm;
	final VisualMappingFunctionFactory continuousMappingFactory;

	public ContinuousMappingLegendPanel(final VisualStyle style, final ContinuousMapping<?, ?> mapping,
			final CyTable table, final CyApplicationManager appManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {
		super();

		this.style = style;
		this.mapping = mapping;
		this.table = table;
		this.appManager = appManager;
		this.vmm = vmm;
		this.vp = mapping.getVisualProperty();
		this.continuousMappingFactory = continuousMappingFactory;

		// this.points = points;
		// this.type = vpt;

		// Resize it when window size changed.
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				setLegend(e);
			}
		});

		setLayout(new BorderLayout());
		setBackground(Color.white);
		setBorder(BORDER);

		final JLabel title = new JLabel(" " + vp.getDisplayName() + " Mapping");
		title.setFont(TITLE_FONT2);
		title.setForeground(TITLE_COLOR);
		title.setBorder(new MatteBorder(0, 10, 1, 0, TITLE_COLOR));

		title.setHorizontalTextPosition(SwingConstants.LEADING);
		title.setPreferredSize(new Dimension(1, 50));
		add(title, BorderLayout.NORTH);

		setLegend(null);
	}

	private void setLegend(ComponentEvent e) {
		if (legend != null) {
			remove(legend);
		}

		Integer trackW = null;

		if (getParent() == null) {
			trackW = 600;
		} else {
			trackW = ((Number) (this.getParent().getParent().getParent().getWidth() * 0.82)).intValue();
			if (trackW < 200) {
				trackW = 200;
			}
		}

		if (Paint.class.isAssignableFrom(vp.getRange().getType())) {
			final GradientEditorPanel gPanel = new GradientEditorPanel(style,
					(ContinuousMapping<Double, Color>) mapping, table, appManager, null, vmm, continuousMappingFactory);
			legend = new JLabel(gPanel.getLegend(trackW, 100));
		} else if (Number.class.isAssignableFrom(vp.getRange().getType())) {
			final C2CMappingEditorPanel numberPanel = new C2CMappingEditorPanel(style,
					mapping, table, appManager, vmm, continuousMappingFactory);
			legend = new JLabel(numberPanel.getLegend(trackW, 150));
		} else {
			try {
				C2DMappingEditorPanel discretePanel = new C2DMappingEditorPanel(style, mapping, table, appManager, vmm,
						null, continuousMappingFactory);
				legend = new JLabel(discretePanel.getLegend(trackW, 150));
			} catch (Exception ex) {
				legend = new JLabel("Legend Generator not available");
			}
		}

		legend.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(legend, BorderLayout.CENTER);
		repaint();
	}

	private JPanel getGradientPanel() {
		JPanel holder = new JPanel();
		holder.setLayout(new GridLayout(1, 2));
		holder.setAlignmentX(0);
		holder.setBackground(Color.white);

		JLabel grad = new JLabel(getColorGradientIcon());
		grad.setAlignmentX(0);
		holder.add(grad);

		JLabel num = new JLabel(getNumberGradientIcon());
		num.setAlignmentX(0);
		holder.add(num);

		return holder;
	}

	int width = 40;
	int height = 40;
	int yoff = height;

	private ImageIcon getNumberGradientIcon() {
		int imageHeight = (points.size() + 1) * height;
		BufferedImage bi = new BufferedImage(width, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.white);
		g2.fillRect(0, 0, width, imageHeight);
		g2.setPaint(Color.black);

		int yoff = (int) (((float) g2.getFontMetrics().getMaxAscent()) / 2);

		ContinuousMappingPoint curr = null;

		for (int i = 0; i < points.size(); i++) {
			curr = (ContinuousMappingPoint) points.get(i);

			g2.drawString(curr.getValue().toString(), 0, ((i + 1) * height) + yoff);
		}

		return new ImageIcon(bi);
	}

	private ImageIcon getColorGradientIcon() {
		int imageHeight = (points.size() + 1) * height;
		BufferedImage bi = new BufferedImage(width, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.white);
		g2.fillRect(0, 0, width, imageHeight);

		ContinuousMappingPoint curr = null;
		ContinuousMappingPoint next = null;

		Rectangle rect = new Rectangle(0, 0, width, height);

		for (int i = 0; i < points.size(); i++) {
			curr = (ContinuousMappingPoint) points.get(i);

			if ((i + 1) < points.size())
				next = (ContinuousMappingPoint) points.get(i + 1);
			else
				next = null;

			if (i == 0) {
				g2.setPaint((Color) (curr.getRange().lesserValue));
				rect.setBounds(0, 0, width, height);
				g2.fill(rect);
			}

			if (next != null) {
				GradientPaint gp = new GradientPaint(0, ((i + 1) * height), (Color) curr.getRange().equalValue, 0,
						((i + 2) * height), (Color) next.getRange().equalValue);
				g2.setPaint(gp);
				rect.setBounds(0, ((i + 1) * height), width, height);
				g2.fill(rect);
			} else {
				g2.setPaint((Color) (curr.getRange().greaterValue));
				rect.setBounds(0, ((i + 1) * height), width, height);
				g2.fill(rect);
			}
		}

		return new ImageIcon(bi);
	}

	// private JPanel getObjectPanel(VisualProperty<?> vp) {
	// Object[][] data = new Object[points.size() + 2][2];
	//
	// ContinuousMappingPoint curr = null;
	//
	// for (int i = 0; i < points.size(); i++) {
	// curr = (ContinuousMappingPoint) points.get(i);
	//
	// if (i == 0) {
	// data[i][0] = curr.getRange().lesserValue;
	// data[i][1] = "< " + curr.getValue().toString();
	// }
	//
	// data[i + 1][0] = curr.getRange().equalValue;
	// data[i + 1][1] = "= " + curr.getValue().toString();
	//
	// if (i == (points.size() - 1)) {
	// data[i + 2][0] = curr.getRange().greaterValue;
	// data[i + 2][1] = "> " + curr.getValue().toString();
	// }
	// }
	//
	// final LegendTable lt = new LegendTable(data, vp);
	//
	// return lt;
	// }
}
