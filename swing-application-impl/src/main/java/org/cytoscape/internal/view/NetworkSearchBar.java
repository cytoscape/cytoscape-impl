package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.view.util.ViewUtil.DEFAULT_PROVIDER_PROP_KEY;
import static org.cytoscape.internal.view.util.ViewUtil.getViewProperty;
import static org.cytoscape.internal.view.util.ViewUtil.hasVisibleOwnedWindows;
import static org.cytoscape.internal.view.util.ViewUtil.setViewProperty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.internal.util.RandomImage;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

@SuppressWarnings("serial")
public class NetworkSearchBar extends JPanel {

	private static final int ICON_SIZE = 32;
	private static final String DEF_SEARCH_TEXT = "Type your query here...";
	
	private JButton providersButton;
	private JButton providerSelectorButton;
	private JPanel contentPane;
	private JTextField searchTextField;
	private JToggleButton optionsButton;
	private JButton searchButton;
	
	private JPopupMenu providersPopup;
	private ProvidersPanel providersPanel;
	
	private OptionsDialog optionsDialog;
	
	private final EmptyIcon emptyIcon = new EmptyIcon(ICON_SIZE, ICON_SIZE);
	private final Map<NetworkSearchTaskFactory, Icon> providerIcons = new HashMap<>();
	
	private final TreeSet<NetworkSearchTaskFactory> providers;
	
	/** This should only be set when the user explicitly selects a provider */
	private NetworkSearchTaskFactory selectedProvider;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkSearchBar(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		var collator = Collator.getInstance();
		providers = new TreeSet<>((NetworkSearchTaskFactory o1, NetworkSearchTaskFactory o2) -> {
			return collator.compare(o1.getName(), o2.getName());
		});
		
		init();
	}
	
	private void setDefaultProvider(NetworkSearchTaskFactory suggestedProvider) {
		if (suggestedProvider == null || !providers.contains(suggestedProvider)) {
			// Check if there is a CyProperty for this
			String id = getViewProperty(DEFAULT_PROVIDER_PROP_KEY, serviceRegistrar);
			
			if (id != null)
				suggestedProvider = getProvider(id);
		}
		
		if (suggestedProvider != null) // Update the CyProperty
			setViewProperty(DEFAULT_PROVIDER_PROP_KEY, suggestedProvider.getId(), serviceRegistrar);
	}
	
	private NetworkSearchTaskFactory getDefaultProvider() {
		String id = getViewProperty(DEFAULT_PROVIDER_PROP_KEY, serviceRegistrar);
		
		return getProvider(id);
	}
	
	public void setSelectedProvider(NetworkSearchTaskFactory newValue) {
		if (newValue != selectedProvider) {
			NetworkSearchTaskFactory oldValue = getSelectedProvider(); // Get the actual "current" provider
			selectedProvider = newValue;
			// Save the last selected provider now, so it can be selected by default when Cytoscape restarts
			setDefaultProvider(newValue);
			
			if (newValue != oldValue)
				firePropertyChange("selectedProvider", oldValue, newValue);
		}
	}
	
	/**
	 * If there is no previously selected provider (by the user),
	 * it returns the preferred one or the first one in the list.
	 */
	public NetworkSearchTaskFactory getSelectedProvider() {
		if (selectedProvider != null)
			return selectedProvider;
		
		NetworkSearchTaskFactory defProvider = getDefaultProvider();
		
		if (defProvider != null)
			return defProvider;
		
		if (!providers.isEmpty())
			return providers.first();
		
		return null;
	}
	
	public NetworkSearchTaskFactory getProvider(String id) {
		for (NetworkSearchTaskFactory tf : providers) {
			if (tf.getId().equals(id))
				return tf;
		}
		
		return null;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getProvidersButton().setEnabled(enabled);
		getProviderSelectorButton().setEnabled(enabled);
		getSearchTextField().setEnabled(enabled);
		getOptionsButton().setEnabled(enabled);
		getSearchButton().setEnabled(enabled);
	}
	
	void update(Collection<NetworkSearchTaskFactory> newProviders) {
		NetworkSearchTaskFactory oldSelected = getSelectedProvider();
		providers.clear();
		providerIcons.clear();
		
		if (newProviders != null) {
			providers.addAll(newProviders);
			newProviders.forEach(p -> {
				Icon icon = p.getIcon();
				
				if (icon instanceof ImageIcon)  {
					ImageIcon ii = (ImageIcon) icon;
					
					if (ii.getIconWidth() > ICON_SIZE || ii.getIconHeight() > ICON_SIZE)
						icon = new ImageIcon(ii.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));
				}
				
				providerIcons.put(p, icon != null ? icon : new ImageIcon(new RandomImage(ICON_SIZE, ICON_SIZE)));
			});
		}
		
		if (selectedProvider != null && !newProviders.contains(selectedProvider))
			selectedProvider = null;
		
		// We are not changing the selectedProvider field here (only the user should do it),
		// but still need to let the widget know that the actual "current" provider has changed.
		// This is done this way to prevent a core provider from preventing another preferred one
		// (from third-party apps) from being pre-selected when Cytoscape restarts,
		// since the preferred one is auto-selected only when the user has not selected another provider yet.
		NetworkSearchTaskFactory newSelected = getSelectedProvider();
		
		if (newSelected != oldSelected)
			firePropertyChange("selectedProvider", oldSelected, newSelected);
	}
	
	void updateProvidersButton() {
		NetworkSearchTaskFactory currentProvider = getSelectedProvider();
		
		if (currentProvider != null) {
			Icon icon = providerIcons.get(currentProvider);
			getProvidersButton().setIcon(icon != null ? icon : emptyIcon);
			getProvidersButton().setToolTipText(currentProvider.getName());
		} else {
			getProvidersButton().setIcon(emptyIcon);
			getProvidersButton().setToolTipText("Please select a search provider...");
		}
		
		getProvidersButton().setEnabled(!providers.isEmpty());
		getProviderSelectorButton().setEnabled(!providers.isEmpty());
	}
	
	void updateSearchEnabled() {
		boolean enabled = getSelectedProvider() != null;
		getSearchTextField().setEnabled(enabled);
		getOptionsButton().setEnabled(enabled);
		updateSearchButton();
	}
	
	void updateSearchButton() {
		NetworkSearchTaskFactory tf = getSelectedProvider();
		getSearchButton().setEnabled(tf != null && tf.isReady());
	}
	
	void updateSelectedSearchComponent(JComponent queryComp) {
		getContentPane().removeAll();
		
		if (queryComp == null)
			queryComp = getSearchTextField();
		
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(queryComp, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(queryComp, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getOptionsButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	private void showProvidersPopup() {
		if (providers.isEmpty())
			return;
		
		if (providersPopup != null)
			disposeProvidersPopup(false); // Just to make sure there will never be more than one dialog
		
		providersPopup = new JPopupMenu();
		providersPopup.setBackground(getBackground());
		providersPopup.setBorder(BorderFactory.createEmptyBorder());
		
		providersPopup.setLayout(new BorderLayout());
		providersPopup.add(getProvidersPanel(), BorderLayout.CENTER);
		
		providersPopup.addPropertyChangeListener("visible", evt -> {
			if (Boolean.FALSE.equals(evt.getNewValue())) {
				updateProvidersButton();
				updateSearchEnabled();
			}
		});
		
		getProvidersPanel().update();
		
		providersPopup.pack();
		providersPopup.show(getProvidersButton(), 0, getProvidersButton().getHeight());
		providersPopup.requestFocus();
		getProvidersPanel().getTable().requestFocusInWindow();
	}
	
	private void disposeProvidersPopup(boolean commit) {
		if (providersPopup != null) {
			if (commit && getProvidersPanel().getSelectedValue() != null)
				setSelectedProvider(getProvidersPanel().getSelectedValue());
			
			providersPopup.removeAll();
			providersPopup.setVisible(false);
			providersPopup = null;
		}
	}
	
	void showOptionsDialog(JComponent comp) {
		if (comp == null)
			return;
		
		getOptionsDialog().update(comp);
		
		var pt = getOptionsButton().getLocationOnScreen(); 
		getOptionsDialog().setLocation(pt.x, pt.y + getOptionsButton().getHeight());
		getOptionsDialog().pack();
		getOptionsDialog().setVisible(true);
		getOptionsDialog().requestFocus();
	}
	
	private void disposeOptionsDialog() {
		getOptionsDialog().dispose();
	}

	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(2, 1, 2, 1)
		));
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		int maxHeight = getContentPane().getPreferredSize().height;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getProvidersButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getProviderSelectorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSearchButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getProvidersButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getProviderSelectorButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getContentPane(), maxHeight, maxHeight, maxHeight)
				.addComponent(getSearchButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		update(Collections.emptyList());
	}
	
	JButton getProvidersButton() {
		if (providersButton == null) {
			providersButton = new JButton(emptyIcon);
			styleButton(providersButton, ICON_SIZE, providersButton.getFont(), -1);
			providersButton.addActionListener(evt -> {
				showProvidersPopup();
			});
			updateProvidersButton();
		}
		
		return providersButton;
	}
	
	JButton getProviderSelectorButton() {
		if (providerSelectorButton == null) {
			providerSelectorButton = new JButton(IconManager.ICON_SORT_DOWN);
			providerSelectorButton.setToolTipText("Click to select a search provider...");
			styleButton(providerSelectorButton, 12, serviceRegistrar.getService(IconManager.class).getIconFont(10.0f),
					SwingConstants.RIGHT);
			providerSelectorButton.addActionListener(evt -> {
				getProvidersButton().doClick();
			});
		}
		
		return providerSelectorButton;
	}
	
	JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBackground(getBackground());
			
			updateSelectedSearchComponent(getSearchTextField());
		}
		
		return contentPane;
	}
	
	JTextField getSearchTextField() {
		if (searchTextField == null) {
			var msgColor = UIManager.getColor("Label.disabledForeground");
			int vgap = 1;
			int hgap = 5;
			
			searchTextField = new JTextField() {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					
					if (getText() == null || getText().trim().isEmpty()) {
						// Set antialiasing
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHints(
								new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
						// Set the font
					    g2.setFont(getFont());
						// Get the FontMetrics
					    FontMetrics metrics = g2.getFontMetrics(getFont());
					    // Determine the X coordinate for the text
					    int x = hgap;
					    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
					    int y = (metrics.getHeight() / 2) + metrics.getAscent() + vgap;
						// Draw
						g2.setColor(msgColor);
						g2.drawString(DEF_SEARCH_TEXT, x, y);
						g2.dispose();
					}
				}
			};
			searchTextField.setBackground(getBackground());
			searchTextField.setMinimumSize(searchTextField.getPreferredSize());
			searchTextField.setBorder(BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));
			searchTextField.setFont(searchTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return searchTextField;
	}
	
	JToggleButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JToggleButton(IconManager.ICON_BARS);
			optionsButton.setToolTipText("More Options...");
			styleButton(optionsButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f),
					SwingConstants.LEFT);
		}
		
		return optionsButton;
	}
	
	JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton(IconManager.ICON_SEARCH);
			styleButton(searchButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f),
					SwingConstants.LEFT);
			searchButton.setBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));
			searchButton.setToolTipText("Search Network");
		}
		
		return searchButton;
	}
	
	ProvidersPanel getProvidersPanel() {
		if (providersPanel == null) {
			providersPanel = new ProvidersPanel();
		}
		
		return providersPanel;
	}
	
	public OptionsDialog getOptionsDialog() {
		if (optionsDialog == null) {
			optionsDialog = new OptionsDialog();
		}
		
		return optionsDialog;
	}
	
	private void styleButton(AbstractButton btn, int width, Font font, int borderSide) {
		btn.setFont(font);
		btn.setContentAreaFilled(false);
		
		if (borderSide == SwingConstants.LEFT)
			btn.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1, 0, 1, 1),
					BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground"))
					
			));
		else if (borderSide == SwingConstants.RIGHT)
			btn.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1, 1, 1, 0),
					BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"))
					
			));
		else
			btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		
		var d = new Dimension(width, Math.max(width, getSearchTextField().getPreferredSize().height));
		btn.setMinimumSize(d);
		btn.setPreferredSize(d);
	}
	
	class ProvidersPanel extends JPanel {
		
		private final static int MAX_VISIBLE_ROWS = 10;
		private final static int COL_COUNT = 3;
		
		final static int ICON_COL_IDX = 0;
		final static int NAME_COL_IDX = 1;
		final static int WEBSITE_COL_IDX = 2;
		
		private JScrollPane scrollPane;
		private JTable table;
		
		ProvidersPanel() {
			setLayout(new BorderLayout());
			add(getScrollPane(), BorderLayout.CENTER);
			
			setKeyBindings(getTable());
		}
		
		private NetworkSearchTaskFactory getProvider(int row) {
			return (NetworkSearchTaskFactory) getTable().getModel().getValueAt(row, NAME_COL_IDX);
		}
		
		public NetworkSearchTaskFactory getSelectedValue() {
			int row = getTable().getSelectedRow();
			
			return row != -1 ? getProvider(row) : null;
		}

		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getTable());
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			}
			
			return scrollPane;
		}
		
		JTable getTable() {
			if (table == null) {
				DefaultTableModel model = new DefaultTableModel() {
					@Override
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
				model.setColumnCount(COL_COUNT);
				
				table = new JTable(model);
				table.setDefaultRenderer(Object.class, new ProvidersTableCellRenderer());
				table.setTableHeader(null);
				table.setIntercellSpacing(new Dimension(0, 0));
				table.setShowGrid(false);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table.setColumnSelectionAllowed(false);
				table.setRowHeight(ICON_SIZE + 2);
				
				table.addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						int row = getTable().rowAtPoint(e.getPoint());
						
						if (row != -1)
							setSelectedRow(row);
					}
				});
				table.addMouseListener(new MouseAdapter() {
					@Override
					public void  mousePressed(MouseEvent e) {
					    int col = getTable().columnAtPoint(e.getPoint());
					    int row = getTable().rowAtPoint(e.getPoint());
					    
					    if (row != -1) {
					    	NetworkSearchTaskFactory tf = getProvider(row);
					    	
							if (col == WEBSITE_COL_IDX && tf != null && tf.getWebsite() != null) {
								serviceRegistrar.getService(OpenBrowser.class).openURL(tf.getWebsite().toString());
							} else {
								getTable().repaint();
								disposeProvidersPopup(true);
							}
					    }
					}
				});
				// Provider descriptions can be very long, so let's make the tooltip visible for a few minutes
				// to give the user a chance to read them
				table.addMouseListener(new DismissDelayMouseAdapter((int) TimeUnit.MINUTES.toMillis(5))); // 5 min
			}
			
			return table;
		}
		
		void update() {
			Object[][] data = new Object[providers.size()][COL_COUNT];
			int nameWidth = 100;
			int selectedRow = -1;
			int i = 0;
			
			Font defFont = ((ProvidersTableCellRenderer) getTable().getDefaultRenderer(Object.class)).defFont;
			AffineTransform af = new AffineTransform();
			FontRenderContext frc = new FontRenderContext(af, true, true);
			
			for (NetworkSearchTaskFactory tf : providers) {
				data[i][ICON_COL_IDX] = tf;
				data[i][NAME_COL_IDX] = tf;
				data[i][WEBSITE_COL_IDX] = tf;
				
				if (tf.equals(getSelectedProvider()))
					selectedRow = i;
				
				nameWidth = Math.max(nameWidth, (int) (defFont.getStringBounds(tf.getName(), frc).getWidth()));
				i++;
			}
			
			nameWidth = Math.min(340, nameWidth);
			
			DefaultTableModel model = (DefaultTableModel) getTable().getModel();
			model.setDataVector(data, new String[COL_COUNT]);
			
			getTable().getColumnModel().getColumn(ICON_COL_IDX).setMinWidth(ICON_SIZE);
			getTable().getColumnModel().getColumn(ICON_COL_IDX).setMaxWidth(ICON_SIZE);
			getTable().getColumnModel().getColumn(NAME_COL_IDX).setMinWidth(nameWidth + 10);
			getTable().getColumnModel().getColumn(WEBSITE_COL_IDX).setMinWidth(32);
			getTable().getColumnModel().getColumn(WEBSITE_COL_IDX).setMaxWidth(32);
			
			setSelectedRow(selectedRow);
			getTable().repaint();
			
			int w = getTable().getColumnModel().getTotalColumnWidth() + 20;
			int h = providers.size() <= MAX_VISIBLE_ROWS ?
					getTable().getPreferredSize().height : getTable().getRowHeight() * MAX_VISIBLE_ROWS;
			getScrollPane().getViewport().setPreferredSize(new Dimension(w, h));
		}

		void setSelectedRow(int row) {
			if (row != -1)
				getTable().setRowSelectionInterval(row, row);
		}
		
		private class ProvidersTableCellRenderer extends DefaultTableCellRenderer {
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			final Font defFont = getFont().deriveFont(LookAndFeelUtil.getSmallFontSize());
			final Font iconFont = iconManager.getIconFont(12.0f);
			final Border defBorder = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(1, 0, 0, 0),
					BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"))
			);
			final Border nameBorder = BorderFactory.createCompoundBorder(
					defBorder,
					BorderFactory.createEmptyBorder(0, 10, 0, 0)
			);
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				setForeground(UIManager.getColor("Label.foreground"));
				setBackground(UIManager.getColor(isSelected ? "Table.selectionBackground" : "Table.background"));
				setHorizontalAlignment(CENTER);
				setFont(defFont);
				setText(null);
				setIcon(null);
				setBorder(defBorder);
				
				if (value instanceof NetworkSearchTaskFactory) {
					NetworkSearchTaskFactory tf = (NetworkSearchTaskFactory) value;
					setToolTipText(tf.getDescription());
					
					switch (column) {
						case ICON_COL_IDX:
							Icon icon = providerIcons.get(tf);
							setIcon(icon != null ? icon : emptyIcon);
							break;
		
						case NAME_COL_IDX:
							setText(tf.getName());
							setHorizontalAlignment(LEFT);
							setBorder(nameBorder);
							break;
						
						case WEBSITE_COL_IDX:
							URL url = tf.getWebsite();
							setText(url != null ? IconManager.ICON_EXTERNAL_LINK : "");
							setFont(iconFont);
							setForeground(UIManager.getColor("Table.focusCellBackground"));
							setToolTipText(url != null ? "Visit Website..." : null);
							break;
					}
				}
				
				return this;
			}
		}
		
		private void setKeyBindings(JComponent comp) {
			var actionMap = comp.getActionMap();
			var inputMap = comp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), KeyAction.VK_TAB);
			
			actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
			actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
			actionMap.put(KeyAction.VK_TAB, new KeyAction(KeyAction.VK_TAB));
		}

		private class KeyAction extends AbstractAction {

			final static String VK_ENTER = "VK_ENTER";
			final static String VK_SPACE = "VK_SPACE";
			final static String VK_TAB = "VK_TAB";
			
			KeyAction(String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				var cmd = e.getActionCommand();
				
				if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE) || cmd.equals(VK_TAB))
					disposeProvidersPopup(true);
			}
		}
	}
	
	private class OptionsDialog extends JDialog {
		
		public OptionsDialog() {
			super(SwingUtilities.getWindowAncestor(NetworkSearchBar.this), ModalityType.MODELESS);
			setBackground(getBackground());
			setUndecorated(true);
			getRootPane().setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					updateSearchEnabled();
				}
			});
			addWindowFocusListener(new WindowFocusListener() {
				@Override
				public void windowLostFocus(WindowEvent e) {
					// If the a component in the Options popup opens another dialog, the Options one
					// loses focus, but we don't want it to be disposed.
					if (!hasVisibleOwnedWindows(OptionsDialog.this)) {
						if (isShowing() && getOptionsButton().isShowing()) {
							// If cursor is over the options button, set the toggle button to not-selected
							// to prevent it from opening the dialog again right after its disposed
							Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
							Point buttonLoc = getOptionsButton().getLocationOnScreen();
							mouseLoc.x -= buttonLoc.x;
							mouseLoc.y -= buttonLoc.y;
							
							if (!getOptionsButton().contains(mouseLoc))
								getOptionsButton().setSelected(false);
						}
						
						// Dispose
						disposeOptionsDialog();
					}
				}
				@Override
				public void windowGainedFocus(WindowEvent e) {
				}
			});
		}
		
		void update(JComponent comp) {
			setContentPane(comp);
			setKeyBindings(comp);
		}
		
		private void setKeyBindings(JComponent comp) {
			var actionMap = comp.getActionMap();
			var inputMap = comp.getInputMap(WHEN_IN_FOCUSED_WINDOW);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KeyAction.VK_ESCAPE);
			actionMap.put(KeyAction.VK_ESCAPE, new KeyAction(KeyAction.VK_ESCAPE));
		}
		
		private class KeyAction extends AbstractAction {

			final static String VK_ESCAPE = "VK_ESCAPE";
			
			KeyAction(String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				var cmd = e.getActionCommand();
				
				if (cmd.equals(VK_ESCAPE)) {
					disposeOptionsDialog();
					getOptionsButton().setSelected(false);
				}
			}
		}
	}
	
	/**
	 * Hack to prolong a tooltip’s visible delay
	 * Thanks to: http://tech.chitgoks.com/2010/05/31/disable-tooltip-delay-in-java-swing/
	 */
	private class DismissDelayMouseAdapter extends MouseAdapter {
		
		final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
		final int dismissDelayMinutes;
		
		public DismissDelayMouseAdapter(int milliseconds) {
		    dismissDelayMinutes = milliseconds;
		}
	    
	    @Override
	    public void mouseEntered(MouseEvent e) {
	        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
	    }
	 
	    @Override
	    public void mouseExited(MouseEvent e) {
	        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
	    }
	}
	
	private class EmptyIcon implements Icon {

		private final String text = IconManager.ICON_BAN;
		private final Color fgColor;
		
		private final int width;
		private final int height;

		public EmptyIcon(int width, int height) {
			this.width = width;
			this.height = height;
			
			Color c = UIManager.getColor("Label.disabledForeground");
			fgColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			// Set antialiasing
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(
					new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			
			// Set the font
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(28.0f);
		    g2.setFont(font);
			// Get the FontMetrics
		    FontMetrics metrics = g2.getFontMetrics(font);
		    // Determine the X coordinate for the text
		    int sx = x + 2 + (width - metrics.stringWidth(text)) / 2;
		    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		    int sy = y + 1 + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
		    
			// Draw
			g2.setColor(fgColor);
			g2.drawString(text, sx, sy);
		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public int getIconHeight() {
			return height;
		}
	}
}
