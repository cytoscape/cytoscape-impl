package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.jdesktop.swingx.JXList;

/**
 * Value chooser for any discrete values. This includes
 * <ul>
 * <li>node shape</li>
 * <li>arrow shape</li>
 * <li>line style</li>
 * <li>etc.</li>
 * </ul>
 */
public class DiscreteValueEditor<T> extends JDialog implements ValueEditor<T> {
	
	private final static long serialVersionUID = 1202339876950593L;
	
	private final Color BORDER_COLOR = UIManager.getLookAndFeelDefaults().getColor("Separator.foreground");
	
	private int iconWidth = -1; // not initialized!
	private int iconHeight = -1; // not initialized!

	// Value data type for this chooser.
	protected final Class<T> type;
	
	// Range object.  Actual values will be provided from 
	protected final Set<T> values;
	protected final VisualProperty<T> vp;
	
	protected final ServicesUtil servicesUtil;
	
	protected Map<T, Icon> iconMap;
	protected boolean canceled;
	
	protected JButton applyButton;
	protected JButton cancelButton;
	protected JXList iconList;
	protected JScrollPane iconListScrollPane;
	protected JPanel mainPanel;
	protected DefaultListModel model;
	
	private final Collator collator = Collator.getInstance(Locale.getDefault());


	public DiscreteValueEditor(final Class<T> type, Set<T> values, final VisualProperty<T> vp,
			final ServicesUtil servicesUtil) {
		if (type == null)
			throw new NullPointerException("'type' must not be null.");
		if (values == null)
			throw new NullPointerException("'values' must not be null.");
		if (servicesUtil == null)
			throw new NullPointerException("'servicesUtil' must not be null.");

		this.values = values;
		this.type = type;
		this.vp = vp;
		this.servicesUtil = servicesUtil;
		this.iconMap = new HashMap<T, Icon>();

		init();
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return canceled != true ? (T) iconList.getSelectedValue() : null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> T showEditor(final Component parent, final S initialValue) {
		setListItems();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		T newValue = getValue();
		canceled = false;
		
		if (newValue == null)
			newValue = initialValue;
		
		return newValue instanceof Font ? (T) ((Font)newValue).deriveFont(12F) : newValue;
	}

	@Override
	public Class<T> getValueType() {
		return type;
	}
	
	protected int getIconWidth() {
		if (iconWidth == -1) {
			if (type == LineType.class || type == ArrowShape.class)
				iconWidth = 64;
			else
				iconWidth = 32;
		}
		
		return iconWidth;
	}
	
	protected int getIconHeight() {
		if (iconHeight == -1) {
			iconHeight = 32;
		}
		
		return iconHeight;
	}
	
	protected void init() {
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Select New Value");
		
		iconList = new JXList();
		iconList.setModel(model = new DefaultListModel());
		iconList.setCellRenderer(new IconCellRenderer());
		iconList.setAutoCreateRowSorter(true);
		iconList.setSortOrder(SortOrder.ASCENDING);
		iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iconList.setCursor(new Cursor(Cursor.HAND_CURSOR));
		iconList.setComparator(new Comparator<T>() {
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
		iconList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					applyButton.doClick();
				}
			}
		});
		
		iconListScrollPane = new JScrollPane();
		iconListScrollPane.setViewportView(iconList);

		applyButton = new JButton();
		applyButton.setText("Apply");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		cancelButton.setVisible(true);

		mainPanel = new JPanel();
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
						.addContainerGap(128, Short.MAX_VALUE)
						.addComponent(cancelButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(applyButton)
						.addContainerGap())
				.addComponent(iconListScrollPane, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE));
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
						.addComponent(iconListScrollPane, GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(applyButton)
								.addComponent(cancelButton))
						.addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
		pack();
	}

	/**
	 * Use current renderer to create icons.
	 * @param values
	 */
	private void renderIcons(final Set<T> values) {
		if (vp == null || type == Font.class)
			return;
		
		iconMap.clear();
		
		final DefaultViewPanel defViewPanel = servicesUtil.get(DefaultViewPanel.class);
		final RenderingEngine<CyNetwork> engine = defViewPanel != null ? defViewPanel.getRenderingEngine() : null;
		
		// Current engine is not ready yet.
		if (engine != null) {
			for (T value: values)
				iconMap.put(value, engine.createIcon(vp, value, getIconWidth(), getIconHeight()));
		}
	}
	
	protected void applyButtonActionPerformed(ActionEvent evt) {
		dispose();
	}
	
	private void cancelButtonActionPerformed(ActionEvent evt) {
		dispose();
		canceled = true;
	}

	private void setListItems() {
		renderIcons(values);
		model.removeAllElements();
		
		for (final T key : values)
			model.addElement(key);

		iconList.repaint();
	}

	protected String getLabel(final T value) {
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
	
	private final class IconCellRenderer extends JPanel implements ListCellRenderer {
		
		private final static long serialVersionUID = 1202339876940871L;
		
		private final Font SELECTED_FONT = new Font("SansSerif", Font.ITALIC, 14);
		private final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 14);
		private final Color SELECTED_COLOR = new Color(30, 30, 80, 25);

		public IconCellRenderer() {
			setOpaque(true);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Component getListCellRendererComponent(final JList list,
													  final Object value,
													  final int index,
													  final boolean isSelected,
													  final boolean cellHasFocus) {
			removeAll();
			
			setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
			
			final Border border = BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR);
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
			else
				textLbl.setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);

			add(textLbl);
			add(Box.createHorizontalGlue());
			
			return this;
		}
	}
}
