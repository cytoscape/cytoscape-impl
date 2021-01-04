package org.cytoscape.view.table.internal.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.function.Function;

import javax.swing.UIManager;

import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class BrowserTablePresentation {
	
	private final IconManager iconManager;
	private final Font defaultFont;
	
	public BrowserTablePresentation(CyServiceRegistrar registrar, Font defaultFont) {
		this.iconManager = registrar.getService(IconManager.class);
		this.defaultFont = defaultFont.deriveFont(LookAndFeelUtil.getSmallFontSize());
	}
	
	public Color getBackgroundColor(CyRow row, CyColumnView colView) {
		// Apply background VP
		Color background = UIManager.getColor("Table.background");
		Function<CyRow,Paint> cellPaintMapping = colView.getCellVisualProperty(BasicTableVisualLexicon.CELL_BACKGROUND_PAINT);
		if(cellPaintMapping != null) {
			Paint vpValue = cellPaintMapping.apply(row);
			if(vpValue instanceof Color) {
				background = (Color) vpValue;
			}
		}
		return background;
	}
	
	public Color getForegroundColor(CyRow row, CyColumnView colView) {
		// Apply background VP
		Color foreground = UIManager.getColor("Table.foreground");
		Function<CyRow,Paint> cellPaintMapping = colView.getCellVisualProperty(BasicTableVisualLexicon.CELL_TEXT_COLOR);
		if(cellPaintMapping != null) {
			Paint vpValue = cellPaintMapping.apply(row);
			if(vpValue instanceof Color) {
				foreground = (Color) vpValue;
			}
		}
		return foreground;
	}

	public Font getFont(CyRow row, CyColumnView colView, Object value) {
		Font font = defaultFont;
		if (value instanceof Boolean) {
			font = iconManager.getIconFont(12.0f);
		} else {
			Function<CyRow,Font> cellFontMapping = colView.getCellVisualProperty(BasicTableVisualLexicon.CELL_FONT_FACE);
			if(cellFontMapping != null) {
				font = cellFontMapping.apply(row);
			}
		}
		return font;
	}
	
}
