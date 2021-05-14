package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;
import org.cytoscape.view.vizmap.gui.internal.CurrentTableService;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyUtil;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.FontCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.IconCellRenderer;
import org.jdesktop.swingx.JXList;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

/**
 * Value chooser for any discrete values. This includes
 * <ul>
 * <li>node shape</li>
 * <li>arrow shape</li>
 * <li>line style</li>
 * <li>etc.</li>
 * </ul>
 */
public class DiscreteValueEditor<T> implements VisualPropertyValueEditor<T> {
	
	// Value data type for this chooser.
	protected final Class<T> type;
	
	// Range object.  Actual values will be provided from 
	protected final Set<T> values;
	
	protected final ServicesUtil servicesUtil;
	
	protected boolean canceled;
	
	protected DiscreteValueDialog dialog;
	
	public DiscreteValueEditor(Class<T> type, Set<T> values, ServicesUtil servicesUtil) {
		if (type == null)
			throw new IllegalArgumentException("'type' must not be null.");
		if (values == null)
			throw new IllegalArgumentException("'values' must not be null.");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null.");

		this.values = values;
		this.type = type;
		this.servicesUtil = servicesUtil;
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return canceled != true ? (T) dialog.getDiscreteValueList().getSelectedValue() : null;
	}
	
	public void setValue(T value) {
		dialog.getDiscreteValueList().setSelectedValue(value, true);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> T showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		if (dialog == null)
			dialog = new DiscreteValueDialog(JOptionPane.getFrameForComponent(parent));
		
		dialog.setTitle(vp != null ? vp.getDisplayName() : "Select New Value");
		
		var supportedValues = getSupportedValues(vp);
		
		dialog.getDiscreteValueList().setVisualProperty((VisualProperty<T>) vp);
		dialog.getDiscreteValueList().setListItems(supportedValues, initialValue);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		
		T newValue = getValue();
		canceled = false;
		
		if (newValue == null)
			newValue = initialValue;
		
		return newValue;
	}

	@Override
	public Class<T> getValueType() {
		return type;
	}
	
	@SuppressWarnings("unchecked")
	private <S extends T> Set<T> getSupportedValues(VisualProperty<S> vp) {
		if (vp == null)
			return values;
		
		var lexicon = servicesUtil.get(CurrentTableService.class).getCurrentVisualLexicon(vp);
		
		return (Set<T>) lexicon.getSupportedValueRange(vp);
	}

	@SuppressWarnings("serial")
	protected class DiscreteValueDialog extends JDialog {
		
		protected JButton applyButton;
		protected JButton cancelButton;
		protected DiscreteValueList<T> discreteValueList;
		protected JScrollPane iconListScrollPane;
		protected JPanel mainPanel;
		
		protected DiscreteValueDialog(Window owner) {
			super(owner, ModalityType.APPLICATION_MODAL);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			iconListScrollPane = new JScrollPane();
			iconListScrollPane.setViewportView(getDiscreteValueList());

			mainPanel = new JPanel();
			{
				var layout = new GroupLayout(mainPanel);
				mainPanel.setLayout(layout);
				
				layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addContainerGap(128, Short.MAX_VALUE)
								.addComponent(getCancelButton())
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(getApplyButton())
								.addContainerGap()
						)
						.addComponent(iconListScrollPane, DEFAULT_SIZE, 291, Short.MAX_VALUE)
				);
				layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
								.addComponent(iconListScrollPane, DEFAULT_SIZE, 312, Short.MAX_VALUE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(layout.createParallelGroup(Alignment.BASELINE)
										.addComponent(getApplyButton())
										.addComponent(getCancelButton())
								)
								.addContainerGap()
						)
				);
			}

			var contentPane = new JPanel();
			var layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addComponent(mainPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addComponent(mainPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			
			setContentPane(contentPane);
			pack();
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getApplyButton().getAction(),
					getCancelButton().getAction());
			getRootPane().setDefaultButton(getApplyButton());
		}
		
		private DiscreteValueList<T> getDiscreteValueList() {
			if (discreteValueList == null) {
				discreteValueList = new DiscreteValueList<T>(type, servicesUtil);
				discreteValueList.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent evt) {
						if (evt.getClickCount() == 2) {
							getApplyButton().doClick();
						}
					}
				});
			}
			
			return discreteValueList;
		}
		
		private JButton getApplyButton() {
			if (applyButton == null) {
				applyButton = new JButton(new AbstractAction("Apply") {
					@Override
					public void actionPerformed(ActionEvent evt) {
						dialog.dispose();
					}
				});
			}
			
			return applyButton;
		}
		
		private JButton getCancelButton() {
			if (cancelButton == null) {
				cancelButton = new JButton(new AbstractAction("Cancel") {
					@Override
					public void actionPerformed(ActionEvent evt) {
						dialog.dispose();
						canceled = true;
					}
				});
				cancelButton.setVisible(true);
			}
			
			return cancelButton;
		}
	}
	
	@SuppressWarnings("serial")
	static class DiscreteValueList<T> extends JXList {
		
		private int iconWidth = -1; // not initialized!
		private int iconHeight = -1; // not initialized!
		
		private final Class<T> type;
		private VisualProperty<T> visualProperty;
		private final Map<T, Icon> iconMap;
		private final DefaultListModel<T> model;
		private final ServicesUtil servicesUtil;

		DiscreteValueList(Class<T> type, ServicesUtil servicesUtil) {
			this.type = type;
			this.servicesUtil = servicesUtil;
			iconMap = new HashMap<T, Icon>();
			
			setModel(model = new DefaultListModel<>());
			setCellRenderer(type == Font.class ? new FontCellRenderer() : new IconCellRenderer<T>(iconMap));
			
			setAutoCreateRowSorter(true);
			setSortOrder(SortOrder.ASCENDING);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			var collator = Collator.getInstance(Locale.getDefault());
			
			setComparator(new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					if (o1 instanceof VisualPropertyValue)
						return collator.compare(((VisualPropertyValue) o1).getDisplayName(),
								((VisualPropertyValue) o2).getDisplayName());
					if (o1 instanceof Font)
						return collator.compare(((Font) o1).getFontName(), ((Font) o2).getFontName());
					return collator.compare(o1.toString(), o2.toString());
				}
			});
		}
		
		void setVisualProperty(VisualProperty<T> visualProperty) {
			this.visualProperty = visualProperty;
		}
		
		/**
		 * Use current renderer to create icons.
		 * @param values
		 */
		private void renderIcons(Set<T> values) {
			if (type == Font.class)
				return;
			
			iconMap.clear();
			
			var currentTableService = servicesUtil.get(CurrentTableService.class);
			var engine = currentTableService.getRenderingEngine(visualProperty);
			
			// Current engine is not ready yet.
			if (engine != null) {
				synchronized (values) {
					for (T value : values) {
						Icon icon = null;
						
						if (value instanceof CyCustomGraphics) {
							var img = ((CyCustomGraphics<?>) value).getRenderedImage();
							
							if (img != null)
								icon = VisualPropertyUtil.resizeIcon(new ImageIcon(img), getIconWidth(), getIconHeight());
						} else if (visualProperty != null) {
							icon = engine.createIcon(visualProperty, value, getIconWidth(), getIconHeight());
						}
						
						if (icon != null)
							iconMap.put(value, icon);
					}
				}
			}
		}
		
		void setListItems(Set<T> newValues, T selectedValue) {
			renderIcons(newValues);
			model.removeAllElements();
			
			for (T key : newValues)
				model.addElement(key);

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
			if (iconHeight == -1)
				iconHeight = 32;
			
			return iconHeight;
		}
	}
}
