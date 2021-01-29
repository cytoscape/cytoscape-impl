package org.cytoscape.view.table.internal.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.cg.model.SVGLayer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/** Cell renderer for attribute browser table. */
@SuppressWarnings("serial")
class BrowserTableCellRenderer extends JPanel implements TableCellRenderer {

	// Define fonts & colors for the cells
	private static final int H_PAD = 8;
	private static final int V_PAD = 2;
	private static EquationIcon EQUATION_ICON = new EquationIcon();
	
	private final JLabel label;
	
	private CyColumn col;
	private CyRow row;
	private CyCustomGraphics<?> cg;
	private CyTableView tableView;
	private CyColumnView columnView;
	
	private final DefaultCellRenderer defCellRenderer = new DefaultCellRenderer();
	private final BrowserTablePresentation presentation;
	
	public BrowserTableCellRenderer(CyServiceRegistrar serviceRegistrar) {
		presentation = new BrowserTablePresentation(serviceRegistrar, defCellRenderer.getFont());
		
		label = new JLabel();
		label.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
		
		// Add padding:
		var border = defCellRenderer.getBorder();
		
		if (border == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));
		
		setBorder(border);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int colIndex) {
		if (colIndex < 0 || colIndex >= table.getColumnCount())
			return this;
		
		var objEditStr = (ValidatedObjectAndEditString) value;
		var validatedObj = objEditStr != null ? objEditStr.getValidatedObject() : null;
		
		var browserTable = (BrowserTable) table;
		var model = (BrowserTableModel) browserTable.getModel();
		tableView = model.getTableView();
		col = model.getColumnByModelIndex(browserTable.convertColumnIndexToModel(colIndex));
		row = model.getCyRow(browserTable.convertRowIndexToModel(rowIndex));
		
		columnView = (CyColumnView) tableView.getColumnView(col);
		
		var bg = presentation.getBackgroundColor(row, columnView);
		var fg = presentation.getForegroundColor(row, columnView);
		var font = presentation.getFont(row, columnView, validatedObj);
	
		label.setFont(font);
		label.setIcon(objEditStr != null && objEditStr.isEquation() ? EQUATION_ICON : null);
		label.setVerticalTextPosition(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);

		if (validatedObj instanceof Boolean)
			label.setHorizontalAlignment(JLabel.CENTER);
		else
			label.setHorizontalAlignment(validatedObj instanceof Number ? JLabel.RIGHT : JLabel.LEFT);

		String text = null;
		String tooltip = null;
		
		boolean isError = objEditStr != null && objEditStr.getErrorText() != null;
		
		// First, set values
		if (objEditStr == null || (validatedObj == null && objEditStr.getErrorText() == null)) {
			text = "";
		} else {
			if (objEditStr.getErrorText() != null) {
				text = "#ERR: " + objEditStr.getErrorText();
			} else if (validatedObj instanceof Boolean) {
				text = validatedObj == Boolean.TRUE ? IconManager.ICON_CHECK_SQUARE : IconManager.ICON_SQUARE_O;
			} else if (validatedObj instanceof Double) {
				String formatStr = null;
				var format = columnView.getVisualProperty(BasicTableVisualLexicon.COLUMN_FORMAT);
				
				if (format != null)
					formatStr = format.getFormat();

				if (formatStr == null || formatStr.isBlank())
					text = validatedObj.toString();
				else
					text = String.format(formatStr, validatedObj);
			} else {
				text = validatedObj.toString();
			}

			tooltip = validatedObj instanceof Boolean ? validatedObj.toString() : text;
		}
		
		if (isSelected) {
			if (table.getSelectedColumn() == colIndex && table.getSelectedRow() == rowIndex) { // Selected
				bg = UIManager.getColor("Table.focusCellBackground");
				fg = UIManager.getColor("Table.focusCellForeground");
			} else {
				bg = UIManager.getColor("Table.selectionBackground");
				fg = isError ? LookAndFeelUtil.getErrorColor() : UIManager.getColor("Table.selectionForeground");
			}
		} else {
			// If non-editable, grey it out.
			if (!table.isCellEditable(0, colIndex))
				fg = UIManager.getColor("TextField.inactiveForeground");
			else
				fg = isError ? LookAndFeelUtil.getErrorColor() : fg;
		}
		
		// Save the custom graphics
		cg = presentation.getCustomGraphics(row, columnView);
		
		setBackground(bg);
		setForeground(fg);
		label.setForeground(fg);
		
		label.setText(text);
		label.setToolTipText(tooltip);
		setToolTipText(tooltip);

		return this;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw custom graphics (sparklines, etc)
		if (cg != null && !cg.equals(NullCustomGraphics.getNullObject())) {
			var g2 = (Graphics2D) g.create();
			
			var w = getWidth();
			var h = getHeight();
			var bounds = new Rectangle2D.Float(0, 0, w, h);
			
			for (var layer : cg.getLayers(tableView, columnView, row)) {
				if (layer instanceof PaintedShape) {
					var ps = (PaintedShape) layer;
					var shape = ps.getShape();

					if (ps.getStroke() != null) {
						var strokePaint = ps.getStrokePaint();

						if (strokePaint == null)
							strokePaint = Color.BLACK;

						g2.setPaint(strokePaint);
						g2.setStroke(ps.getStroke());
						g2.draw(shape);
					}

					g2.setPaint(ps.getPaint());
					g2.fill(shape);
				} else if (layer instanceof Cy2DGraphicLayer) {
					if (layer instanceof SVGLayer) {
						var rect = new Rectangle2D.Double(w / 2.0f, h / 2.0f, w, h);
						((SVGLayer) layer).draw(g2, rect, rect);
					} else {
						layer = affineTransform(layer);
						((Cy2DGraphicLayer) layer).draw(g2, tableView, col, row);
					}
				} else if (layer instanceof ImageCustomGraphicLayer) {
					var b = layer.getBounds2D().getBounds();
					
					// If this is just a paint, getBounds2D will return null and we can use our own width and height
					if (b != null) {
						double cgW = b.getWidth();
						double cgH = b.getHeight();

						// In case size is same, return the original.
						if (w != cgW || h != cgH) {
							double scale = Math.min(w / cgW, h / cgH);
							var xform = AffineTransform.getScaleInstance(scale, scale);
							layer = layer.transform(xform);
							b = layer.getBounds2D().getBounds();
						}
					}
					
					double cw = b.getWidth();
					double ch = b.getHeight();
					double xOffset = (w - cw) / 2.0;
					double yOffset = (h - ch) / 2.0;
					
					var img = ((ImageCustomGraphicLayer) layer).getPaint(b).getImage();
					g2.drawImage(img, (int) xOffset, (int) yOffset, (int) cw, (int) ch, null);
				} else {
					g2.setPaint(layer.getPaint(bounds));
					g2.fill(bounds);
				}
			}
			
			g2.dispose();
		}
	}
	
	private CustomGraphicLayer affineTransform(CustomGraphicLayer layer) {
		var b = layer.getBounds2D();
		
		// If this is just a paint, getBounds2D will return null and we can use our own width and height
		if (b != null) {
			double w = getWidth();
			double h = getHeight();
			double cw = b.getWidth();
			double ch = b.getHeight();
			double cx = b.getX();
			double cy = b.getY();
			double xOffset = -cx + ((w - cw) / 2.0);
			double yOffset = -cy + ((h - ch) / 2.0);
			
			var xform = new AffineTransform();
			xform.translate(xOffset, yOffset);
			xform.scale(w / cw, h / ch);
			layer = layer.transform(xform);
		}
		
		return layer;
	}
	
	private static class EquationIcon implements Icon {

		static final int HEIGHT = 8;
		static final int WIDTH = 8;

		@Override
		public int getIconHeight() {
			return HEIGHT;
		}

		@Override
		public int getIconWidth() {
			return WIDTH;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(LookAndFeelUtil.getSuccessColor());

			int[] xPoints = new int[] { 0, 0, WIDTH };
			int[] yPoints = new int[] { HEIGHT, 0, 0 };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}
}
