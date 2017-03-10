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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.MenuGravityTracker;
import org.cytoscape.util.swing.PopupMenuGravityTracker;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

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

	/** Menu items under the options button */
	private JPopupMenu mainMenu;
	private PopupMenuGravityTracker mainMenuGravityTracker;
	
	/** Context menu */
	private JPopupMenu contextPopupMenu;
	private JMenu editSubMenu;
	private MenuGravityTracker editSubMenuGravityTracker;
	private JMenu mapValueGeneratorsSubMenu;
	
	private Map<String/*visual style name*/, JPanel> defViewPanelsMap;

	private CyNetworkView previewNetView;
	private RenderingEngineFactory<CyNetwork> engineFactory; // TODO refactor
	private ServicesUtil servicesUtil;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	/**
	 * Create new instance of VizMapperMainPanel object. GUI layout is handled
	 * by abstract class.
	 */
	public VizMapperMainPanel(final ServicesUtil servicesUtil) {
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		
		this.servicesUtil = servicesUtil;
		
		vpSheetMap = new HashMap<>();
		defViewPanelsMap = new HashMap<>();

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
	
	public void hideSelectedItems() {
		final VisualPropertySheet vpSheet = getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems())
				vpSheet.setVisible(item, false);
		}
	}
	
	/**
	 * Add the menu item to the "Options" menu.
	 * @param menuItem
	 * @param gravity
	 * @param insertSeparatorBefore
	 * @param insertSeparatorAfter
	 */
	public void addOption(final JMenuItem menuItem, final double gravity, boolean insertSeparatorBefore,
			boolean insertSeparatorAfter) {
		addMenuItem(getMainMenuGravityTracker(), menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		if (menuItem.getAction() instanceof CyAction)
			getMainMenu().addPopupMenuListener((CyAction)menuItem.getAction());
	}

	public void removeOption(final JMenuItem menuItem) {
		getMainMenuGravityTracker().removeComponent(menuItem);
		
		if (menuItem.getAction() instanceof CyAction)
			getMainMenu().removePopupMenuListener((CyAction)menuItem.getAction());
	}
	
	/**
	 * Add the menu item under the "Edit" context sub-menu.
	 * @param menuItem
	 * @param gravity
	 * @param insertSeparatorBefore
	 * @param insertSeparatorAfter
	 */
	public void addContextMenuItem(final JMenuItem menuItem, final double gravity, boolean insertSeparatorBefore,
			boolean insertSeparatorAfter) {
		addMenuItem(getEditSubMenuGravityTracker(), menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().addPopupMenuListener((CyAction)menuItem.getAction());
	}
	
	public void removeContextMenuItem(final JMenuItem menuItem) {
		getEditSubMenuGravityTracker().removeComponent(menuItem);
		
		if (menuItem.getAction() instanceof CyAction)
			getContextMenu().removePopupMenuListener((CyAction)menuItem.getAction());
	}

	// ==[ PRIVATE METHODS ]============================================================================================

	private void init() {
		setMinimumSize(new Dimension(420, getMinimumSize().height));
		setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(getStylesPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPn(), DEFAULT_SIZE, 280, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getStylesPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPropertiesPn(), DEFAULT_SIZE, 510, Short.MAX_VALUE)
				)
		);
	}
	
	private JPanel getStylesPnl() {
		if (stylesPnl == null) {
			stylesPnl = new JPanel();
			stylesPnl.setOpaque(!isAquaLAF());
			
			// TODO: For some reason, the Styles button is naturally taller than the Options one on Nimbus and Windows.
			//       Let's force it to have the same height.
			getStylesBtn().setPreferredSize(
					new Dimension(getStylesBtn().getPreferredSize().width, getOptionsBtn().getPreferredSize().height));
			
			final GroupLayout layout = new GroupLayout(stylesPnl);
			stylesPnl.setLayout(layout);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getStylesBtn(), 0, 146, Short.MAX_VALUE)
					.addComponent(getOptionsBtn(), PREFERRED_SIZE, 64, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
					.addComponent(getStylesBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getOptionsBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
			final IconManager iconManager = servicesUtil.get(IconManager.class);
			
			optionsBtn = new DropDownMenuButton(getMainMenu(), false);
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setFont(iconManager.getIconFont(11.0f));
			optionsBtn.setText(IconManager.ICON_CARET_DOWN);
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
	
	private PopupMenuGravityTracker getMainMenuGravityTracker() {
		if (mainMenuGravityTracker == null) {
			mainMenuGravityTracker = new PopupMenuGravityTracker(getMainMenu());
		}
		
		return mainMenuGravityTracker;
	}
	
	private MenuGravityTracker getEditSubMenuGravityTracker() {
		if (editSubMenuGravityTracker == null) {
			editSubMenuGravityTracker = new MenuGravityTracker(getEditSubMenu());
		}
		
		return editSubMenuGravityTracker;
	}
	
	private void addMenuItem(final GravityTracker gravityTracker, final JMenuItem menuItem, final double gravity,
			boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		if (insertSeparatorBefore)
			gravityTracker.addMenuSeparator(gravity - .0001);
		
		gravityTracker.addMenuItem(menuItem, gravity);
		
		if (insertSeparatorAfter)
			gravityTracker.addMenuSeparator(gravity + .0001);
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
		
		private CloseDialogMenuListener closeDialogMenuListener;
		
		public VisualStyleDropDownButton() {
			super(true);
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
			if (engineFactory != null && previewNetView != null) {
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
}
