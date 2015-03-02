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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyUtil;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.FontCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.IconCellRenderer;
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
	
	// Value data type for this chooser.
	protected final Class<T> type;
	
	// Range object.  Actual values will be provided from 
	protected final Set<T> values;
	protected final VisualProperty<T> vp;
	
	protected final ServicesUtil servicesUtil;
	
	protected boolean canceled;
	
	protected JButton applyButton;
	protected JButton cancelButton;
	protected DiscreteValueList<T> discreteValueList;
	protected JScrollPane iconListScrollPane;
	protected JPanel mainPanel;
	
	@SuppressWarnings("unchecked")
	public DiscreteValueEditor(final VisualProperty<T> vp, final ServicesUtil servicesUtil) {
		if (vp == null)
			throw new NullPointerException("'vp' must not be null.");
		if (servicesUtil == null)
			throw new NullPointerException("'servicesUtil' must not be null.");
		
		final Range<?> range = vp.getRange();
		
		if (range instanceof DiscreteRange == false)
			throw new IllegalArgumentException("Visual Property's range must be a DiscreteRange.");

		this.values = ((DiscreteRange<T>) range).values();
		this.type = ((DiscreteRange<T>) range).getType();
		this.vp = vp;
		this.servicesUtil = servicesUtil;

		init();
	}
	
	protected DiscreteValueEditor(final Class<T> type, final Set<T> values, final ServicesUtil servicesUtil) {
		if (type == null)
			throw new NullPointerException("'type' must not be null.");
		if (values == null)
			throw new NullPointerException("'values' must not be null.");
		if (servicesUtil == null)
			throw new NullPointerException("'servicesUtil' must not be null.");

		this.values = values;
		this.type = type;
		this.vp = null;
		this.servicesUtil = servicesUtil;
		
		init();
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return canceled != true ? (T) getDiscreteValueList().getSelectedValue() : null;
	}
	
	public void setValue(final T value) {
		getDiscreteValueList().setSelectedValue(value, true);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> T showEditor(final Component parent, final S initialValue) {
		final Set<T> supportedValues = getSupportedValues();
		
		getDiscreteValueList().setListItems(supportedValues, initialValue);
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
	
	private void init() {
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(vp != null ? vp.getDisplayName() : "Select New Value");
		
		iconListScrollPane = new JScrollPane();
		iconListScrollPane.setViewportView(getDiscreteValueList());

		mainPanel = new JPanel();
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
						.addContainerGap(128, Short.MAX_VALUE)
						.addComponent(getCancelButton())
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(getApplyButton())
						.addContainerGap())
				.addComponent(iconListScrollPane, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE));
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
						.addComponent(iconListScrollPane, GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(getApplyButton())
								.addComponent(getCancelButton()))
						.addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		
		pack();
	}
	
	private DiscreteValueList<T> getDiscreteValueList() {
		if (discreteValueList == null) {
			discreteValueList = new DiscreteValueList<T>(type, vp, servicesUtil);
			discreteValueList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent evt) {
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
			applyButton = new JButton();
			applyButton.setText("Apply");
			applyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					applyButtonActionPerformed(evt);
				}
			});
		}
		
		return applyButton;
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					cancelButtonActionPerformed(evt);
				}
			});
			cancelButton.setVisible(true);
		}
		
		return cancelButton;
	}

	private void applyButtonActionPerformed(ActionEvent evt) {
		dispose();
	}
	
	private void cancelButtonActionPerformed(ActionEvent evt) {
		dispose();
		canceled = true;
	}
	
	private Set<T> getSupportedValues() {
		if (vp == null)
			return values;
		
		final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		final VisualLexicon lexicon = appMgr.getCurrentNetworkViewRenderer()
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		
		return lexicon.getSupportedValueRange(vp);
	}

	static class DiscreteValueList<T> extends JXList {
		
		private static final long serialVersionUID = 391558018818678186L;
		
		private int iconWidth = -1; // not initialized!
		private int iconHeight = -1; // not initialized!
		
		private final Class<T> type;
		private final VisualProperty<T> vp;
		private final Set<T> values;
		private final Map<T, Icon> iconMap;
		private final DefaultListModel model;
		private final ServicesUtil servicesUtil;

		DiscreteValueList(final Class<T> type,
						  final VisualProperty<T> vp,
						  final ServicesUtil servicesUtil) {
			this.type = type;
			this.vp = vp;
			this.values = Collections.synchronizedSet(new LinkedHashSet<T>());
			this.servicesUtil = servicesUtil;
			iconMap = new HashMap<T, Icon>();
			
			setModel(model = new DefaultListModel());
			setCellRenderer(type == Font.class ? new FontCellRenderer() : new IconCellRenderer<T>(iconMap));
			
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
		private void renderIcons(final Set<T> values) {
			if (type == Font.class)
				return;
			
			iconMap.clear();
			
			final DefaultViewPanel defViewPanel = servicesUtil.get(DefaultViewPanel.class);
			final RenderingEngine<CyNetwork> engine = defViewPanel != null ? defViewPanel.getRenderingEngine() : null;
			
			// Current engine is not ready yet.
			if (engine != null) {
				synchronized (values) {
					for (T value: values) {
						Icon icon = null;
						
						if (value instanceof CyCustomGraphics) {
							final Image img = ((CyCustomGraphics)value).getRenderedImage();
							
							if (img != null)
								icon = VisualPropertyUtil.resizeIcon(new ImageIcon(img), getIconWidth(), getIconHeight());
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
	}
}
