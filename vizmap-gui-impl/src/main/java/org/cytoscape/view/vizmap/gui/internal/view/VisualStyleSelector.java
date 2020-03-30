package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
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
	private final int ITEM_BORDER_WIDTH = 3;
	private final int PREF_COLUMNS = 3;
	
	final Color BG_COLOR;
	final Color FG_COLOR;
	final Color SEL_BG_COLOR;
	final Color SEL_FG_COLOR;
	final Color BORDER_COLOR;
	
	private JTextField searchTextField;
	private GridPanel gridPanel;
	private JScrollPane gridsScrollPane;
	
	private int cols;
	
	/** Should store all styles in the current session */
	private LinkedList<VisualStyle> allStyles;
	/** Store filtered in styles; can be null */
	private LinkedList<VisualStyle> filteredStyles;
	
	private VisualStyle selectedItem;
	private VisualStyle focusedItem;
	
	private String titleFilter = "";
	
	private final Map<VisualStyle, JPanel> vsPanelMap;
	private final Map<String, RenderingEngine<CyNetwork>> engineMap;
	
	private final CyNetworkView previewNetView;
	private final Map<String/*visual style name*/, JPanel> defViewPanelsMap;
	private final ServicesUtil servicesUtil;
	
	public VisualStyleSelector(ServicesUtil servicesUtil) {
		super(true);

		this.servicesUtil = servicesUtil;
		
		allStyles = new LinkedList<>();
		vsPanelMap = new HashMap<>();
		engineMap = new HashMap<>();
		defViewPanelsMap = new HashMap<>();
		
		BG_COLOR = UIManager.getColor("Table.background");
		FG_COLOR = UIManager.getColor("Table.foreground");
		SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
		SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
		BORDER_COLOR = UIManager.getColor("Separator.foreground");
		
		previewNetView = createPreviewNetworkView();
		
		init();
		
		setKeyBindings(this);
		setKeyBindings(getGridPanel());
	}
	
	@Override
    public void addNotify() {
    	super.addNotify();
    	getSearchTextField().requestFocusInWindow();
    }
	
	public void update(SortedSet<VisualStyle> styles) {
		allStyles.clear();
		vsPanelMap.clear();
		
		if (styles != null)
			allStyles.addAll(styles);
		
		createPreviewRenderingEngines();
		updateGridPanel();
		
		setFocus(selectedItem);
		setEnabled(!isEmpty());
	}
	
	public void setSelectedItem(VisualStyle vs) {
		if (vs == null || (allStyles != null && allStyles.contains(vs) && !vs.equals(selectedItem))) {
			var oldStyle = selectedItem;
			selectedItem = vs;
			setFocus(selectedItem);
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
		return allStyles == null || allStyles.isEmpty();
	}
	
	public void resetFilter() {
		getSearchTextField().setText("");
	}
	
	public void dispose() {
		selectedItem = null;
		focusedItem = null;
		
		try {
			allStyles.clear();
			filteredStyles = null;
			vsPanelMap.clear();
			engineMap.clear();
			defViewPanelsMap.clear();
			
			previewNetView.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getGridsScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getSearchTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getGridsScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	JTextField getSearchTextField() {
		if (searchTextField == null) {
			searchTextField = new JTextField();
			searchTextField.putClientProperty("JTextField.variant", "search"); // Aqua LAF only
			searchTextField.setToolTipText("Search by style name...");
			
			var debouncer = new DebounceTimer(250);
			
			searchTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent evt) {
					updateTitleFilter();
				}
				@Override
				public void removeUpdate(DocumentEvent evt) {
					updateTitleFilter();
				}
				@Override
				public void changedUpdate(DocumentEvent evt) {
					// Ignore...
				}
				private void updateTitleFilter() {
					debouncer.debounce(() -> setTitleFilter(searchTextField.getText()));
				}
			});
		}
		
		return searchTextField;
	}
	
	JPanel getGridPanel() {
		if (gridPanel == null) {
			gridPanel = new GridPanel();
			gridPanel.setBackground(BG_COLOR);
		}
		
		return gridPanel;
	}
	
	JScrollPane getGridsScrollPane() {
		if (gridsScrollPane == null) {
			gridsScrollPane = new JScrollPane(getGridPanel());
			gridsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			gridsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gridsScrollPane.setBackground(BG_COLOR);
			gridsScrollPane.getViewport().setBackground(BG_COLOR);
		}
		
		return gridsScrollPane;
	}
	
	private void setTitleFilter(String s) {
		if (s == null)
			s = "";
		
		s = s.trim().toLowerCase();
		
		if (!s.equals(titleFilter)) {
			titleFilter = s;
			filterStyles();
		}
	}
	
	private void createPreviewRenderingEngines() {
		if (allStyles != null && previewNetView != null) {
			defViewPanelsMap.clear();
			engineMap.clear();
			
			var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
			var engineFactory = vmProxy.getRenderingEngineFactory(previewNetView);
			
			for (var vs : allStyles) {
				var p = new JPanel();
				defViewPanelsMap.put(vs.getTitle(), p);
				
				var engine = engineFactory.createRenderingEngine(p, previewNetView);
				engineMap.put(vs.getTitle(), engine);
			}
		}
	}
	
	private JPanel createItem(VisualStyle vs) {
		var panel = new JPanel(new BorderLayout());
		panel.setBackground(BG_COLOR);
		panel.setFocusable(true);
		
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
			public void mouseEntered(MouseEvent e) {
				setFocus(vs);
			}
			@Override
			public void mouseClicked(MouseEvent e) {
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
				var bgColor = bgPaint instanceof Color ? (Color) bgPaint : BG_COLOR;
				iconLbl.setBackground(bgColor);
				
				panel.add(iconLbl, BorderLayout.CENTER);
			}
		}
		
		vsPanelMap.put(vs, panel);
		
		return panel;
	}
	
	private void filterStyles() {
		filteredStyles = titleFilter == null || titleFilter.isBlank() ? null : new LinkedList<>();
		
		if (filteredStyles != null) {
			for (var vs : allStyles) {
				if (vs.getTitle().toLowerCase().contains(titleFilter))
					filteredStyles.add(vs);
			}
		}
		
		updateGridPanel();
		
		getGridPanel().invalidate();
		getGridPanel().repaint();
		
		firePropertyChange("filterChanged", false, true);
	}
	
	private void setFocus(VisualStyle vs) {
		focusedItem = vs;
		updateItems();
	}
	
	private void setFocus(int index) {
		var styles = filteredStyles != null ? filteredStyles : allStyles;
		
		if (index > -1 && index < styles.size()) {
			var vs = styles.get(index);
			setFocus(vs);
		}
	}
	
	private void updateGridPanel() {
		getGridPanel().removeAll();
		var styles = filteredStyles != null ? filteredStyles : allStyles;
		
		if (styles.isEmpty())
			return;
		
		var itemWidth = ITEM_WIDTH + 2 * ITEM_BORDER_WIDTH;
		var size = getGridPanel().getSize();
		var width = size != null ? size.width : 0;
		cols = width <= 0 ? PREF_COLUMNS : calculateColumns(itemWidth, width);
		
		var rows = calculateRows(styles.size(), cols);
		
		getGridPanel().setLayout(new GridLayout(rows, cols));
		
		for (var vs : styles) {
			var itemPnl = vsPanelMap.get(vs);
			
			if (itemPnl == null) {
				itemPnl = createItem(vs);
//				setSelectionKeyBindings(itemPnl);
			}
			
			if (!isFilteredOut(vs))
				getGridPanel().add(itemPnl);
		}
		
		if (styles.size() < cols) {
			var diff = cols - styles.size();
			
			for (int i = 0; i < diff; i++) {
				var filler = new JPanel();
				filler.setBackground(BG_COLOR);
				getGridPanel().add(filler);
			}
		}
		
		getGridPanel().updateUI();
	}
	
	private boolean isFilteredOut(VisualStyle vs) {
		return false;
	}
	
	private void updateItems() {
		for (var entry : vsPanelMap.entrySet())
			updateItem(entry.getValue(), entry.getKey());
	}

	private void updateItem(JPanel panel, VisualStyle vs) {
		var w = 1;
		
		if (vs.equals(focusedItem)) {
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(w,  w,  w,  w),
					BorderFactory.createLineBorder(SEL_BG_COLOR, ITEM_BORDER_WIDTH - w));
			panel.setBorder(border);
		} else if (vs.equals(selectedItem)) {
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(w,  w,  w,  w),
					BorderFactory.createLineBorder(SEL_BG_COLOR, ITEM_BORDER_WIDTH - w));
			panel.setBorder(border);
		} else {
			// not selected and not focused...
			w = ITEM_BORDER_WIDTH - w; // invert...
			
			var border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(w,  w,  w,  w),
					BorderFactory.createLineBorder(BORDER_COLOR, ITEM_BORDER_WIDTH - w));
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
	
	private static int calculateColumns(int itemWidth, int gridWidth) {
		return itemWidth > 0 ? Math.floorDiv(gridWidth, itemWidth) : 0;
	}
	
	private static int calculateRows(int total, int cols) {
		return (int) Math.round(Math.ceil((float)total / (float)cols));
	}
	
	private CyNetworkView createPreviewNetworkView() {
		// Create dummy view first
		var net = servicesUtil.get(CyNetworkFactory.class).createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		var source = net.addNode();
		var target = net.addNode();

		net.getRow(source).set(CyNetwork.NAME, "Source");
		net.getRow(target).set(CyNetwork.NAME, "Target");

		var edge = net.addEdge(source, target, true);
		net.getRow(edge).set(CyNetwork.NAME, "Source (interaction) Target");

		net.getRow(net).set(CyNetwork.NAME, "Default Appearance");
		var view = servicesUtil.get(CyNetworkViewFactory.class).createNetworkView(net);

		// Set node locations
		view.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		view.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 0d);
		view.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 150d);
		view.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 20d);
		
		return view;
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

	private class GridPanel extends JPanel implements Scrollable {

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return defViewPanelsMap == null || defViewPanelsMap.isEmpty();
		}
	}

// TODO
//	private class KeyAction extends AbstractAction {
//
//		final static String VK_CTRL_A = "VK_CTRL_A";
//		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
//		
//		KeyAction(final String actionCommand) {
//			putValue(ACTION_COMMAND_KEY, actionCommand);
//		}
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//			
//			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
//					!NetworkViewGrid.this.isVisible() || isEmpty())
//				return; // We don't want to steal the key event from these components
//			
//			final String cmd = e.getActionCommand();
//			
//			if (cmd.equals(VK_CTRL_A))
//				selectAll();
//			else if (cmd.equals(VK_CTRL_SHIFT_A))
//				deselectAll();
//		}
//	}
	
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
			} else if (!allStyles.isEmpty()) {
				final VisualStyle vs = focusedItem != null ? focusedItem : allStyles.getFirst();
				final int size = allStyles.size();
				final int idx = allStyles.indexOf(vs);
				int newIdx = idx;
				
				if (cmd.equals(VK_RIGHT)) {
					newIdx = idx + 1;
				} else if (cmd.equals(VK_LEFT)) {
					newIdx = idx - 1;
				} else if (cmd.equals(VK_UP)) {
					newIdx = idx - cols < 0 ? idx : idx - cols;
				} else if (cmd.equals(VK_DOWN)) {
					final boolean sameRow = Math.ceil(size / (double) cols) == Math.ceil((idx + 1) / (double) cols);
					newIdx = sameRow ? idx : Math.min(size - 1, idx + cols);
				}
				
				if (newIdx != idx)
					setFocus(newIdx);
			}
		}
	}
}