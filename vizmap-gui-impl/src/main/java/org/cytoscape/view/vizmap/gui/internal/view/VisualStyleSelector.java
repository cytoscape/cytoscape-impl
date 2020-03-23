package org.cytoscape.view.vizmap.gui.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class VisualStyleSelector extends JPanel {

	private final int ITEM_WIDTH = 120;
	private final int MAX_COLUMNS = 3;
	
	final Color BG_COLOR;
	final Color FG_COLOR;
	final Color SEL_BG_COLOR;
	final Color SEL_FG_COLOR;
	final Color BORDER_COLOR;
	
	private int cols;
	
	private LinkedList<VisualStyle> styles;
	private VisualStyle selectedItem;
	private VisualStyle focusedItem;
	
	private final Map<VisualStyle, JPanel> vsPanelMap;
	private final Map<String, RenderingEngine<CyNetwork>> engineMap;
	
	private CyNetworkView previewNetView;
	private final Map<String/*visual style name*/, JPanel> defViewPanelsMap;
	private final ServicesUtil servicesUtil;
	
	public VisualStyleSelector(ServicesUtil servicesUtil) {
		super(true);

		this.servicesUtil = servicesUtil;
		
		styles = new LinkedList<>();
		vsPanelMap = new HashMap<>();
		engineMap = new HashMap<>();
		defViewPanelsMap = new HashMap<>();
		
		BG_COLOR = UIManager.getColor("TextField.background");
		FG_COLOR = UIManager.getColor("TextField.foreground");
		SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
		SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
		BORDER_COLOR = UIManager.getColor("Separator.foreground");
		
		setBackground(BG_COLOR);
		setKeyBindings(this);
	}
	
	public void update(SortedSet<VisualStyle> styles, CyNetworkView previewNetView) {
		this.previewNetView = previewNetView;
		this.styles.clear();
		this.vsPanelMap.clear();
		
		if (styles != null)
			this.styles.addAll(styles);
		
		createPreviewRenderingEngines();
		
		cols = MAX_COLUMNS;
		removeAll();
		
		var layout = new GridLayout(0, cols);
		setLayout(layout);
		
		if (styles != null) {
			for (var vs : styles) {
				var itemPnl = createItem(vs);
				add(itemPnl);
			}
		}
		
		setFocus(selectedItem);
		setEnabled(!this.styles.isEmpty());
	}
	
	public void setSelectedItem(VisualStyle vs) {
		if (vs == null || (styles != null && styles.contains(vs) && !vs.equals(selectedItem))) {
			var oldStyle = selectedItem;
			selectedItem = vs;
			repaint();
			firePropertyChange("selectedItem", oldStyle, vs);
		}
	}
	
	public VisualStyle getSelectedItem() {
		return selectedItem;
	}
	
	public JPanel getDefaultView(VisualStyle vs) {
		return defViewPanelsMap.get(vs.getTitle());
	}
	
	public RenderingEngine<CyNetwork> getRenderingEngine(VisualStyle vs) {
		return vs != null ? engineMap.get(vs.getTitle()) : null;
	}
	
	public boolean isEmpty() {
		return styles.isEmpty();
	}
	
	private void createPreviewRenderingEngines() {
		if (styles != null && previewNetView != null) {
			defViewPanelsMap.clear();
			engineMap.clear();
			
			var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
			var engineFactory = vmProxy.getRenderingEngineFactory(previewNetView);
			
			for (var vs : styles) {
				var p = new JPanel();
				defViewPanelsMap.put(vs.getTitle(), p);
				
				var engine = engineFactory.createRenderingEngine(p, previewNetView);
				engineMap.put(vs.getTitle(), engine);
			}
		}
	}
	
	private JPanel createItem(final VisualStyle vs) {
		var panel = new JPanel(new BorderLayout());
		panel.setBackground(BG_COLOR);
		
		// Text label
		var lbl = new JLabel(vs.getTitle());
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setOpaque(true);
		
		// TODO Truncate the style name and add "..." if too long
//			lbl.setUI(new BasicLabelUI() { // TODO add tooltip if truncated
//				@Override
//		        protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon,
//		            Rectangle viewR, Rectangle iconR, Rectangle textR) {
//		            return super.layoutCL(label, fontMetrics, text, icon, viewR, iconR, textR);
//		        }
//			});
		
		panel.add(lbl, BorderLayout.SOUTH);
		
		// Events
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				setFocus(vs);
			}
			@Override
			public void mouseClicked(final MouseEvent e) {
				setSelectedItem(focusedItem);
			}
		});
		
		// Preview image
		if (previewNetView != null) {
			var engine = getRenderingEngine(vs);
			
			if (engine != null) {
				vs.apply(previewNetView);
				previewNetView.updateView();
				previewNetView.fitContent();
				
				// TODO cache images
				var img = engine.createImage(ITEM_WIDTH, 68);
				var icon = new ImageIcon(img); 
				var iconLbl = new JLabel(icon);
				iconLbl.setOpaque(true);
				var bgPaint = vs.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
				var bgColor = bgPaint instanceof Color ? (Color)bgPaint : BG_COLOR;
				iconLbl.setBackground(bgColor);
				
				panel.add(iconLbl, BorderLayout.CENTER);
			}
		}
		
		vsPanelMap.put(vs, panel);
		
		return panel;
	}
	
	private void setFocus(VisualStyle vs) {
		focusedItem = vs;
		updateItems();
	}
	
	private void setFocus(int index) {
		if (index > -1 && index < styles.size()) {
			final VisualStyle vs = styles.get(index);
			setFocus(vs);
		}
	}
	
	private void updateItems() {
		for (var entry : vsPanelMap.entrySet())
			updateItem(entry.getValue(), entry.getKey());
	}

	private void updateItem(JPanel panel, VisualStyle vs) {
		if (vs.equals(focusedItem)) {
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1,  1,  1,  1),
					BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
			panel.setBorder(border);
		} else if (vs.equals(selectedItem)) {
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1,  1,  1,  1),
					BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
			panel.setBorder(border);
		} else {
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(2,  2,  2,  2),
					BorderFactory.createLineBorder(BORDER_COLOR, 1));
			panel.setBorder(border);
		}
		
		var layout = (BorderLayout) panel.getLayout();
		var label = (JLabel) layout.getLayoutComponent(BorderLayout.SOUTH);
		
		if (label != null) {
			if (vs.equals(focusedItem)) {
				label.setBackground(SEL_BG_COLOR);
				label.setForeground(SEL_FG_COLOR);
			} else {
				label.setBackground(BG_COLOR);
				label.setForeground(FG_COLOR);
			}
		}
	}
	
	private void setKeyBindings(JPanel panel) {
		var actionMap = panel.getActionMap();
		var inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyAction.VK_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyAction.VK_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
		
		actionMap.put(KeyAction.VK_LEFT, new KeyAction(KeyAction.VK_LEFT));
		actionMap.put(KeyAction.VK_RIGHT, new KeyAction(KeyAction.VK_RIGHT));
		actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
		actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
		actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
		actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
	}

	private class KeyAction extends AbstractAction {

		final static String VK_LEFT = "VK_LEFT";
		final static String VK_RIGHT = "VK_RIGHT";
		final static String VK_UP = "VK_UP";
		final static String VK_DOWN = "VK_DOWN";
		final static String VK_ENTER = "VK_ENTER";
		final static String VK_SPACE = "VK_SPACE";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE)) {
				setSelectedItem(focusedItem);
			} else if (!styles.isEmpty()) {
				final VisualStyle vs = focusedItem != null ? focusedItem : styles.getFirst();
				final int size = styles.size();
				final int idx = styles.indexOf(vs);
				int newIdx = idx;
				
				if (cmd.equals(VK_RIGHT)) {
					newIdx = idx + 1;
				} else if (cmd.equals(VK_LEFT)) {
					newIdx = idx - 1;
				} else if (cmd.equals(VK_UP)) {
					newIdx = idx - cols < 0 ? idx : idx - cols;
				} else if (cmd.equals(VK_DOWN)) {
					final boolean sameRow = Math.ceil(size/(double)cols) == Math.ceil((idx+1)/(double)cols);
					newIdx = sameRow ? idx : Math.min(size - 1, idx + cols);
				}
				
				if (newIdx != idx)
					setFocus(newIdx);
			}
		}
	}
}