package org.cytoscape.view.vizmap.gui.internal.view;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager.CyFont;

/**
 * VizMapper UI main panel.
 */
@SuppressWarnings("serial")
public class VizMapperMainPanel extends JPanel implements VizMapGUI, DefaultViewPanel, DefaultViewEditor,
														  CytoPanelComponent2 {

	private static final String TITLE = "Style";
	private static final String ID = "org.cytoscape.Style";

	private DropDownMenuButton optionsBtn;
	private JPanel stylesPnl;
	private JTabbedPane propertiesPn;
	private final Map<Class<? extends CyIdentifiable>, VisualPropertySheet> vpSheetMap;
	protected VisualStyleDropDownButton stylesBtn;
	protected DefaultComboBoxModel stylesCmbModel;

	private final ThemeManager themeMgr;
	
	/** Menu items under the options button */
	private JPopupMenu mainMenu;
	
	/** Context menu */
	private JPopupMenu contextPopupMenu;
	private JMenu editSubMenu;
	private JMenu mapValueGeneratorsSubMenu;
	
	private Map<String/*visual style name*/, JPanel> defViewPanelsMap;

	private CyNetworkView previewNetView;
	private RenderingEngineFactory<CyNetwork> engineFactory; // TODO refactor
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Create new instance of VizMapperMainPanel object. GUI layout is handled
	 * by abstract class.
	 */
	public VizMapperMainPanel(final ThemeManager themeMgr) {
		if (themeMgr == null)
			throw new IllegalArgumentException("'themeMgr' must not be null");
		
		this.themeMgr = themeMgr;
		
		vpSheetMap = new HashMap<Class<? extends CyIdentifiable>, VisualPropertySheet>();
		defViewPanelsMap = new HashMap<String, JPanel>();

		init();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * Dummy panel that is used to prevent NullPointerExceptions for clients of the
	 * deprecated method {@link #getDefaultViewPanel()}.
	 */
	private JPanel defViewPanel = new JPanel();
	
	@Override
	@Deprecated
	public JPanel getDefaultViewPanel() {
		return defViewPanel;
	}
	
	@Override
	@Deprecated
	public DefaultViewEditor getDefaultViewEditor() {
		return this;
	}
	
	@Override
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		return getStylesBtn().getRenderingEngine(getSelectedVisualStyle());
	}
	
	/**
	 * @return The correspondent JPanel which was used to create the rendering engine that then generates
	 * the preview image of the visual style in the Current Style selector.
	 * This JPanel is never displayed in the UI, though.
	 */
	@Override
	@Deprecated
	public Component getDefaultView(final VisualStyle vs) {
		return defViewPanelsMap.get(vs);
	}

	@Override
	@Deprecated
	public void showEditor(Component parent) {
		// Doesn't do anything anymore, since it has been deprecated.
	}
	
	public VisualStyle getSelectedVisualStyle() {
		return getStylesBtn().getSelectedItem();
	}
	
	public void setSelectedVisualStyle(final VisualStyle style) {
		getStylesBtn().setSelectedItem(style);
	}
	
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return new HashSet<VisualPropertySheet>(vpSheetMap.values());
	}
	
	public VisualPropertySheet getVisualPropertySheet(final Class<? extends CyIdentifiable> targetDataType) {
		return vpSheetMap.get(targetDataType);
	}
	
	public void addVisualPropertySheet(final VisualPropertySheet sheet) {
		if (sheet == null)
			return;
		
		final Class<? extends CyIdentifiable> type = sheet.getModel().getTargetDataType();
		
		if (vpSheetMap.containsKey(type))
			getPropertiesPn().remove(vpSheetMap.get(type));
		
		getPropertiesPn().addTab(sheet.getModel().getTitle(), sheet);
		vpSheetMap.put(type, sheet);
	}
	
	public void removeAllVisualPropertySheets() {
		getPropertiesPn().removeAll();
		vpSheetMap.clear();
	}
	
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return (VisualPropertySheet) getPropertiesPn().getSelectedComponent();
	}
	
	public void setSelectedVisualPropertySheet(final VisualPropertySheet sheet) {
		if (sheet != null) {
			final int idx = getPropertiesPn().indexOfTab(sheet.getModel().getTitle());
			
			if (idx != -1)
				getPropertiesPn().setSelectedIndex(idx);
		}
	}
	
	public void updateVisualStyles(final SortedSet<VisualStyle> styles, final CyNetworkView previewNetView,
			final RenderingEngineFactory<CyNetwork> engineFactory) {
		this.previewNetView = previewNetView;
		this.engineFactory = engineFactory;
		
		final VisualStyleDropDownButton vsBtn = getStylesBtn();
		vsBtn.setItems(styles);
	}

	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		setMinimumSize(new Dimension(420, getMinimumSize().height));
		setOpaque(false);
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(getStylesPnl(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPn(), GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addComponent(getStylesPnl(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(getPropertiesPn(), GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)));
	}
	
	private JPanel getStylesPnl() {
		if (stylesPnl == null) {
			stylesPnl = new JPanel();
			stylesPnl.setOpaque(false);
			
			final GroupLayout stylesPanelLayout = new GroupLayout(stylesPnl);
			stylesPnl.setLayout(stylesPanelLayout);
			
			stylesPanelLayout.setHorizontalGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(stylesPanelLayout.createSequentialGroup()
							.addComponent(getStylesBtn(), 0, 146, Short.MAX_VALUE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(getOptionsBtn(), GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
					));
			stylesPanelLayout.setVerticalGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(getStylesBtn(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(getOptionsBtn())
					);
		}
		
		return stylesPnl;
	}
	
	JTabbedPane getPropertiesPn() {
		if (propertiesPn == null) {
			propertiesPn = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.WRAP_TAB_LAYOUT);
		}
		
		return propertiesPn;
	}

	VisualStyleDropDownButton getStylesBtn() {
		if (stylesBtn == null) {
			stylesBtn = new VisualStyleDropDownButton();
			stylesBtn.setToolTipText("Current Style");
		}
		
		return stylesBtn;
	}
	
	DropDownMenuButton getOptionsBtn() {
		if (optionsBtn == null) {
			optionsBtn = new DropDownMenuButton(getMainMenu(), false);
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setFont(themeMgr.getFont(CyFont.FONTAWESOME_FONT).deriveFont(11.0f));
			optionsBtn.setText("\uF0D7"); // icon-caret-down
		}
		
		return optionsBtn;
	}
	
	JPopupMenu getMainMenu() {
		if (mainMenu == null) {
			mainMenu = new JPopupMenu();
		}
		
		return mainMenu;
	}
	
	JPopupMenu getContextMenu() {
		if (contextPopupMenu == null) {
			contextPopupMenu = new JPopupMenu();
			contextPopupMenu.add(getEditSubMenu());
			contextPopupMenu.add(getMapValueGeneratorsSubMenu());
			contextPopupMenu.add(new JSeparator());
			
			{
				final JMenuItem mi = new JMenuItem("Hide Selected Visual Properties");
				mi.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						hideSelectedItems();
					}
				});
				contextPopupMenu.add(mi);
			}
		}
		
		return contextPopupMenu;
	}
	
	JMenu getEditSubMenu() {
		if (editSubMenu == null) {
			editSubMenu = new JMenu("Edit");
		}
		
		return editSubMenu;
	}
	
	JMenu getMapValueGeneratorsSubMenu() {
		if (mapValueGeneratorsSubMenu == null) {
			mapValueGeneratorsSubMenu = new JMenu("Mapping Value Generators");
		}
		
		return mapValueGeneratorsSubMenu;
	}
	
	private void hideSelectedItems() {
		final VisualPropertySheet vpSheet = getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems())
				vpSheet.setVisible(item, false);
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	class VisualStyleDropDownButton extends DropDownMenuButton {

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
		
		public VisualStyleDropDownButton() {
			super(true);
			styles = new LinkedList<VisualStyle>();
			vsPanelMap = new HashMap<VisualStyle, JPanel>();
			engineMap = new HashMap<String, RenderingEngine<CyNetwork>>();
			
			setHorizontalAlignment(LEFT);
			
			final JList list = new JList();
			BG_COLOR = list.getBackground();
			FG_COLOR = list.getForeground();
			SEL_BG_COLOR = list.getSelectionBackground();
			SEL_FG_COLOR = list.getSelectionForeground();
			Separator sep = new Separator();
			BORDER_COLOR = sep.getForeground();
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (styles != null && !styles.isEmpty())
						showDialog();
				}
			});
		}
		
		public void setItems(final SortedSet<VisualStyle> styles) {
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
			if (styles != null) {
				defViewPanelsMap.clear();
				engineMap.clear();
				
				for (final VisualStyle vs : styles) {
					final JPanel p = new JPanel();
					defViewPanelsMap.put(vs.getTitle(), p);
					
					final RenderingEngine<CyNetwork> engine = engineFactory.createRenderingEngine(p, previewNetView);
					engineMap.put(vs.getTitle(), engine);
				}
			}
		}
		
		private void showDialog() {
			dialog = new JDialog(SwingUtilities.getWindowAncestor(VisualStyleDropDownButton.this),
					ModalityType.MODELESS);
			dialog.setUndecorated(true);
			dialog.setBackground(BG_COLOR);
			
			dialog.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					closeDialog();
				}
			});
			
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
			scr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			dialog.add(scr);
			
			final Point pt = getLocationOnScreen(); 
			dialog.setLocation(pt.x, pt.y);
			dialog.pack();
			// TODO set maximum size
//			dialog.setSize(new Dimension(dialog.getSize().width, 390));
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
					closeDialog();
				}
			});
			
			// Preview image
			if (engineFactory != null && previewNetView != null) {
				// TODO review
				final RenderingEngine<CyNetwork> engine = getRenderingEngine(vs);
				
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

		private void closeDialog() {
			if (dialog != null) {
				vsPanelMap.clear();
				dialog.dispose();
				dialog = null;
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
					closeDialog();
				} else if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE)) {
					setSelectedItem(focusedItem);
					closeDialog();
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
}
