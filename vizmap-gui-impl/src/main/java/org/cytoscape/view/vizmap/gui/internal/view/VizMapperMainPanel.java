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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.CytoPanelComponent;
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
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;

/**
 * VizMapper UI main panel.
 */
@SuppressWarnings("serial")
public class VizMapperMainPanel extends JPanel implements VizMapGUI, DefaultViewPanel, DefaultViewEditor,
														  CytoPanelComponent, PopupMenuListener {

	private static final String TITLE = "Visual Styles";

	private DropDownMenuButton optionsBtn;
	private JPanel stylesPnl;
	private JTabbedPane propertiesPn;
	private final Map<Class<? extends CyIdentifiable>, VisualPropertySheet> vpSheetMap;
	protected VisualStyleDropDownButton stylesBtn;
	protected DefaultComboBoxModel stylesCmbModel;

	protected IconManager iconMgr;
	protected VizMapperUtil vizMapperUtil;
	
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
	public VizMapperMainPanel(final IconManager iconMgr) {
		if (iconMgr == null)
			throw new IllegalArgumentException("'iconMgr' must not be null");
		
		this.iconMgr = iconMgr;
		
		vpSheetMap = new HashMap<Class<? extends CyIdentifiable>, VisualPropertySheet>();
		defViewPanelsMap = new HashMap<String, JPanel>();

		init();
//		initDefaultEditors(); // TODO
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public String getTitle() {
		return TITLE;
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

	@Override
	public JPanel getDefaultViewPanel() { // TODO: deprecate it?
//		return this.defaultViewImagePanel;
		return new JPanel();
	}
	
	@Override
	public DefaultViewEditor getDefaultViewEditor() {
		return this; // TODO: remove interface implementation???
	}
	
	@Override
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Component getDefaultView(final VisualStyle vs) {
		return defViewPanelsMap.get(vs);
	}

	@Override
	public void showEditor(Component parent) {
		// TODO Auto-generated method stub
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
	
	public VisualPropertySheet getCurrentVisualPropertySheet() {
		return (VisualPropertySheet) getPropertiesPn().getSelectedComponent();
	}
	
	public synchronized void updateVisualStyles(final SortedSet<VisualStyle> styles, final CyNetworkView previewNetView,
			final RenderingEngineFactory<CyNetwork> engineFactory) {
		this.previewNetView = previewNetView;
		this.engineFactory = engineFactory;
		
		final VisualStyleDropDownButton vsBtn = getStylesBtn();
		vsBtn.setItems(styles);
//		cmb.removeAllItems();
		defViewPanelsMap.clear();
//		
		for (final VisualStyle vs : styles) {
			final JPanel p = new JPanel();
//			p.setSize(120, 80);
			defViewPanelsMap.put(vs.getTitle(), p);
//			
//			cmb.addItem(vs);
		}
	}
	
	// --- EVENTS ---
	
	@Override
	public void popupMenuCanceled(final PopupMenuEvent e) {
		// disableAllPopup();
	}

	@Override
	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
	}

	// ==[ PRIVATE METHODS ]============================================================================================

// TODO
//	private void initDefaultEditors() {
//		nodeNumericalAttrEditor = editorManager.getDefaultComboBoxEditor("nodeNumericalAttrEditor");
//		edgeNumericalAttrEditor = editorManager.getDefaultComboBoxEditor("edgeNumericalAttrEditor");
//		mappingTypeEditor = editorManager.getDefaultComboBoxEditor("mappingTypeEditor");
//	}

	private void init() {
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
		
		setPreferredSize(new Dimension(380, getPreferredSize().height));
	}
	
	private JPanel getStylesPnl() {
		if (stylesPnl == null) {
			stylesPnl = new JPanel();
			
			final JLabel curStyleLabel = new JLabel("Current Visual Style");
			
			final GroupLayout stylesPanelLayout = new GroupLayout(stylesPnl);
			stylesPnl.setLayout(stylesPanelLayout);
			
			stylesPanelLayout.setHorizontalGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(stylesPanelLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(curStyleLabel)
									.addComponent(getStylesBtn(), 0, 146, Short.MAX_VALUE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(getOptionsBtn(), GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
							.addContainerGap()
					));
			stylesPanelLayout.setVerticalGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(stylesPanelLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(curStyleLabel)
							.addGroup(stylesPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(getStylesBtn(), GroupLayout.PREFERRED_SIZE,
												  GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(getOptionsBtn()))
					));
		}
		
		return stylesPnl;
	}
	
	private JTabbedPane getPropertiesPn() {
		if (propertiesPn == null) {
			propertiesPn = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.WRAP_TAB_LAYOUT);
		}
		
		return propertiesPn;
	}

	VisualStyleDropDownButton getStylesBtn() {
		if (stylesBtn == null) {
			stylesBtn = new VisualStyleDropDownButton();
		}
		
		return stylesBtn;
	}
	
	DropDownMenuButton getOptionsBtn() {
		if (optionsBtn == null) {
			optionsBtn = new DropDownMenuButton(getMainMenu());
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setIcon(iconMgr.getIcon("optionIcon"));
			optionsBtn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
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
		}
		
		return contextPopupMenu;
	}
	
	JMenu getEditSubMenu() {
		if (editSubMenu == null) {
			editSubMenu = new JMenu("Edit");
			
			final JMenuItem removeVpMi = new JMenuItem("Remove Selected Visual Properties");
			removeVpMi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final VisualPropertySheet vpSheet = getCurrentVisualPropertySheet();
					
					for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems())
						vpSheet.setVisible(item, false);
				}
			});
			editSubMenu.add(removeVpMi);
		}
		
		return editSubMenu;
	}
	
	JMenu getMapValueGeneratorsSubMenu() {
		if (mapValueGeneratorsSubMenu == null) {
			mapValueGeneratorsSubMenu = new JMenu("Mapping Value Generators");
		}
		
		return mapValueGeneratorsSubMenu;
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	class VisualStyleDropDownButton extends DropDownMenuButton {

		private static final int ITEM_WIDTH = 120;
		private static final int MAX_COLUMNS = 3;
		private static final int MAX_ROWS = 5;
		
		private int cols;
		
		private JDialog dialog;
		private LinkedList<VisualStyle> items;
		private VisualStyle selectedItem;
		private VisualStyle focusedItem;
		
		private Map<VisualStyle, JPanel> vsPanelMap;
		private Map<String, RenderingEngine<CyNetwork>> engineMap;
		
		public VisualStyleDropDownButton() {
			super(true);
			items = new LinkedList<VisualStyle>();
			vsPanelMap = new HashMap<VisualStyle, JPanel>();
			engineMap = new HashMap<String, RenderingEngine<CyNetwork>>();
			
			setHorizontalAlignment(LEFT);
			
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (items != null && !items.isEmpty())
						showDialog();
				}
			});
		}
		
		public synchronized void setItems(final SortedSet<VisualStyle> Item) {
			items.clear();
			
			if (Item != null)
				items.addAll(Item);
			
			setEnabled(!items.isEmpty());
		}
		
		public synchronized void setSelectedItem(final VisualStyle item) {
			if (item == null || (items != null && items.contains(item) && !item.equals(selectedItem))) {
				setText(item != null ? item.getTitle() : "");
				final VisualStyle oldStyle = selectedItem;
				selectedItem = item;
				firePropertyChange("selectedItem", oldStyle, item);
			}
		}
		
		public synchronized VisualStyle getSelectedItem() {
			return selectedItem;
		}
		
		private void showDialog() {
			dialog = new JDialog(SwingUtilities.getWindowAncestor(VisualStyleDropDownButton.this),
					ModalityType.MODELESS);
			dialog.setUndecorated(true);
			dialog.setBackground(UIManager.getColor("ComboBox.background"));
			
			dialog.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					closeDialog();
				}
			});
			
			cols = MAX_COLUMNS;
// TODO delete			
//			if (items != null && items.size() <= MAX_COLUMNS*MAX_ROWS) {
//				final int size = items.size();
//				int testCols = MAX_COLUMNS;
//				
//				while (testCols >= 2) {
//					cols = testCols;
//					
//					if (size % testCols == 0)
//						break;
//					testCols--;
//				}
//				
//				if (size % cols != 0 && size / cols > MAX_ROWS)
//					cols = 2;
//			}
			
			final GridLayout gridLayout = new GridLayout(0, cols);
			final JPanel mainPnl = new JPanel(gridLayout);
			mainPnl.setBackground(UIManager.getColor("Table.background"));
			setKeyBindings(mainPnl);
			
			if (items != null) {
				for (final VisualStyle vs : items) {
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
//System.out.println(dialog.getX() + "," + dialog.getY()+ " | " + dialog.getSize());
		}
		
		private JPanel createItem(final VisualStyle vs) {
			final Color BG_COLOR = UIManager.getColor("Table.background");
			
			final JPanel panel = new JPanel(new BorderLayout());
			panel.setBackground(BG_COLOR);
			
			// Text label
			final JLabel lbl = new JLabel(vs.getTitle());
			lbl.setHorizontalAlignment(CENTER);
			
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
				RenderingEngine<CyNetwork> engine = engineMap.get(vs.getTitle());
				final JPanel p = defViewPanelsMap.get(vs.getTitle());
				
				if (engine == null && p != null) {
					engine = engineFactory.createRenderingEngine(p, previewNetView);
					engineMap.put(vs.getTitle(), engine);
				}
				
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
			if (index > -1 && index < items.size()) {
				final VisualStyle vs = items.get(index);
				setFocus(vs);
			}
		}
		
		private void updateItems() {
			for (final Map.Entry<VisualStyle, JPanel> entry : vsPanelMap.entrySet())
				updateItem(entry.getValue(), entry.getKey());
		}

		private void updateItem(final JPanel panel, final VisualStyle vs) {
			final Color BG_COLOR = UIManager.getColor("Table.background");
			final Color FG_COLOR = UIManager.getColor("Table.foreground");
			final Color SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
			final Color SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
			final Color BORDER_COLOR = UIManager.getColor("Separator.foreground");
			final Color FOCUS_COLOR = UIManager.getColor("Focus.color");
			
			final Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,  2,  2,  2),
					BorderFactory.createLineBorder(BORDER_COLOR, 1));
			final Border focusBorder = BorderFactory.createLineBorder(FOCUS_COLOR, 3);
			final Border selectionBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1,  1,  1,  1),
										 BorderFactory.createLineBorder(FOCUS_COLOR, 2));
			
			if (vs.equals(focusedItem)) {
				panel.setBorder(focusBorder);
			} else if (vs.equals(selectedItem)) {
				panel.setBorder(selectionBorder);
			} else {
				panel.setBorder(border);
			}
			
			final BorderLayout layout = (BorderLayout) panel.getLayout();
			final JLabel label = (JLabel) layout.getLayoutComponent(BorderLayout.SOUTH);
			
			if (label != null) {
				if (vs.equals(selectedItem)) {
					label.setForeground(SEL_FG_COLOR);
					label.setBackground(SEL_BG_COLOR);
				} else {
					label.setForeground(FG_COLOR);
					label.setBackground(BG_COLOR);
				}
			}
		}

		private synchronized void closeDialog() {
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
				} else if (!items.isEmpty()) {
					final VisualStyle vs = focusedItem != null ? focusedItem : items.getFirst();
					final int size = items.size();
					final int idx = items.indexOf(vs);
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
