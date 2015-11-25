package org.cytoscape.ding.impl.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.lang.reflect.Method;
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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.ding.internal.util.IconUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.jdesktop.swingx.JXList;

@SuppressWarnings("serial")
public class DiscreteValueList<T> extends JXList {
	
	private int iconWidth = -1; // not initialized!
	private int iconHeight = -1; // not initialized!
	
	private final Class<T> type;
	private final VisualProperty<T> vp;
	private final Set<T> values;
	private final Map<T, Icon> iconMap;
	private final DefaultListModel model;
	
	private final DefaultViewPanel defViewPanel;

	@SuppressWarnings("rawtypes")
	DiscreteValueList(final Class<T> type, final VisualProperty<T> vp, final DefaultViewPanel defViewPanel) {
		this.type = type;
		this.vp = vp;
		this.defViewPanel = defViewPanel;
		this.values = Collections.synchronizedSet(new LinkedHashSet<T>());
		iconMap = new HashMap<T, Icon>();
		
		setModel(model = new DefaultListModel());
		setCellRenderer(new IconCellRenderer());
		
		setAutoCreateRowSorter(true);
		setSortOrder(SortOrder.ASCENDING);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		
		setComparator(new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (o1 instanceof VisualPropertyValue)
					return collator.compare(((VisualPropertyValue)o1).getDisplayName(),
							((VisualPropertyValue)o2).getDisplayName());
				if (o1 instanceof Font)
					return collator.compare(((Font)o1).getFontName(), ((Font)o2).getFontName());
				return collator.compare(o1.toString(), o2.toString());
			}
		});
	}
	
	/**
	 * Use current renderer to create icons.
	 * @param values
	 */
	@SuppressWarnings("rawtypes")
	private void renderIcons(final Set<T> values) {
		if (type == Font.class)
			return;
		
		iconMap.clear();
		
		final RenderingEngine<CyNetwork> engine = defViewPanel != null ? defViewPanel.getRenderingEngine() : null;
		
		// Current engine is not ready yet.
		if (engine != null) {
			synchronized (values) {
				for (T value: values) {
					Icon icon = null;
					
					if (value instanceof CyCustomGraphics) {
						final Image img = ((CyCustomGraphics)value).getRenderedImage();
						
						if (img != null)
							icon = IconUtil.resizeIcon(new ImageIcon(img), getIconWidth(), getIconHeight());
					} else if (vp != null) {
						icon = engine.createIcon(vp, value, getIconWidth(), getIconHeight());
					}
					
					if (icon != null)
						iconMap.put(value, icon);
				}
			}
		}
	}
	
	protected void setListItems(final Collection<T> newValues, final T selectedValue) {
		synchronized (values) {
			values.clear();
			
			if (newValues != null)
				values.addAll(newValues);
		}
		
		renderIcons(values);
		model.removeAllElements();
		
		synchronized (values) {
			for (final T key : values)
				model.addElement(key);
		}

		if (selectedValue != null)
			setSelectedValue(selectedValue, true);
		
		repaint();
	}
	
	private int getIconWidth() {
		if (iconWidth == -1) {
			if (type == LineType.class || type == ArrowShape.class)
				iconWidth = 64;
			else
				iconWidth = 32;
		}
		
		return iconWidth;
	}
	
	private int getIconHeight() {
		if (iconHeight == -1) {
			iconHeight = 32;
		}
		
		return iconHeight;
	}
	
	private final class IconCellRenderer extends JPanel implements ListCellRenderer {
		
		private final Color BG_COLOR = UIManager.getColor("Table.background");
		private final Color FG_COLOR = UIManager.getColor("Table.foreground");
		private final Color SELECTED_BG_COLOR = UIManager.getColor("Table.selectionBackground");
		private final Color SELECTED_FG_COLOR = UIManager.getColor("Table.selectionForeground");

		public IconCellRenderer() {
			setOpaque(true);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Component getListCellRendererComponent(final JList list,
													  final Object value,
													  final int index,
													  final boolean isSelected,
													  final boolean cellHasFocus) {
			removeAll();
			
			setBackground(isSelected ? SELECTED_BG_COLOR : BG_COLOR);
			setForeground(isSelected ? SELECTED_FG_COLOR : FG_COLOR);
			
			final Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"));
			final Border paddingBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
			setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			final Icon icon = iconMap.get(value);
			
			if (icon != null) {
				final JLabel iconLbl = new JLabel(iconMap.get(value));
				iconLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
				add(iconLbl);
				add(Box.createHorizontalStrut(20));
			}
			
			final JLabel textLbl = new JLabel(getLabel((T)value));
			
			if (value instanceof Font)
				textLbl.setFont(((Font) value).deriveFont(14.0f));

			add(textLbl);
			add(Box.createHorizontalGlue());
			
			return this;
		}
	}
	
	private String getLabel(final T value) {
		String text = null;
		
		// Use reflection to check existence of "getDisplayName" method
		final Class<? extends Object> valueClass = value.getClass();
		
		if (value instanceof Font) {
			text  = ((Font)value).getFontName();
		} else {
			try {
				final Method displayMethod = valueClass.getMethod("getDisplayName", (Class<?>)null);
				final Object returnVal = displayMethod.invoke(value, (Class<?>)null);
				
				if (returnVal != null)
					text = returnVal.toString();
			} catch (Exception e) {
				// Use toString is failed.
				text = value.toString();
			}
		}
		
		return text;
	}

}
