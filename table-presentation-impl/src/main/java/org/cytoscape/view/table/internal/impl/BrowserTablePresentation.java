package org.cytoscape.view.table.internal.impl;

import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_FONT_FACE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.CELL_TEXT_COLOR;
import static org.cytoscape.view.table.internal.BrowserTableVisualLexicon.CELL_CUSTOMGRAPHICS;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

public class BrowserTablePresentation {
	
	private final IconManager iconManager;
	private final Font defaultFont;

	public BrowserTablePresentation(CyServiceRegistrar registrar, Font defaultFont) {
		this.iconManager = registrar.getService(IconManager.class);
		this.defaultFont = defaultFont.deriveFont(LookAndFeelUtil.getSmallFontSize());
	}

	public Color getBackgroundColor(CyRow row, CyColumnView colView) {
		var color = UIManager.getColor("Table.background");
		
		if (color == null || colView.isSet(CELL_BACKGROUND_PAINT)) {
			var fn = colView.getCellVisualProperty(CELL_BACKGROUND_PAINT);
			
			if (fn != null) {
				var val = fn.apply(row);
				
				if (val instanceof Color)
					color = (Color) val;
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
			var fn1 = colView.getCellVisualProperty(CELL_FONT_FACE);
			
			if (fn1 != null)
				font = fn1.apply(row);
			
			var fn2 = colView.getCellVisualProperty(CELL_FONT_SIZE);
			
			if (fn2 != null) {
				float size = fn2.apply(row);
				font = font.deriveFont(size);
			}
		}
		
		return font;
	}

	public CyCustomGraphics<?> getCustomGraphics(CyRow row, CyColumnView colView) {
		var fn = colView.getCellVisualProperty(CELL_CUSTOMGRAPHICS);

		return fn != null ? fn.apply(row) : null;
	}
}
