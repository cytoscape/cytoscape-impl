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
import java.lang.reflect.Method;
import java.util.HashMap;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.jdesktop.swingx.JXTitledPanel;

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
	private final Class<T> type;
	
	// Range object.  Actual values will be provided from 
	private final DiscreteRange<T> range;
	private final VisualProperty<T> vp;
	
	private final CyApplicationManager appManager;
	
	private Map<T, Icon> iconMap;
	private boolean canceled;
	
	private JButton applyButton;
	private JButton cancelButton;
	private JList iconList;
	private JScrollPane iconListScrollPane;
	private JXTitledPanel mainPanel;
	private DefaultListModel model;

	public DiscreteValueEditor(final CyApplicationManager appManager, final Class<T> type,
			final DiscreteRange<T> dRange, final VisualProperty<T> vp) {
		super();
		
		if (dRange == null)
			throw new NullPointerException("Range object is null.");

		this.range = dRange;
		this.type = type;
		this.appManager = appManager;
		this.vp = vp;
		this.iconMap = new HashMap<T, Icon>();

		initComponents();
		setListItems();
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return canceled != true ? (T) iconList.getSelectedValue() : null;
	}
	
	@Override
	public <S extends T> T showEditor(Component parent, S initialValue) {
		setListItems();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		final T newValue = getValue();
		canceled = false;
		
		if (newValue == null)
			return initialValue;
		else
			return newValue;
	}

	@Override
	public Class<T> getValueType() {
		return type;
	}
	
	protected int getIconWidth() {
		if (iconWidth == -1) {
			if (vp == BasicVisualLexicon.NODE_BORDER_LINE_TYPE || vp == BasicVisualLexicon.EDGE_LINE_TYPE ||
				vp == BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE || vp == BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE)
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
	
	private void initComponents() {
		setModal(true);
		
		mainPanel = new JXTitledPanel(vp.getDisplayName());
		iconListScrollPane = new JScrollPane();
		iconList = new JList();
		applyButton = new JButton();
		cancelButton = new JButton();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Select New Value");

		mainPanel.setTitleFont(new Font("SansSerif", 1, 14));

		iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iconList.setCursor(new Cursor(Cursor.HAND_CURSOR));
		iconListScrollPane.setViewportView(iconList);

		applyButton.setText("Apply");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});
		
		cancelButton.setVisible(true);

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel.getContentContainer());
		mainPanel.getContentContainer().setLayout(mainPanelLayout);
		
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
		final RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
		
		// Current engine is not ready yet.
		if (engine == null)
			return;
		
		iconMap.clear();
		
		for (T value: values)
			iconMap.put(value, engine.createIcon(vp, value, getIconWidth(), getIconHeight()));
	}
	
	private void cancelButtonActionPerformed(ActionEvent evt) {
		dispose();
		canceled = true;
	}

	private void applyButtonActionPerformed(ActionEvent evt) {
		dispose();
	}

	private void setListItems() {
		final Set<T> values = range.values();
		renderIcons(values);
		
		model = new DefaultListModel();
		iconList.setModel(model);

		for (final T key : values)
			model.addElement(key);

		iconList.setCellRenderer(new IconCellRenderer());
		iconList.repaint();
	}

	private String getLabel(final Object value) {
		String text = null;
		
		// Use reflection to check existence of "getDisplayName" method
		final Class<? extends Object> valueClass = value.getClass();
		
		try {
			final Method displayMethod = valueClass.getMethod("getDisplayName", (Class<?>)null);
			final Object returnVal = displayMethod.invoke(value, (Class<?>)null);
			
			if (returnVal != null)
				text = returnVal.toString();
		} catch (Exception e) {
			// Use toString is failed.
			text = value.toString();
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
			
			final JLabel iconLbl = new JLabel(iconMap.get(value));
			iconLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			
			final JLabel textLbl = new JLabel(getLabel(value));
			textLbl.setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);

//			if (icon != null)
//				setPreferredSize(new Dimension(icon.getIconWidth() + 230, icon.getIconHeight() + 24));
//			else
//				setPreferredSize(new Dimension(230, 60));
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			add(iconLbl);
			add(Box.createHorizontalStrut(20));
			add(textLbl);
			add(Box.createHorizontalGlue());
			
			return this;
		}
	}
}
