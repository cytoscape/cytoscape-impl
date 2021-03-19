package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_FONT_FACE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_TEXT_COLOR;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.COLUMN_TEXT_WRAPPED;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_TOOLTIP;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ALTERNATE_ROW_COLORS;
import static org.cytoscape.view.table.internal.BrowserTableVisualLexicon.CELL_CUSTOMGRAPHICS;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public class BrowserTablePresentation {
	
	private final IconManager iconManager;
	private final Font defaultFont;
	
	public BrowserTablePresentation(Font defaultFont, CyServiceRegistrar registrar) {
		this.iconManager = registrar.getService(IconManager.class);
		this.defaultFont = defaultFont.deriveFont(LookAndFeelUtil.getSmallFontSize());
	}

	public Color getBackgroundColor(CyRow row, int rowIndex, CyColumnView colView, CyTableView tableView) {
		var color = UIManager.getColor("Table.background");
		
		if (Boolean.TRUE.equals(tableView.getVisualProperty(TABLE_ALTERNATE_ROW_COLORS))) {
			if (rowIndex % 2 != 0) {
				color = UIManager.getColor("Table.alternateRowColor");
				
				// The property "Table.alternateRowColor" may have started with a 100% transparency,
				// because we want to preserve the correct LAF color, but don't want then to affect all JTables.
				// We can now set the "correct" transparency of the LAF color and set it again.
				if (color.getAlpha() == 0)
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
			}
		} else {
			if (color == null || colView.isSet(CELL_BACKGROUND_PAINT)) {
				var fn = colView.getCellVisualProperty(CELL_BACKGROUND_PAINT);
				
				if (fn != null) {
					var val = fn.apply(row);
					
					if (val instanceof Color) {
						color = (Color) val;
						
						// To fix a glitch on Nimbus where ColorUIResource is not applied properly,
						// which renders the rows with the standard LAF alternate colors instead
						if (color instanceof ColorUIResource) 
							color = new Color(color.getRGB());
					}
				}
			}
		}
		
		return color;
	}

	public Color getForegroundColor(CyRow row, CyColumnView colView) {
		var color = UIManager.getColor("Table.foreground");
		
		if (color == null || colView.isSet(CELL_TEXT_COLOR)) {
			var fn = colView.getCellVisualProperty(CELL_TEXT_COLOR);
			
			if (fn != null) {
				var vpValue = fn.apply(row);
				
				if (vpValue instanceof Color)
					color = (Color) vpValue;
			}
		}
		
		return color;
	}

	public Font getFont(CyRow row, CyColumnView colView, Object value) {
		var font = defaultFont;
		
		if (value instanceof Boolean) {
			font = iconManager.getIconFont(12.0f);
		} else if (colView.isSet(CELL_FONT_FACE)) {
			var fn = colView.getCellVisualProperty(CELL_FONT_FACE);

			if (fn != null)
				font = fn.apply(row);
		}
		
		if (colView.isSet(CELL_FONT_SIZE)) {
			var fn = colView.getCellVisualProperty(CELL_FONT_SIZE);
			
			if (fn != null) {
				float size = fn.apply(row);
				font = font.deriveFont(size);
			}
		}
		
		return font;
	}
	
	public String getTooltip(CyRow row, CyColumnView colView, String defaultTooltip) {
		var tooltip = defaultTooltip;
		
		if (colView.isSet(CELL_TOOLTIP)) {
			var fn = colView.getCellVisualProperty(CELL_TOOLTIP);
			
			if (fn != null) {
				tooltip = fn.apply(row);
				
				// Unfortunately, the default VP value cannot be null, so let's assume empty is the same as null.
				// Also, once the UI sets an empty String as value, it may be impossible for the user to remove it,
				// so it makes more sense that empty means "not set", in which case we should use the defaultTooltip.
				if (tooltip == null || tooltip.isEmpty())
					tooltip = defaultTooltip;
			}
		}
		
		return tooltip;
	}
	
	public CyCustomGraphics<?> getCustomGraphics(CyRow row, CyColumnView colView) {
		var fn = colView.getCellVisualProperty(CELL_CUSTOMGRAPHICS);

		return fn != null ? fn.apply(row) : null;
	}
	
	public boolean isTextWrapped(CyRow row, CyColumnView colView) {
		var b = false;
		
		if (colView.isSet(COLUMN_TEXT_WRAPPED)) {
			var fn = colView.getCellVisualProperty(COLUMN_TEXT_WRAPPED);
			
			if (fn != null)
				b = fn.apply(row);
		}
		
		return b;
	}
}
