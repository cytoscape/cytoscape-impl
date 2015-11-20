package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.view.NetworkViewGrid.MAX_THUMBNAIL_SIZE;
import static org.cytoscape.internal.view.NetworkViewGrid.MIN_THUMBNAIL_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_ARROW_LEFT;
import static org.cytoscape.util.swing.IconManager.ICON_ARROW_RIGHT;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_TH;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.view.NetworkViewGrid.ThumbnailPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

@SuppressWarnings("serial")
public class NetworkViewsPanel extends JPanel {

	private static final String GRID_NAME = "__NETWORK_VIEW_GRID__";
	
	private JPanel contentPane;
	private JPanel toolBarsPanel;
	private JPanel gridToolBar;
	private JPanel viewToolBar;
	private final CardLayout cardLayout;
	private final NetworkViewGrid networkViewGrid;
	private JScrollPane gridScrollPane;
	
	private JButton viewModeButton;
	private JButton destroyViewButton;
	private JSlider thumbnailSlider;
	
	private JButton gridModeButton;
	private JButton previousViewButton;
	private JButton nextViewButton;
	private JButton detachViewButton;
	private JLabel viewTitleLabel;
	private JTextField viewTitleTextField;
	
	private final Map<String, NetworkViewContainer> viewContainers;
	private final Map<String, JFrame> viewFrames;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewsPanel(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		viewContainers = new LinkedHashMap<>();
		viewFrames = new HashMap<>();
		cardLayout = new CardLayout();
		networkViewGrid = new NetworkViewGrid(serviceRegistrar);
		
		networkViewGrid.addPropertyChangeListener("thumbnailPanels", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				for (ThumbnailPanel tp : networkViewGrid.getThumbnailPanels()) {
					tp.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() == 2) {
								setCurrentNetworkView(tp.getNetworkView());
								show(tp.getNetworkView());
							}
						}
					});
				}
			}
		});
		
		init();
	}
	
	public RenderingEngine<CyNetwork> addNetworkView(final CyNetworkView view,
			final RenderingEngineFactory<CyNetwork> engineFactory) {
		if (isRendered(view))
			return null;
		
		final NetworkViewContainer vc = new NetworkViewContainer(view, engineFactory, serviceRegistrar);
		
		vc.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				final CyNetworkView oldView = getCurrentNetworkView();
				
				if ((oldView == null && view == null) || (oldView != null && oldView.equals(view)))
					return;
				
				firePropertyChange("currentNetworkView", oldView, view);
			}
		});
		
		viewContainers.put(vc.getName(), vc);
		networkViewGrid.addThumbnail(vc.getRenderingEngine());
		getContentPane().add(vc, vc.getName());
		show(vc.getName());
		
		return vc.getRenderingEngine();
	}
	
	public boolean isRendered(final CyNetworkView view) {
		final String name = NetworkViewContainer.createUniqueName(view);
		return viewContainers.containsKey(name) || viewFrames.containsKey(name);
	}

	public void remove(final CyNetworkView view) {
		if (view == null)
			return;
		
		final int total = getContentPane().getComponentCount();
		
		for (int i = 0; i < total; i++) {
			final Component c = getContentPane().getComponent(i);
			
			if (c instanceof NetworkViewContainer) {
				if (((NetworkViewContainer) c).getNetworkView().equals(view)) {
					cardLayout.removeLayoutComponent(c);
					viewContainers.remove(((NetworkViewContainer) c).getName());
					networkViewGrid.removeThumbnail(((NetworkViewContainer) c).getRenderingEngine());
					showGrid();
					
					break;
				}
			}
		}
	}
	
	public CyNetworkView getCurrentNetworkView() {
		return networkViewGrid.getCurrentNetworkView();
	}
	
	public void setCurrentNetworkView(final CyNetworkView view) {
		networkViewGrid.setCurrentNetworkView(view);
		
		if (view == null) {
			showGrid();
		} else {
			final String name = NetworkViewContainer.createUniqueName(view);
			
			if (isGridMode()) {
				final JFrame frame = viewFrames.get(name);
				
				if (frame != null)
					showFrame(frame);
				else if (networkViewGrid.getCurrentThumbnailPanel() != null)
					networkViewGrid.scrollRectToVisible(networkViewGrid.getCurrentThumbnailPanel().getBounds());
			} else {
				show(name);
			}
		}
	}
	
	public void showGrid() {
		cardLayout.show(getContentPane(), GRID_NAME);
		updateToolBars();
		networkViewGrid.update(getThumbnailSlider().getValue()); // TODO remove it when already updating after view changes
	}
	
	public void dettachNetworkView(final NetworkViewContainer vc) {
		if (vc == null)
			return;
		
		final String name = vc.getName();
		final CyNetworkView view = vc.getNetworkView();
		
		cardLayout.removeLayoutComponent(vc);
		viewContainers.remove(name);
		
		final JFrame frame = new JFrame(vc.getTitle());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(vc.getContentPane());
		frame.setLayeredPane(vc.getLayeredPane());
		frame.setGlassPane(vc.getGlassPane());
		
		viewFrames.put(name, frame);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				viewFrames.remove(name);
				vc.setContentPane(frame.getContentPane());
				vc.setLayeredPane(frame.getLayeredPane());
				vc.setGlassPane(frame.getGlassPane());
				getContentPane().add(vc, vc.getName());
				viewContainers.put(vc.getName(), vc);
				show(vc.getName());
			}
		});
		
		int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
		int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
		final boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
				!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
		
		if (w > 0 && h > 0) {
			frame.getContentPane().setPreferredSize(new Dimension(w, h));
		}
		
		showGrid();
		
		frame.pack();
		frame.setResizable(resizable);
		frame.setVisible(true);
		view.updateView();
	}
	
	private void show(final CyNetworkView view) {
		if (view != null)
			show(NetworkViewContainer.createUniqueName(view));
	}
	
	private void show(final String name) {
		if (name != null) {
			final JFrame frame = viewFrames.get(name);
			
			if (frame != null) {
				showFrame(frame);
			} else if (viewContainers.containsKey(name)) {
				cardLayout.show(getContentPane(), name);
				updateToolBars();
				
				// FIXME Why view not refreshing???
				final NetworkViewContainer vc = viewContainers.get(name);
				
				if (vc != null) {
					vc.invalidate();
					vc.updateUI();
					vc.getNetworkView().updateView();
				}
			} 
		} else {
			showGrid();
		}
	}

	private void showFrame(final JFrame frame) {
		frame.setVisible(true);
		frame.toFront();
		showGrid();
	}
	
	private boolean isGridMode() {
		return getCurrentCard() == getGridScrollPane();
	}
	
	private void updateToolBars() {
		final Component currentCard = getCurrentCard();
		
		if (currentCard instanceof NetworkViewContainer) {
			getGridToolBar().setVisible(false);
			getViewToolBar().setVisible(true);
			updateViewToolBar((NetworkViewContainer) currentCard);
		} else {
			getViewToolBar().setVisible(false);
			getGridToolBar().setVisible(true);
			updateGridToolBar();
		}
	}

	private void updateGridToolBar() {
		getViewModeButton().setEnabled(
				serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView() != null);
	}

	private void updateViewToolBar(final NetworkViewContainer vc) {
		getViewTitleLabel().setText(vc.getTitle());
		getViewToolBar().invalidate();
	}

	private NetworkViewContainer getCurrentViewContainer() {
		final Component c = getCurrentCard();

		return c instanceof NetworkViewContainer ? (NetworkViewContainer) c : null;
	}
	
	private Component getCurrentCard() {
		Component current = null;
		
		for (Component comp : getContentPane().getComponents()) {
			if (comp.isVisible())
				current = comp;
		}
		System.out.println("\t\t>> " + current.getName() + "\n");
		return current;
	}
	
	private void init() {
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Separator.foreground")));
		
		setLayout(new BorderLayout());
		add(getContentPane(), BorderLayout.CENTER);
		add(getToolBarsPanel(), BorderLayout.SOUTH);
		
		updateToolBars();
	}
	
	private JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(cardLayout);
			// Add the first panel in the card layout
			contentPane.add(getGridScrollPane(), GRID_NAME);
		}
		
		return contentPane;
	}
	
	private JPanel getToolBarsPanel() {
		if (toolBarsPanel == null) {
			toolBarsPanel = new JPanel(new BorderLayout());
			
			toolBarsPanel.setBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			toolBarsPanel.add(getGridToolBar(), BorderLayout.NORTH);
			toolBarsPanel.add(getViewToolBar(), BorderLayout.SOUTH);
		}
		
		return toolBarsPanel;
	}
	
	private JPanel getGridToolBar() {
		if (gridToolBar == null) {
			gridToolBar = new JPanel();
			gridToolBar.setName("gridToolBar");
			
			gridToolBar.setBorder(
					BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final GroupLayout layout = new GroupLayout(gridToolBar);
			gridToolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(!LookAndFeelUtil.isAquaLAF());
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getThumbnailSlider(), 100, 100, 100)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getThumbnailSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return gridToolBar;
	}
	
	private JPanel getViewToolBar() {
		if (viewToolBar == null) {
			viewToolBar = new JPanel();
			viewToolBar.setName("viewToolBar");
			
			final JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(viewToolBar);
			viewToolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviousViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNextViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), 100, 260, 320)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getDestroyViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPreviousViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNextViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getViewTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewTitleTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDestroyViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return viewToolBar;
	}
	
	private JScrollPane getGridScrollPane() {
		if (gridScrollPane == null) {
			gridScrollPane = new JScrollPane(networkViewGrid,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gridScrollPane.setName(GRID_NAME);
			gridScrollPane.getViewport().setBackground(networkViewGrid.getBackground());
		}
		
		return gridScrollPane;
	}
	
	private JButton getViewModeButton() {
		if (viewModeButton == null) {
			viewModeButton = new JButton(ICON_SHARE_ALT_SQUARE);
			viewModeButton.setToolTipText("Show Network View");
			viewModeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
			CytoPanelUtil.styleButton(viewModeButton);
			
			viewModeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					show(getCurrentNetworkView());
				}
			});
		}
		
		return viewModeButton;
	}
	
	private JButton getDetachViewButton() {
		if (detachViewButton == null) {
			detachViewButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachViewButton.setToolTipText("Detach Network View");
			detachViewButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
			CytoPanelUtil.styleButton(detachViewButton);
			
			detachViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (getCurrentViewContainer() != null)
						dettachNetworkView(getCurrentViewContainer());
				}
			});
		}
		
		return detachViewButton;
	}
	
	private JButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JButton(ICON_TH);
			gridModeButton.setToolTipText("Show Thumbnails");
			gridModeButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			CytoPanelUtil.styleButton(gridModeButton);
			
			gridModeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showGrid();
				}
			});
		}
		
		return gridModeButton;
	}
	
	private JButton getPreviousViewButton() {
		if (previousViewButton == null) {
			previousViewButton = new JButton(ICON_ARROW_LEFT);
			previousViewButton.setToolTipText("Previous View");
			previousViewButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			CytoPanelUtil.styleButton(previousViewButton);
			
			previousViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cardLayout.previous(getContentPane());
					updateToolBars();
				}
			});
		}
		
		return previousViewButton;
	}
	
	private JButton getNextViewButton() {
		if (nextViewButton == null) {
			nextViewButton = new JButton(ICON_ARROW_RIGHT);
			nextViewButton.setToolTipText("Next View");
			nextViewButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
			CytoPanelUtil.styleButton(nextViewButton);
			
			nextViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cardLayout.next(getContentPane());
					updateToolBars();
				}
			});
		}
		
		return nextViewButton;
	}
	
	private JLabel getViewTitleLabel() {
		if (viewTitleLabel == null) {
			viewTitleLabel = new JLabel();
			viewTitleLabel.setToolTipText("Double-click to change the title...");
			viewTitleLabel.setFont(viewTitleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			viewTitleLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2)
						showViewTitleEditor();
				}
			});
		}
		
		return viewTitleLabel;
	}
	
	public JTextField getViewTitleTextField() {
		if (viewTitleTextField == null) {
			viewTitleTextField = new JTextField();
			viewTitleTextField.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua (Mac OS X) only
			viewTitleTextField.setVisible(false);
			viewTitleTextField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeCurrentViewTitle(viewTitleTextField.getText());
				}
			});
			viewTitleTextField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					changeCurrentViewTitle(viewTitleTextField.getText());
				}
			});
		}
		
		return viewTitleTextField;
	}
	
	private JButton getDestroyViewButton() {
		if (destroyViewButton == null) {
			destroyViewButton = new JButton(ICON_TRASH_O);
			destroyViewButton.setToolTipText("Destroy Network View");
			destroyViewButton.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
			CytoPanelUtil.styleButton(destroyViewButton);
			
			destroyViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final NetworkViewContainer vc = getCurrentViewContainer();
					
					if (vc != null) {
						remove(vc.getNetworkView());
						
						// TODO Move to NetworkViewManager (fire event)
						if (serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViewSet().contains(vc.getNetworkView()))
							serviceRegistrar.getService(CyNetworkViewManager.class).destroyNetworkView(vc.getNetworkView());
					}
				}
			});
		}
		
		return destroyViewButton;
	}
	
	private JSlider getThumbnailSlider() {
		if (thumbnailSlider == null) {
			final int value = Math.round(MIN_THUMBNAIL_SIZE + (MAX_THUMBNAIL_SIZE - MIN_THUMBNAIL_SIZE) / 4.0f);
			thumbnailSlider = new JSlider(MIN_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE, value);
			thumbnailSlider.setToolTipText("Thumbnail Size");
			thumbnailSlider.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua (Mac OS X) only
			
			thumbnailSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (!thumbnailSlider.getValueIsAdjusting()) {
						final int thumbSize = thumbnailSlider.getValue();
						networkViewGrid.update(thumbSize);
					}
				}
			});
		}
		
		return thumbnailSlider;
	}
	
	private void showViewTitleEditor() {
		getViewTitleTextField().setText(getViewTitleLabel().getText());
		getViewTitleLabel().setVisible(false);
		getViewTitleTextField().setVisible(true);
		getViewTitleTextField().requestFocusInWindow();
	}
	
	private void changeCurrentViewTitle(String text) {
		if (text != null) {
			text = text.trim();
			
			// TODO Make sure it's unique
			if (!text.isEmpty()) {
				getViewTitleLabel().setText(text);
				// TODO This will fire a ViewChangedEvent - Just let the NetworkViewManager ask this panel to update itself instead?
				getCurrentNetworkView().setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, text);
				
				final ThumbnailPanel tp = networkViewGrid.getCurrentThumbnailPanel();
				
				if (tp != null)
					tp.update();
			}
		}
		
		getViewTitleTextField().setText(null);
		getViewTitleTextField().setVisible(false);
		getViewTitleLabel().setVisible(true);
		getViewToolBar().invalidate();
	}
	
	static void styleButton(final AbstractButton btn, final Font font) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setRolloverEnabled(false);
		btn.setFocusPainted(false);
		btn.setFocusable(false);
	}
}
