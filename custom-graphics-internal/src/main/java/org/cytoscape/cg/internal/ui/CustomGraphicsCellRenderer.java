package org.cytoscape.cg.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.cg.internal.util.VisualPropertyIconFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Cell renderer for Custom Graphics Browser.
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CustomGraphicsCellRenderer extends JPanel implements ListCellRenderer<CyCustomGraphics> {

	private static final int ICON_SIZE = 96;
	private static final int CELL_WIDTH = 150;

	private final Map<CyCustomGraphics<?>, ImagePanel> panelMap;

	public CustomGraphicsCellRenderer() {
		panelMap = new HashMap<>();
	}

	@Override
	public Component getListCellRendererComponent(JList list, CyCustomGraphics cg, int index, boolean isSelected,
			boolean cellHasFocus) {
		ImagePanel target = null;
		
		if (cg != null) {
			target = panelMap.get(cg);
			
			if (target == null) {
				target = new ImagePanel(cg);
				panelMap.put(cg, target);
			}
			
			target.setSelected(isSelected);
		}
		
		return target;
	}

	private class ImagePanel extends JPanel {
		
		private final JLabel nameLbl;
		private final JLabel iconLbl;
		
		final Color BG_COLOR;
		final Color FG_COLOR;
		final Color SEL_BG_COLOR;
		final Color SEL_FG_COLOR;
		final Color BORDER_COLOR;
		
		ImagePanel(CyCustomGraphics<?> cg) {
			super(new BorderLayout());
			
			var list = new JList<>();
			BG_COLOR = list.getBackground();
			FG_COLOR = list.getForeground();
			SEL_BG_COLOR = list.getSelectionBackground();
			SEL_FG_COLOR = list.getSelectionForeground();
			BORDER_COLOR = new Separator().getForeground();
			
			setToolTipText(cg.getDisplayName());
			
			var name = ViewUtil.getShortName(cg.getDisplayName());
			
			nameLbl = new JLabel(name);
			nameLbl.setHorizontalAlignment(JLabel.CENTER);
			nameLbl.setFont(nameLbl.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			nameLbl.setOpaque(true);
			
			iconLbl = new JLabel();
			iconLbl.setHorizontalAlignment(JLabel.CENTER);
			iconLbl.setOpaque(true);
			iconLbl.setBackground(BG_COLOR);
				
			var icon = VisualPropertyIconFactory.createIcon(cg, ICON_SIZE, ICON_SIZE);
			iconLbl.setIcon(icon);
			
			add(iconLbl, BorderLayout.CENTER);
			add(nameLbl, BorderLayout.SOUTH);
			
			setPreferredSize(new Dimension(CELL_WIDTH, 10 + getPreferredSize().height));
		}
		
		void setSelected(boolean selected) {
			final Border border;
			
			if (selected) {
				border = BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1,  1,  1,  1),
						BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
				
				nameLbl.setBackground(SEL_BG_COLOR);
				nameLbl.setForeground(SEL_FG_COLOR);
			} else {
				border = BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(2,  2,  2,  2),
						BorderFactory.createLineBorder(BORDER_COLOR, 1));
				
				nameLbl.setBackground(BG_COLOR);
				nameLbl.setForeground(FG_COLOR);
			}
			
			setBorder(border);
		}
	}
}
