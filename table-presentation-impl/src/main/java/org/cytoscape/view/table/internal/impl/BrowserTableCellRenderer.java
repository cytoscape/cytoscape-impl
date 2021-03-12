package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.ROW_HEIGHT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

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
public class BrowserTableCellRenderer extends JPanel implements TableCellRenderer {

	/** Extra horizontal padding */
	private static final int H_PAD = 5;
	/** Extra vertical padding */
	private static final int V_PAD = 0; // Careful when changing this value, it could crop the text or boolean icons!
	
	private final Pattern htmlPattern = Pattern.compile(
			"[\\S\\s]*\\<html[\\S\\s]*\\>[\\S\\s]*\\<\\/html[\\S\\s]*\\>[\\S\\s]*",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
	);
	
	private JLabel label;
	private JTextArea textArea;
	
	private Color bg;
	private Color fg;
	private Font font;
	private String text;
	private String tooltip;
	private boolean isError;
	private boolean isEquation;
	private CyCustomGraphics<?> cg;
	private final Border border;
	
	private CyColumn col;
	private CyRow row;
	private CyTableView tableView;
	private CyColumnView columnView;
	
	private final DefaultCellRenderer defCellRenderer = new DefaultCellRenderer();
	private final BrowserTablePresentation presentation;
	
	public BrowserTableCellRenderer(CyServiceRegistrar serviceRegistrar) {
		presentation = new BrowserTablePresentation(defCellRenderer.getFont(), serviceRegistrar);
		
		setLayout(new BorderLayout());
		
		// Add padding:
		var defBorder = defCellRenderer.getBorder();
		
		if (defBorder == null)
			border = BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD);
		else
			border = BorderFactory.createCompoundBorder(defBorder, BorderFactory.createEmptyBorder(V_PAD, H_PAD, V_PAD, H_PAD));
		
		setBorder(border);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int colIndex) {
		removeAll();
		
		if (colIndex < 0 || colIndex >= table.getColumnCount() || rowIndex > table.getRowCount())
			return this;
		
		var objEditStr = (ValidatedObjectAndEditString) value;
		var validatedObj = objEditStr != null ? objEditStr.getValidatedObject() : null;
		
		var browserTable = (BrowserTable) table;
		var model = (BrowserTableModel) browserTable.getModel();
		tableView = model.getTableView();
		col = model.getColumnByModelIndex(browserTable.convertColumnIndexToModel(colIndex));
		row = model.getCyRow(browserTable.convertRowIndexToModel(rowIndex));
		
		columnView = (CyColumnView) tableView.getColumnView(col);
		
		bg = presentation.getBackgroundColor(row, rowIndex, columnView, tableView);
		fg = presentation.getForegroundColor(row, columnView);
		font = presentation.getFont(row, columnView, validatedObj);
	
		isError = objEditStr != null && objEditStr.getErrorText() != null;
		isEquation = objEditStr != null && objEditStr.isEquation();
		
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

			tooltip = validatedObj instanceof Boolean ? validatedObj.toString() : text; // default tooltip
			tooltip = presentation.getTooltip(row, columnView, tooltip);
			
			if (tooltip != null && tooltip.isBlank())
				tooltip = null; // don't show an empty tooltip rectangle!
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
		setToolTipText(tooltip);
		
		final JComponent textComp;
		
		// Numbers and Booleans should not be wrapped, though error messages can be
		boolean wrap = (isError || !(validatedObj instanceof Boolean) || !(validatedObj instanceof Number))
				&& presentation.isTextWrapped(row, columnView);
		
		if (wrap) {
			var matcher = htmlPattern.matcher(text);
			
			if (matcher.matches())
				textComp = getTextPane(); // Just so we can render HTML text
			else
				textComp = getTextArea();
		} else {
			textComp = getLabel();
			
			if (validatedObj instanceof Boolean)
				((JLabel) textComp).setHorizontalAlignment(JLabel.CENTER);
			else
				((JLabel) textComp).setHorizontalAlignment(validatedObj instanceof Number ? JLabel.RIGHT : JLabel.LEFT);
		}
		
		if (wrap) {
			var borderInsets = border.getBorderInsets(this);
			int hpad = borderInsets.left + borderInsets.right;
			int vpad = borderInsets.top + borderInsets.bottom;
			textComp.setSize(table.getColumnModel().getColumn(colIndex).getWidth() - hpad, textComp.getPreferredSize().height);
			var h = textComp.getPreferredSize().height + vpad;
			
			// Careful here! We don't want to cause an infinite loop.
			if (h != table.getRowHeight(rowIndex) && h > tableView.getVisualProperty(ROW_HEIGHT))
				table.setRowHeight(rowIndex, h);
		}
		
		add(textComp, BorderLayout.CENTER);
		
		return this;
	}

	private JComponent getLabel() {
		if (label == null) {
			label = new JLabel();
			label.setOpaque(false);
			label.setVerticalTextPosition(JLabel.CENTER);
		}
		
		label.setForeground(fg);
		label.setFont(font);
		label.setText(text);
		label.setToolTipText(tooltip);
		
		return label;
	}
	
	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
			textArea.setBorder(BorderFactory.createEmptyBorder());
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
		}
		
		textArea.setBackground(bg);
		textArea.setForeground(fg);
		textArea.setFont(font);
		textArea.setText(text);
		textArea.setToolTipText(tooltip);
		
		return textArea;
	}

	private JTextPane getTextPane() {
		// If we don't create the JTextPane every time, text wrapping only works the first time
		// (at least on macOS, there's a flag in the UI class that affects its preferred size's height,
		// and it seems we cannot reset it once the component is created)
		var textPane = new JTextPane();
		textPane.setBorder(BorderFactory.createEmptyBorder());
		textPane.setContentType("text/html");
		textPane.setEditorKit(new WrappedHtmlEditorKit());
		textPane.setBackground(bg);
		textPane.setForeground(fg);
		textPane.setText(text);
		textPane.setToolTipText(tooltip);
		
		if (LookAndFeelUtil.isNimbusLAF()) {
			var defaults = UIManager.getLookAndFeelDefaults();
			defaults.put("TextPane.background", bg);
			defaults.put("TextPane.foreground", fg);
			textPane.putClientProperty("Nimbus.Overrides", defaults);
			textPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		}
		
		return textPane;
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
		
		// Draw the equation icon
		if (isEquation) {
			var g2 = (Graphics2D) g.create();
			
			int w = 8;
			int h = 8;
			int[] xPoints = new int[] { 0, 0, w };
			int[] yPoints = new int[] { h, 0, 0 };
			g2.setColor(LookAndFeelUtil.getSuccessColor());
			g2.fillPolygon(xPoints, yPoints, 3);
			
			g2.dispose();
		}
	}
	
	private CustomGraphicLayer affineTransform(CustomGraphicLayer layer) {
		var b = layer.getBounds2D();
		
		// If this is just a paint, getBounds2D will return null and we can use our own width and height
		if (b != null) {
			int pad = 1; // minor padding to better separate sparklines in adjacent cells
			int x = pad;
			int y = pad;
			double w = getWidth() - 2 * pad;
			double h = getHeight() - 2 * pad;
			double cw = b.getWidth();
			double ch = b.getHeight();
			double cx = b.getX();
			double cy = b.getY();
			double xOffset = -cx + x + ((w - cw) / 2.0);
			double yOffset = -cy + y + ((h - ch) / 2.0);
			
			var xform = new AffineTransform();
			xform.translate(xOffset, yOffset);
			xform.scale(w / cw, h / ch);
			layer = layer.transform(xform);
		}
		
		return layer;
	}
	
	private class WrappedHtmlEditorKit extends HTMLEditorKit {

		private ViewFactory viewFactory;

		public WrappedHtmlEditorKit() {
			this.viewFactory = new WrappedHtmlFactory();
		}

		@Override
		public ViewFactory getViewFactory() {
			return this.viewFactory;
		}

		private class WrappedHtmlFactory extends HTMLEditorKit.HTMLFactory {
			
			@Override
			public View create(Element elem) {
				var v = super.create(elem);

				if (v instanceof LabelView) {
					var o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);

					if ((o instanceof HTML.Tag) && o == HTML.Tag.BR)
						return v;

					return new WrapLabelView(elem);
				}

				return v;
			}

			private class WrapLabelView extends LabelView {
				
				public WrapLabelView(Element elem) {
					super(elem);
				}

				@Override
				public float getMinimumSpan(int axis) {
					switch (axis) {
						case View.X_AXIS:
							return 0;
						case View.Y_AXIS:
							return super.getMinimumSpan(axis);
						default:
							throw new IllegalArgumentException("Invalid axis: " + axis);
					}
				}
			}
		}
	}
}
