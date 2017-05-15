package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.hasVisibleOwnedWindows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.internal.util.RandomImage;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
public class NetworkSearchPanel extends JPanel {

	private static final String DEFAULT_PROVIDER_PROP_KEY = "networkSearch.defaultProvider";
	private static final int ICON_SIZE = 32;
	private static final String DEF_SEARCH_TEXT = "Type your query here...";
	
	private JButton providersButton;
	private JButton providerSelectorButton;
	private JPanel contentPane;
	private JTextField searchTextField;
	private JButton optionsButton;
	private JButton searchButton;
	
	private JPopupMenu providersPopup;
	private ProvidersPanel providersPanel;
	
	private OptionsDialog optionsDialog;
	
	private final EmptyIcon emptyIcon = new EmptyIcon(ICON_SIZE, ICON_SIZE);
	private final Map<NetworkSearchTaskFactory, Icon> providerIcons = new HashMap<>();
	
	private final Set<NetworkSearchTaskFactory> providers;
	private NetworkSearchTaskFactory defaultProvider;
	private NetworkSearchTaskFactory selectedProvider;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkSearchPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		final Collator collator = Collator.getInstance();
		providers = new TreeSet<>((NetworkSearchTaskFactory o1, NetworkSearchTaskFactory o2) -> {
			return collator.compare(o1.getName(), o2.getName());
		});
		
		init();
	}
	
	public void setDefaultProvider(NetworkSearchTaskFactory suggestedProvider) {
		Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		
		if (suggestedProvider == null || !providers.contains(suggestedProvider)) {
			// Check if there is a CyProperty for this
			String id = props.getProperty(DEFAULT_PROVIDER_PROP_KEY);
			
			if (id != null)
				suggestedProvider = getProvider(id);
		}
		
		if (suggestedProvider != null) {
			// Update the CyProperty as well;
			props.setProperty(DEFAULT_PROVIDER_PROP_KEY, suggestedProvider.getId());
		} else {
			if (!providers.isEmpty())
				suggestedProvider = providers.iterator().next();
			// Do not set this provider as default in the CyProperty,
			// because it may be provided by an app that is missing right now,
			// but can be installed later.
		}
		
		defaultProvider = suggestedProvider;
	}
	
	public NetworkSearchTaskFactory getDefaultProvider() {
		return defaultProvider;
	}
	
	public void setSelectedProvider(NetworkSearchTaskFactory newValue) {
		if (newValue != selectedProvider) {
			NetworkSearchTaskFactory oldValue = selectedProvider;
			selectedProvider = newValue;
			firePropertyChange("selectedProvider", oldValue, newValue);
		}
	}
	
	public NetworkSearchTaskFactory getSelectedProvider() {
		return selectedProvider;
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
		
		setDefaultProvider(defaultProvider);
		
		if (selectedProvider != null && providers.contains(selectedProvider))
			setSelectedProvider(selectedProvider);
		else
			setSelectedProvider(defaultProvider);
	}
	
	void updateProvidersButton() {
		if (selectedProvider != null) {
			Icon icon = providerIcons.get(selectedProvider);
			getProvidersButton().setIcon(icon != null ? icon : emptyIcon);
			getProvidersButton().setToolTipText(selectedProvider.getName());
		} else {
			getProvidersButton().setIcon(emptyIcon);
			getProvidersButton().setToolTipText("Please select a search provider...");
		}
		
		getProvidersButton().setEnabled(!providers.isEmpty());
		getProviderSelectorButton().setEnabled(!providers.isEmpty());
	}
	
	void updateSearchEnabled() {
		boolean enabled = selectedProvider != null;
		getSearchTextField().setEnabled(enabled);
		getOptionsButton().setEnabled(enabled);
		updateSearchButton();
	}
	
	void updateSearchButton() {
		getSearchButton().setEnabled(selectedProvider != null && selectedProvider.isReady());
	}
	
	void updateSelectedSearchComponent(JComponent queryComp) {
		getContentPane().removeAll();
		
		if (queryComp == null)
			queryComp = getSearchTextField();
		
		final GroupLayout layout = new GroupLayout(getContentPane());
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
			if (evt.getNewValue() == Boolean.FALSE) {
				updateProvidersButton();
				updateSearchEnabled();
			}
		});
		
		getProvidersPanel().update();
		
		providersPopup.pack();
		providersPopup.show(getProvidersButton(), 0, getProvidersButton().getHeight());
		providersPopup.requestFocus();
		getProvidersPanel().getProvidersList().requestFocusInWindow();
	}
	
	private void disposeProvidersPopup(boolean commit) {
		if (providersPopup != null) {
			if (commit && getProvidersPanel().getProvidersList().getSelectedValue() != null)
				setSelectedProvider(getProvidersPanel().getProvidersList().getSelectedValue());
			
			providersPopup.removeAll();
			providersPopup.setVisible(false);
			providersPopup = null;
		}
	}
	
	void showOptionsDialog(JComponent comp) {
		if (comp == null)
			return;
		
		getOptionsDialog().update(comp);
		
		final Point pt = getOptionsButton().getLocationOnScreen(); 
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
				BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(2, 1, 2, 1)
		));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getProvidersButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getProviderSelectorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSearchButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getProvidersButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getProviderSelectorButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
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
			final Color msgColor = UIManager.getColor("Label.disabledForeground");
			final int vgap = 1;
			final int hgap = 5;
			
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
			searchTextField.setMinimumSize(searchTextField.getPreferredSize());
			searchTextField.setBorder(BorderFactory.createEmptyBorder(vgap, hgap, vgap, hgap));
			searchTextField.setFont(searchTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return searchTextField;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(IconManager.ICON_ELLIPSIS_V);
			optionsButton.setToolTipText("More Options...");
			styleButton(optionsButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f),
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
		
		Dimension d = new Dimension(width, Math.max(width, getSearchTextField().getPreferredSize().height));
		btn.setMinimumSize(d);
		btn.setPreferredSize(d);
	}
	
	class ProvidersPanel extends JPanel {
		
		private JScrollPane scrollPane;
		private JList<NetworkSearchTaskFactory> providersList;
		
		ProvidersPanel() {
			setLayout(new BorderLayout());
			add(getScrollPane(), BorderLayout.CENTER);
			
			setKeyBindings();
		}
		
		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getProvidersList());
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			}
			
			return scrollPane;
		}
		
		JList<NetworkSearchTaskFactory> getProvidersList() {
			if (providersList == null) {
				providersList = new JList<>(new DefaultListModel<>());
				
				providersList.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						disposeProvidersPopup(true);
					}
				});
				providersList.addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						int index = getProvidersList().locationToIndex(e.getPoint());
						
						if (index > -1)
							getProvidersList().setSelectedIndex(index);
					}
				});
				
				// Renderer
				final JPanel cell = new JPanel();
				cell.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 0, 1),
						BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"))
				));
				
				final JLabel iconLabel = new JLabel(emptyIcon);
				
				final JLabel nameLabel = new JLabel(" --- ");
				nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				
				final GroupLayout layout = new GroupLayout(cell);
				cell.setLayout(layout);
				layout.setAutoCreateContainerGaps(false);
				layout.setAutoCreateGaps(false);
				
				layout.setHorizontalGroup(layout.createSequentialGroup()
						.addComponent(iconLabel, ICON_SIZE, ICON_SIZE, ICON_SIZE)
						.addGap(10)
						.addComponent(nameLabel, 120, PREFERRED_SIZE, 380)
						.addGap(10)
				);
				layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(iconLabel, ICON_SIZE, ICON_SIZE, ICON_SIZE)
						.addComponent(nameLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
				
				providersList.setCellRenderer((JList<? extends NetworkSearchTaskFactory> list, NetworkSearchTaskFactory value,
						int index, boolean isSelected, boolean cellHasFocus) -> {
							Icon icon = providerIcons.get(value);
							iconLabel.setIcon(icon != null ? icon: emptyIcon);
							nameLabel.setText(value.getName());
							cell.setToolTipText(value.getDescription());
							
							String bg = isSelected ? "Table.selectionBackground" : "Table.background";
							String fg = isSelected ? "Table.selectionForeground" : "Table.foreground"; 
							cell.setBackground(UIManager.getColor(bg));
							nameLabel.setForeground(UIManager.getColor(fg));
							
							cell.revalidate();
							
							return cell;
				});
			}
			
			return providersList;
		}
		
		void update() {
			DefaultListModel<NetworkSearchTaskFactory> model =
					(DefaultListModel<NetworkSearchTaskFactory>) getProvidersList().getModel();
			model.removeAllElements();
			
			for (NetworkSearchTaskFactory tf : providers)
				model.addElement(tf);
			
			getProvidersList().setSelectedValue(selectedProvider, true);
			getProvidersList().setVisibleRowCount(Math.min(10, providers.size()));
		}
		
		private void setKeyBindings() {
			final ActionMap actionMap = getActionMap();
			final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
			
			actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
			actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
		}

		private class KeyAction extends AbstractAction {

			final static String VK_ENTER = "VK_ENTER";
			final static String VK_SPACE = "VK_SPACE";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				final String cmd = e.getActionCommand();
				
				if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE))
					disposeProvidersPopup(true);
			}
		}
	}
	
	private class OptionsDialog extends JDialog {
		
		public OptionsDialog() {
			super(SwingUtilities.getWindowAncestor(NetworkSearchPanel.this), ModalityType.MODELESS);
			setBackground(getBackground());
			setUndecorated(true);
			
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
					if (!hasVisibleOwnedWindows(OptionsDialog.this))
						disposeOptionsDialog();
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
			final ActionMap actionMap = comp.getActionMap();
			final InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), KeyAction.VK_ESCAPE);
			actionMap.put(KeyAction.VK_ESCAPE, new KeyAction(KeyAction.VK_ESCAPE));
		}
		
		private class KeyAction extends AbstractAction {

			final static String VK_ESCAPE = "VK_ESCAPE";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				final String cmd = e.getActionCommand();
				
				if (cmd.equals(VK_ESCAPE))
					disposeOptionsDialog();
			}
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
