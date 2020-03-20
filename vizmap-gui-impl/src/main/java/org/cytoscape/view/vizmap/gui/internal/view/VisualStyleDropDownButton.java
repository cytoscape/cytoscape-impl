package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
public class VisualStyleDropDownButton extends DropDownMenuButton {

	final int ITEM_WIDTH = 120;
	final int MAX_COLUMNS = 3;
	final int MAX_ROWS = 5;
	
	final Color BG_COLOR;
	final Color FG_COLOR;
	final Color SEL_BG_COLOR;
	final Color SEL_FG_COLOR;
	final Color BORDER_COLOR;
	
	private int cols;
	
	private JDialog dialog;
	private LinkedList<VisualStyle> styles;
	private VisualStyle selectedItem;
	private VisualStyle focusedItem;
	
	private Map<VisualStyle, JPanel> vsPanelMap;
	private Map<String, RenderingEngine<CyNetwork>> engineMap;
	
	private CloseDialogMenuListener closeDialogMenuListener;
	
	private CyNetworkView previewNetView;
	private final Map<String/*visual style name*/, JPanel> defViewPanelsMap;
	private final ServicesUtil servicesUtil;
	
	public VisualStyleDropDownButton(
			Map<String/*visual style name*/, JPanel> defViewPanelsMap,
			ServicesUtil servicesUtil
	) {
		super(true);
		
		this.defViewPanelsMap = defViewPanelsMap;
		this.servicesUtil = servicesUtil;
		
		styles = new LinkedList<>();
		vsPanelMap = new HashMap<>();
		engineMap = new HashMap<>();
		closeDialogMenuListener = new CloseDialogMenuListener();
		
		setHorizontalAlignment(LEFT);
		
		BG_COLOR = UIManager.getColor("TextField.background");
		FG_COLOR = UIManager.getColor("TextField.foreground");
		SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
		SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
		BORDER_COLOR = UIManager.getColor("Separator.foreground");
		
		addActionListener(evt -> {
			if (styles != null && !styles.isEmpty())
				showDialog();
		});
	}
	
	public void update(SortedSet<VisualStyle> styles, CyNetworkView previewNetView) {
		this.previewNetView = previewNetView;
		this.styles.clear();
		
		if (styles != null)
			this.styles.addAll(styles);
		
		createPreviewRenderingEngines();
		setEnabled(!this.styles.isEmpty());
	}
	
	public void setSelectedItem(final VisualStyle vs) {
		if (vs == null || (styles != null && styles.contains(vs) && !vs.equals(selectedItem))) {
			final VisualStyle oldStyle = selectedItem;
			selectedItem = vs;
			repaint();
			firePropertyChange("selectedItem", oldStyle, vs);
		}
	}
	
	public VisualStyle getSelectedItem() {
		return selectedItem;
	}
	
	public RenderingEngine<CyNetwork> getRenderingEngine(final VisualStyle vs) {
		return vs != null ? engineMap.get(vs.getTitle()) : null;
	}
	
	@Override
	public void repaint() {
		setText(selectedItem != null ? selectedItem.getTitle() : "");
		super.repaint();
	}
	
	private void createPreviewRenderingEngines() {
		if (styles != null && previewNetView != null) {
			defViewPanelsMap.clear();
			engineMap.clear();
			
			var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
			var engineFactory = vmProxy.getRenderingEngineFactory(previewNetView);
			
			for (final VisualStyle vs : styles) {
				final JPanel p = new JPanel();
				defViewPanelsMap.put(vs.getTitle(), p);
				
				final RenderingEngine<CyNetwork> engine = engineFactory.createRenderingEngine(p, previewNetView);
				engineMap.put(vs.getTitle(), engine);
			}
		}
	}
	
	private void showDialog() {
		setEnabled(false); // Disable the button to prevent accidental repeated clicks
		disposeDialog(); // Just to make sure there will never be more than one dialog
			
		dialog = new JDialog(SwingUtilities.getWindowAncestor(VisualStyleDropDownButton.this),
				ModalityType.MODELESS);
		dialog.setUndecorated(true);
		dialog.setBackground(BG_COLOR);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				disposeDialog();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				onDialogDisposed();
			
				if (LookAndFeelUtil.isAquaLAF())
					removeMenuListeners();
			}
		});
		
		// Opening a Mac/Aqua menu does not trigger a Window Deactivated event on the Style dialog!
		if (LookAndFeelUtil.isAquaLAF())
			addMenuListeners();
		
		cols = MAX_COLUMNS;
		
		final GridLayout gridLayout = new GridLayout(0, cols);
		final JPanel mainPnl = new JPanel(gridLayout);
		mainPnl.setBackground(BG_COLOR);
		setKeyBindings(mainPnl);
		
		if (styles != null) {
			for (final VisualStyle vs : styles) {
				final JPanel itemPnl = createItem(vs);
				mainPnl.add(itemPnl);
			}
		}
		
		setFocus(selectedItem);
		
		final JScrollPane scr = new JScrollPane(mainPnl);
		scr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		final GroupLayout layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(scr, 500, DEFAULT_SIZE, 1060)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scr, DEFAULT_SIZE, DEFAULT_SIZE, 660)
		);
		
		dialog.getContentPane().add(scr);
		
		final Point pt = getLocationOnScreen(); 
		dialog.setLocation(pt.x, pt.y);
		dialog.pack();
		dialog.setVisible(true);
		dialog.requestFocus();
	}

	private JPanel createItem(final VisualStyle vs) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BG_COLOR);
		
		// Text label
		final JLabel lbl = new JLabel(vs.getTitle());
		lbl.setHorizontalAlignment(CENTER);
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
				disposeDialog();
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
				final Image img = engine.createImage(ITEM_WIDTH, 68);
				final ImageIcon icon = new ImageIcon(img); 
				final JLabel iconLbl = new JLabel(icon);
				iconLbl.setOpaque(true);
				final Paint bgPaint = vs.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
				final Color bgColor = bgPaint instanceof Color ? (Color)bgPaint : BG_COLOR;
				iconLbl.setBackground(bgColor);
				
				panel.add(iconLbl, BorderLayout.CENTER);
			}
		}
		
		vsPanelMap.put(vs, panel);
		
		return panel;
	}
	
	private void setFocus(final VisualStyle vs) {
		focusedItem = vs;
		updateItems();
	}
	
	private void setFocus(final int index) {
		if (index > -1 && index < styles.size()) {
			final VisualStyle vs = styles.get(index);
			setFocus(vs);
		}
	}
	
	private void updateItems() {
		for (final Map.Entry<VisualStyle, JPanel> entry : vsPanelMap.entrySet())
			updateItem(entry.getValue(), entry.getKey());
	}

	private void updateItem(final JPanel panel, final VisualStyle vs) {
		if (vs.equals(focusedItem)) {
			final Border border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1,  1,  1,  1),
					BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
			panel.setBorder(border);
		} else if (vs.equals(selectedItem)) {
			final Border border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1,  1,  1,  1),
					BorderFactory.createLineBorder(SEL_BG_COLOR, 2));
			panel.setBorder(border);
		} else {
			final Border border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(2,  2,  2,  2),
					BorderFactory.createLineBorder(BORDER_COLOR, 1));
			panel.setBorder(border);
		}
		
		final BorderLayout layout = (BorderLayout) panel.getLayout();
		final JLabel label = (JLabel) layout.getLayoutComponent(BorderLayout.SOUTH);
		
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
	
	private void disposeDialog() {
		if (dialog != null)
			dialog.dispose();
	}

	private void onDialogDisposed() {
		if (dialog != null) {
			vsPanelMap.clear();
			dialog = null;
		}
		
		setEnabled(!styles.isEmpty()); // Re-enable the Styles button
	}
	
	private void addMenuListeners() {
		final JMenuBar menuBar = servicesUtil.get(CySwingApplication.class).getJMenuBar();
		
		if (menuBar != null) {
			for (int i = 0; i < menuBar.getMenuCount(); i++)
				menuBar.getMenu(i).addMenuListener(closeDialogMenuListener);
		}
	}
	
	private void removeMenuListeners() {
		final JMenuBar menuBar = servicesUtil.get(CySwingApplication.class).getJMenuBar();
		
		if (menuBar != null) {
			for (int i = 0; i < menuBar.getMenuCount(); i++)
				menuBar.getMenu(i).removeMenuListener(closeDialogMenuListener);
		}
	}
	
	private void setKeyBindings(final JPanel panel) {
		final ActionMap actionMap = panel.getActionMap();
		final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyAction.VK_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyAction.VK_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KeyAction.VK_ESCAPE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
		
		actionMap.put(KeyAction.VK_LEFT, new KeyAction(KeyAction.VK_LEFT));
		actionMap.put(KeyAction.VK_RIGHT, new KeyAction(KeyAction.VK_RIGHT));
		actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
		actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
		actionMap.put(KeyAction.VK_ESCAPE, new KeyAction(KeyAction.VK_ESCAPE));
		actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
		actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
	}

	private class KeyAction extends AbstractAction {

		final static String VK_LEFT = "VK_LEFT";
		final static String VK_RIGHT = "VK_RIGHT";
		final static String VK_UP = "VK_UP";
		final static String VK_DOWN = "VK_DOWN";
		final static String VK_ESCAPE = "VK_ESCAPE";
		final static String VK_ENTER = "VK_ENTER";
		final static String VK_SPACE = "VK_SPACE";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_ESCAPE)) {
				disposeDialog();
			} else if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE)) {
				setSelectedItem(focusedItem);
				disposeDialog();
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
	
	private class CloseDialogMenuListener implements MenuListener {

		@Override
		public void menuSelected(MenuEvent e) {
			disposeDialog();
		}

		@Override
		public void menuDeselected(MenuEvent e) {
			// Ignore...
		}

		@Override
		public void menuCanceled(MenuEvent e) {
			// Ignore...
		}
	}
}