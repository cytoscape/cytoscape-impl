package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.Tunable;

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
	
	private final EmptyIcon emptyIcon = new EmptyIcon(ICON_SIZE, ICON_SIZE);
	
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
	
	public void setSelectedProvider(NetworkSearchTaskFactory selectedProvider) {
		if (selectedProvider != this.selectedProvider) {
			this.selectedProvider = selectedProvider;
			updateProvidersButton();
			updateSearchEnabled();
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
		
		if (newProviders != null)
			providers.addAll(newProviders);
		
		setDefaultProvider(defaultProvider);
		
		if (selectedProvider != null && providers.contains(selectedProvider))
			setSelectedProvider(selectedProvider);
		else
			setSelectedProvider(defaultProvider);
	}
	
	private void updateProvidersButton() {
		if (selectedProvider != null) {
			getProvidersButton().setIcon(selectedProvider.getIcon());
			getProvidersButton().setToolTipText(selectedProvider.getName());
		} else {
			getProvidersButton().setIcon(emptyIcon);
			getProvidersButton().setToolTipText("Please select a search provider...");
		}
		
		getProvidersButton().setEnabled(!providers.isEmpty());
		getProviderSelectorButton().setEnabled(!providers.isEmpty());
	}
	
	private void updateSearchEnabled() {
		boolean enabled = selectedProvider != null;
		getSearchTextField().setEnabled(enabled);
		getOptionsButton().setEnabled(enabled);
		getSearchButton().setEnabled(enabled);
	}
	
	private void showProvidersPopup() {
		if (providers.isEmpty())
			return;
		
		setEnabled(false); // Disable the search components to prevent accidental repeated clicks
		
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
			
			providersPopup.setVisible(false);
			providersPopup = null;
		}
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
			styleButton(providersButton, ICON_SIZE, providersButton.getFont());
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
			styleButton(providerSelectorButton, 12, serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
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
			
			final GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return contentPane;
	}
	
	JTextField getSearchTextField() {
		if (searchTextField == null) {
			final Color msgColor = UIManager.getColor("Label.disabledForeground");
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
					    int y = metrics.getHeight() / 2 + metrics.getAscent();
						// Draw
						g2.setColor(msgColor);
						g2.drawString(DEF_SEARCH_TEXT, x, y);
						g2.dispose();
					}
				}
			};
			searchTextField.setMinimumSize(searchTextField.getPreferredSize());
			searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 1, UIManager.getColor("Separator.foreground")),
					BorderFactory.createEmptyBorder(1, hgap-1, 1, hgap-1)
			));
			searchTextField.setFont(searchTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return searchTextField;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(IconManager.ICON_ELLIPSIS_V);
			styleButton(optionsButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
		}
		
		return optionsButton;
	}
	
	JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton(IconManager.ICON_SEARCH);
			styleButton(searchButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
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
	
	private void styleButton(AbstractButton btn, int width, Font font) {
		btn.setFont(font);
		btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		btn.setContentAreaFilled(false);
		
		Dimension d = new Dimension(width, getSearchTextField().getPreferredSize().height);
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
						.addComponent(iconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(10)
						.addComponent(nameLabel, 120, PREFERRED_SIZE, 380)
						.addGap(10)
				);
				layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(iconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(nameLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
				
				providersList.setCellRenderer((JList<? extends NetworkSearchTaskFactory> list, NetworkSearchTaskFactory value,
						int index, boolean isSelected, boolean cellHasFocus) -> {
							iconLabel.setIcon(value.getIcon());
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
	
	// =================================================================================================================
	/**
	 * Task Factory that haver to be implemented in order to create and register a Network Search provider.
	 */
	public static interface NetworkSearchTaskFactory extends TaskFactory {
		
		/**
		 * Returns the unique id of this network search provider.
		 * Use namespaces to make sure it is unique (e.g. "org.myCompany.mySearch").
		 * @return A unique id for this search provider.
		 */
		String getId();
		
		/**
		 * A short name to be displayed to the user.
		 * @return The name of this search provider.
		 */
		String getName();

		/**
		 * An optional short text describing what this search provider does.
		 * @return A text that describes this search provider, which can be null.
		 */
		String getDescription();
		
		/**
		 * An icon that represents this search provider.
		 * @return If null, Cytoscape may provide a default or random icon for this search provider.
		 */
		Icon getIcon();
		
		/**
		 * An optional URL the user can use to find more information about this search provider.
		 * @return A URL to a website, which can be null.
		 */
		URL getWebsite();

		/**
		 * @return If null, Cytoscape will create a basic search field for you.
		 */
		JComponent createQueryComponent();
		
		/**
		 * @return If null, extra search options will not be available to the end user.
		 */
		JComponent createOptionsComponent();
	}
	
	// TODO: Think about commands--use tunables for options
	public static abstract class AbstractNetworkSearchTaskFactory extends AbstractTaskFactory
			implements NetworkSearchTaskFactory {
		
		// TODO For Commands, Tunables go into Tasks?
		// TODO Use conventions? e.g. 'query' (type String) creates a standard JTextField unless createQueryComponent() returns not null
		@Tunable(description = "Search Query:")
		public String query;
		
		private final String id;
		private final String name;
		private final String description;
		private final Icon icon;
		private final URL website;
		
		protected AbstractNetworkSearchTaskFactory(String id, String name, Icon icon) {
			this(id, name, null, icon, null);
		}
		
		protected AbstractNetworkSearchTaskFactory(String id, String name, String description, Icon icon) {
			this(id, name, description, icon, null);
		}
		
		protected AbstractNetworkSearchTaskFactory(String id, String name, String description, Icon icon, URL website) {
			if (id == null || id.trim().isEmpty())
				throw new IllegalArgumentException("'id' must not be null or blank.");
			if (name == null || name.trim().isEmpty())
				throw new IllegalArgumentException("'name' must not be null or blank.");
			
			this.id = id;
			this.name = name;
			this.description = description;
			this.icon = icon != null ? icon : new ImageIcon(new RandomImage(ICON_SIZE, ICON_SIZE));
			this.website = website;
		}
		
		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public URL getWebsite() {
			return website;
		}
		
		public String getQuery() {
			return query;
		}
		
		@Override
		public JComponent createQueryComponent() {
			return null;
		}
		
		@Override
		public JComponent createOptionsComponent() {
			return null;
		}
		
		@Override
		public boolean isReady() {
			return query != null && !query.trim().isEmpty();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 11;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractNetworkSearchTaskFactory other = (AbstractNetworkSearchTaskFactory) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return name;
		}
		
		private class RandomImage extends BufferedImage {

			private final int grain = 5;
			private final int colorRange = 5;
			
			public RandomImage(int width, int height) {
				super(width, height, BufferedImage.TYPE_INT_ARGB);
				draw();
			}
			
			private void draw() {
				int w = getWidth();
				int h = getHeight();
				
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

				int max = 200, min = 100;
				
				Color color = randomColor();
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();
				
				g2.setColor(color);
				
				double blockout = Math.random();
				int x = 0, y = 0;
				
				for (int i = 0; i < grain; i++) {
					for (int j = 0; j < grain; j++) {
						if (blockout < 0.4) {
							g2.fillRect(x, y, w / grain, h / grain);
							g2.fillRect(w - x - w / grain, y, w / grain, h / grain);
							x += w / grain;
						} else {
							red -= colorRange;
							red = Math.min(max, Math.max(red, min));
							
							green += colorRange;
							green = Math.min(max, Math.max(green, min));
							
							blue += colorRange;
							blue = Math.min(max, Math.max(blue, min));
							
							g2.setColor(new Color(red, green, blue));
							x += w / grain;
						}
						
						blockout = Math.random();
					}
					
					y += h / grain;
					x = 0;
				}
			}
			
			private Color randomColor() {
				// Get rainbow, pastel colors
				Random random = new Random();
				final float hue = random.nextFloat();
				final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
				final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
				
				return Color.getHSBColor(hue, saturation, luminance);
			}
		}
	}
}
