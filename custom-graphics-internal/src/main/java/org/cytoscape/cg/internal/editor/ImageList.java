package org.cytoscape.cg.internal.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.UIManager;

import org.cytoscape.cg.internal.util.ViewUtil;
import org.cytoscape.cg.internal.util.VisualPropertyIconFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.icon.EmptyIcon;

@SuppressWarnings("serial")
public class ImageList extends JXList {
	
	private static final int ICON_SIZE = 40;
	
	private final Set<CyCustomGraphics> values;
	private final Map<CyCustomGraphics, Icon> iconMap;
	private final DefaultListModel<CyCustomGraphics> model;
	
	private final DefaultViewPanel defViewPanel;

	public ImageList(DefaultViewPanel defViewPanel) {
		this.defViewPanel = defViewPanel;
		this.values = Collections.synchronizedSet(new LinkedHashSet<>());
		iconMap = new HashMap<>();
		
		setModel(model = new DefaultListModel<>());
		setCellRenderer(new IconCellRenderer());
		
		setAutoCreateRowSorter(true);
		setSortOrder(SortOrder.ASCENDING);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		var collator = Collator.getInstance(Locale.getDefault());
		
		setComparator(new Comparator<CyCustomGraphics<?>>() {
			@Override
			public int compare(CyCustomGraphics<?> o1, CyCustomGraphics<?> o2) {
				return collator.compare(o1.getDisplayName(), o2.getDisplayName());
			}
		});
	}
	
	/**
	 * Use current renderer to create icons.
	 */
	private void renderIcons(Set<CyCustomGraphics> values) {
		iconMap.clear();
		
		var engine = defViewPanel != null ? defViewPanel.getRenderingEngine() : null;
		
		// Current engine is not ready yet.
		if (engine != null) {
			synchronized (values) {
				for (CyCustomGraphics<?> val : values) {
					var icon = VisualPropertyIconFactory.createIcon(val, ICON_SIZE, ICON_SIZE);
					
					if (icon != null)
						iconMap.put(val, icon);
				}
			}
		}
	}
	
	public void setListItems(Collection<CyCustomGraphics> newValues, CyCustomGraphics<?> selectedValue) {
		synchronized (values) {
			values.clear();
			
			if (newValues != null)
				values.addAll(newValues);
		}
		
		renderIcons(values);
		model.removeAllElements();
		
		synchronized (values) {
			for (CyCustomGraphics<?> val : values)
				model.addElement(val);
		}

		if (selectedValue != null)
			setSelectedValue(selectedValue, true);
		
		repaint();
	}
	
	@SuppressWarnings("rawtypes")
	private final class IconCellRenderer extends JPanel implements ListCellRenderer<CyCustomGraphics> {
		
		private final Color BG_COLOR = UIManager.getColor("Table.background");
		private final Color FG_COLOR = UIManager.getColor("Table.foreground");
		private final Color SELECTED_BG_COLOR = UIManager.getColor("Table.selectionBackground");
		private final Color SELECTED_FG_COLOR = UIManager.getColor("Table.selectionForeground");
		
		private JLabel iconLbl = new JLabel("");
		private JLabel textLbl = new JLabel("");
		
		private final Icon emptyIcon = new EmptyIcon(ICON_SIZE, ICON_SIZE);

		public IconCellRenderer() {
			setOpaque(true);
			
			var border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"));
			var paddingBorder = BorderFactory.createEmptyBorder(0, 4, 0, 4);
			setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(iconLbl);
			add(Box.createHorizontalStrut(20));
			add(textLbl);
			add(Box.createHorizontalGlue());
			
			LookAndFeelUtil.makeSmall(textLbl);
		}
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends CyCustomGraphics> list,
				CyCustomGraphics value,
				int index,
				boolean isSelected,
				boolean cellHasFocus
		) {
			setBackground(isSelected ? SELECTED_BG_COLOR : BG_COLOR);
			setForeground(isSelected ? SELECTED_FG_COLOR : FG_COLOR);

			var icon = iconMap.get(value);

			iconLbl.setIcon(icon != null ? iconMap.get(value) : emptyIcon);
			textLbl.setText(ViewUtil.getShortName(value.getDisplayName()));
			
			revalidate();

			return this;
		}
	}
}
