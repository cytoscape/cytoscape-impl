package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
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
	protected VisualStyleSelector styleSelector;
	
	/** Menu items under the options button */
	private JPopupMenu mainMenu;
	private PopupMenuGravityTracker mainMenuGravityTracker;
	
	/** Context menu */
	private JPopupMenu contextPopupMenu;
	private JMenu editSubMenu;
	private MenuGravityTracker editSubMenuGravityTracker;
	private JMenu mapValueGeneratorsSubMenu;
	
	private TextIcon icon;
	
	private ServicesUtil servicesUtil;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMainPanel(ServicesUtil servicesUtil) {
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		
		this.servicesUtil = servicesUtil;
		
		vpSheetMap = new HashMap<>();
		
		// We need to build and keep this UI component here because of the API method getDefaultView(),
		// so instead of creating this object only when needed (e.g. before showing the dialog),
		// we keep it to store the panels for the rendered style previews
		styleSelector = new VisualStyleSelector(servicesUtil);
		
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
		if (icon == null)
			icon = new TextIcon(IconManager.ICON_PAINT_BRUSH,
					servicesUtil.get(IconManager.class).getIconFont(14.0f), 16, 16);
		
		return icon;
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
		return styleSelector.getRenderingEngine(getSelectedVisualStyle());
	}
	
	/**
	 * @return The correspondent JPanel which was used to create the rendering engine that then generates
	 * the preview image of the visual style in the Current Style selector.
	 * This JPanel is never displayed in the UI, though.
	 */
	@Override
	@Deprecated
	public Component getDefaultView(VisualStyle vs) {
		return styleSelector.getDefaultView(vs);
	}

	@Override
	@Deprecated
	public void showEditor(Component parent) {
		// Doesn't do anything anymore, since it has been deprecated.
	}
	
	public VisualStyle getSelectedVisualStyle() {
		return styleSelector.getSelectedItem();
	}
	
	public void setSelectedVisualStyle(final VisualStyle style) {
		getStylesBtn().setSelectedItem(style);
	}
	
	public Set<VisualPropertySheet> getVisualPropertySheets() {
		return new HashSet<>(vpSheetMap.values());
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
	
	public void updateVisualStyles(SortedSet<VisualStyle> styles) {
		getStylesBtn().update(styles);
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
		setMinimumSize(new Dimension(420, 240));
		setPreferredSize(new Dimension(420, 385));
		setOpaque(!isAquaLAF());
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(getStylesPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getPropertiesPn(), DEFAULT_SIZE, 280, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getStylesPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getPropertiesPn(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
			
			var layout = new GroupLayout(stylesPnl);
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
			var iconManager = servicesUtil.get(IconManager.class);
			
			optionsBtn = new DropDownMenuButton(getMainMenu(), false);
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setFont(iconManager.getIconFont(12.0f));
			optionsBtn.setText(IconManager.ICON_BARS);
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
				mi.addActionListener(evt -> hideSelectedItems());
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

		private JDialog dialog;
		
		private CloseDialogMenuListener closeDialogMenuListener;
		
		VisualStyleDropDownButton() {
			super(true);
			
			closeDialogMenuListener = new CloseDialogMenuListener();
			setHorizontalAlignment(LEFT);
			
			addActionListener(evt -> {
				if (!styleSelector.isEmpty())
					showDialog();
			});
			
			styleSelector.addPropertyChangeListener("selectedItem", evt -> {
				repaint();
				disposeDialog();
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			});
		}
		
		public void update(SortedSet<VisualStyle> styles) {
			styleSelector.update(styles);
			setEnabled(!styleSelector.isEmpty());
		}
		
		public void setSelectedItem(VisualStyle vs) {
			styleSelector.setSelectedItem(vs);
		}
		
		@Override
		public void repaint() {
			var selectedItem = styleSelector.getSelectedItem();
			setText(selectedItem != null ? selectedItem.getTitle() : "");
			super.repaint();
		}
		
		private void showDialog() {
			setEnabled(false); // Disable the button to prevent accidental repeated clicks
			disposeDialog(); // Just to make sure there will never be more than one dialog
			
			dialog = new JDialog(SwingUtilities.getWindowAncestor(VisualStyleDropDownButton.this),
					ModalityType.MODELESS);
			dialog.setUndecorated(true);
			dialog.setBackground(styleSelector.getBackground());
			
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
			
			var layout = new GroupLayout(dialog.getContentPane());
			dialog.getContentPane().setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(styleSelector, 500, DEFAULT_SIZE, 1060)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(styleSelector, DEFAULT_SIZE, DEFAULT_SIZE, 660)
			);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), null, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					disposeDialog();
				}
			});
			
			var pt = getLocationOnScreen(); 
			dialog.setLocation(pt.x, pt.y);
			dialog.pack();
			dialog.setVisible(true);
			dialog.requestFocus();
		}

		private void disposeDialog() {
			if (dialog != null) {
				dialog.dispose();
				styleSelector.resetFilter();
			}
		}

		private void onDialogDisposed() {
			if (dialog != null)
				dialog = null;
			
			setEnabled(!styleSelector.isEmpty()); // Re-enable the Styles button
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
