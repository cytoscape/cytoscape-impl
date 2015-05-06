package org.cytoscape.internal.layout.ui;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.util.ListSingleSelection;


/**
 *
 * The LayoutSettingsDialog is a dialog that provides an interface into all of the
 * various settings for layout algorithms.  Each CyLayoutAlgorithm must return a single
 * JPanel that provides all of its settings.
 */
public class LayoutSettingsDialog extends JDialog implements ActionListener {
	
	private final static long serialVersionUID = 1202339874277105L;
	
	private CyLayoutAlgorithm currentLayout;
	private TaskFactory currentAction;

	private JTabbedPane tabbedPane;
	private JPanel settingsPnl;
	private JPanel prefLayoutPnl;
	private JPanel buttonPnl;
	private JPanel settingsButtonPnl;
	private JComboBox<CyLayoutAlgorithm> algorithmCmb;
	private JPanel algorithmPnl;
	private JPanel layoutAttrPnl;
    private JComboBox<CyLayoutAlgorithm> prefAlgorithmCmb;

	private CyLayoutAlgorithmManager layoutAlgorithmMgr;
	private CySwingApplication swingApp;
	private CyApplicationManager appMgr;
	private PanelTaskManager taskMgr;
	private CyProperty<?> cyProperty;
	private DynamicTaskFactoryProvisioner factoryProvisioner;
	private LayoutAttributeTunable layoutAttrTunable;
	private final SelectedTunable selectedTunable;
	
	private boolean initialized;
	
	private static final String UNWEIGHTED = "(none)";

	/**
	 * Creates a new LayoutSettingsDialog object.
	 */
	public LayoutSettingsDialog(final CyLayoutAlgorithmManager cyLayoutAlgorithmManager, 
	                            final CySwingApplication desktop,
	                            final CyApplicationManager appMgr,
	                            final PanelTaskManager taskManager,
	                            final CyProperty<?> cytoscapePropertiesServiceRef,
	                            DynamicTaskFactoryProvisioner factoryProvisioner) {
		super(desktop.getJFrame(), "Layout Settings", false);

		this.layoutAlgorithmMgr = cyLayoutAlgorithmManager;
		this.swingApp = desktop;
		this.appMgr = appMgr;
		this.taskMgr = taskManager;
		this.cyProperty = cytoscapePropertiesServiceRef;
		this.factoryProvisioner = factoryProvisioner;
		
		initComponents();
		selectedTunable = new SelectedTunable();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Initialize and display
		if (isVisible()) {
			requestFocus();
		} else {
			if (!initialized) {
				initialize();
				setLocationRelativeTo(swingApp.getJFrame());
				pack();
			}
			setNetworkView(appMgr.getCurrentNetworkView());
			setVisible(true);
			initialized = true;
		}
	}

    void addLayout(CyLayoutAlgorithm layout) {
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
    		public void run() {
    	        initialize();
    		}
    	});
    }

    void removeLayout(final CyLayoutAlgorithm layout) {
    	SwingUtilities.invokeLater(new Runnable() {
    		@Override
    		public void run() {
    	    	if (currentLayout == layout) {
    	    		getAlgorithmPnl().removeAll();
    	    	}
    	    	initialize();
    		}
    	});
    }

    private void initComponents() {
    	final JPanel contentPane = new JPanel();
    	final GroupLayout layout = new GroupLayout(contentPane);
    	contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTabbedPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
    	
		setContentPane(contentPane);
        pack();
    }

	private void initialize() {
		final Properties props = (Properties) cyProperty.getProperties();
		final String pref = props.getProperty("preferredLayoutAlgorithm", "force-directed");
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		final TreeSet<CyLayoutAlgorithm> allLayouts = new TreeSet<>(new Comparator<CyLayoutAlgorithm>() {
			@Override
			public int compare(CyLayoutAlgorithm o1, CyLayoutAlgorithm o2) {
				return collator.compare(o1.toString(), o2.toString());
			}
		});
		allLayouts.addAll(layoutAlgorithmMgr.getAllLayouts());
		
		// Populate the algorithm selector
		getAlgorithmCmb().removeAllItems();
		
		for (CyLayoutAlgorithm algo : allLayouts) 
			getAlgorithmCmb().addItem(algo);
		
		getAlgorithmCmb().setSelectedItem(currentLayout);

		// For the tabbedPanel "Set preferred Layout"
		getPrefAlgorithmCmb().removeAllItems();
		getPrefAlgorithmCmb().setRenderer(new LayoutAlgorithmListCellRenderer("Select preferred algorithm"));

		CyLayoutAlgorithm prefAlgo = null;
		
		for (CyLayoutAlgorithm algo : allLayouts) { 
			getPrefAlgorithmCmb().addItem(algo);
			
			if (algo.getName().equals(pref))
				prefAlgo = algo;
		}
		
		getPrefAlgorithmCmb().setSelectedItem(prefAlgo);
	}
	
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
	        tabbedPane.addTab("Layout Settings", getSettingsPnl());
	        tabbedPane.addTab("Preferred Layout", getPrefLayoutPnl());
		}
		
		return tabbedPane;
	}

	private JPanel getSettingsPnl() {
		if (settingsPnl == null) {
			settingsPnl = new JPanel();
			settingsPnl.setAutoscrolls(true);
			settingsPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			
			final JLabel algoLbl = new JLabel("Layout Algorithm:");
			
			final GroupLayout layout = new GroupLayout(settingsPnl);
			settingsPnl.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(algoLbl)
					.addComponent(getAlgorithmCmb())
					.addComponent(getAlgorithmPnl())
					.addComponent(getSettingsButtonPnl())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(algoLbl)
					.addComponent(getAlgorithmCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getAlgorithmPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getSettingsButtonPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return settingsPnl;
	}
	
	private JPanel getPrefLayoutPnl() {
		if (prefLayoutPnl == null) {
			prefLayoutPnl = new JPanel();
	        prefLayoutPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
	        
	        final GroupLayout layout = new GroupLayout(prefLayoutPnl);
	        prefLayoutPnl.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			final JLabel label = new JLabel("Preferred Layout Algorithm:");
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(label)
					.addComponent(getPrefAlgorithmCmb())
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPrefAlgorithmCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return prefLayoutPnl;
	}
	
	private JPanel getAlgorithmPnl() {
		if (algorithmPnl == null) {
			algorithmPnl = new JPanel();
			algorithmPnl.setLayout(new BoxLayout(algorithmPnl, BoxLayout.PAGE_AXIS));
			algorithmPnl.setAutoscrolls(true);
			algorithmPnl.setBorder(LookAndFeelUtil.createPanelBorder());
			algorithmPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
		}
		
		return algorithmPnl;
	}
	
	private JPanel getSettingsButtonPnl() {
		if (settingsButtonPnl == null) {
			settingsButtonPnl = new JPanel();
			settingsButtonPnl.setLayout(new BoxLayout(settingsButtonPnl, BoxLayout.LINE_AXIS));
			settingsButtonPnl.setAlignmentX(Component.CENTER_ALIGNMENT);
			settingsButtonPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			
			final JButton applyBtn = new JButton("Apply Layout");
			applyBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Object context = currentLayout.getDefaultLayoutContext();
					
					if (taskMgr.validateAndApplyTunables(context))
						taskMgr.execute(currentAction.createTaskIterator());
				}
			});

			settingsButtonPnl.add(Box.createHorizontalGlue());
			settingsButtonPnl.add(applyBtn);
			settingsButtonPnl.add(Box.createHorizontalGlue());
		}
		
		return settingsButtonPnl;
	}
	
	@SuppressWarnings("serial")
	public JPanel getButtonPnl() {
		if (buttonPnl == null) {
			buttonPnl = new JPanel();
			buttonPnl.setLayout(new BoxLayout(buttonPnl, BoxLayout.LINE_AXIS));
			buttonPnl.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
			
			final JButton doneBtn = new JButton(new AbstractAction("Done") {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			
			buttonPnl.add(Box.createHorizontalGlue());
			buttonPnl.add(doneBtn);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, doneBtn.getAction());
		}
		
		return buttonPnl;
	}
	
	private JComboBox<CyLayoutAlgorithm> getAlgorithmCmb() {
		if (algorithmCmb == null) {
			algorithmCmb = new JComboBox<CyLayoutAlgorithm>();
			algorithmCmb.setRenderer(new LayoutAlgorithmListCellRenderer("Select algorithm to view settings"));
			
			algorithmCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Object o = algorithmCmb.getSelectedItem();
					
					if (o instanceof CyLayoutAlgorithm) {
						currentLayout = (CyLayoutAlgorithm) o;
						//Checking if the context has already been charged, if so there is no need to do it again
						final Object context = currentLayout.getDefaultLayoutContext();

						final TaskFactory provisioner = factoryProvisioner.createFor(wrapWithContext(currentLayout, context));
						final JPanel tunablePnl = taskMgr.getConfiguration(provisioner, context);

						layoutAttrPnl = new JPanel();
						layoutAttrPnl.setLayout(new BoxLayout(layoutAttrPnl, BoxLayout.PAGE_AXIS));
						layoutAttrPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
						
						final CyNetworkView view = appMgr.getCurrentNetworkView();
						setNetworkView(view);

						getAlgorithmPnl().removeAll();
						getAlgorithmPnl().add(layoutAttrPnl);
						
						if (tunablePnl == null){
							JOptionPane.showMessageDialog(
									LayoutSettingsDialog.this,
									"Can not change settings for this algorithm, because tunable info is not available.",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
						} else {
							tunablePnl.setAlignmentX(Component.CENTER_ALIGNMENT);
							setPanelsTransparent(tunablePnl);
							getAlgorithmPnl().add(tunablePnl);
						}
						
						if (currentLayout.getSupportsSelectedOnly() && hasSelectedNodes(view)) {
							selectedTunable.selectedNodesOnly = true;
							final JPanel panel = taskMgr.getConfiguration(null, selectedTunable);
							setPanelsTransparent(panel);
							getAlgorithmPnl().add(panel);
						}
						
						currentAction = provisioner;
						LayoutSettingsDialog.this.pack();
					}
				}
			});
		}
		
		return algorithmCmb;
	}
	
	private JComboBox<CyLayoutAlgorithm> getPrefAlgorithmCmb() {
		if (prefAlgorithmCmb == null) {
			prefAlgorithmCmb = new JComboBox<CyLayoutAlgorithm>();
	        prefAlgorithmCmb.setModel(new DefaultComboBoxModel<CyLayoutAlgorithm>());
	        
	        prefAlgorithmCmb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					final CyLayoutAlgorithm layout = (CyLayoutAlgorithm) prefAlgorithmCmb.getSelectedItem();
					
					if (layout != null) {
						final String pref = layout.getName(); 
						final Properties props = (Properties) cyProperty.getProperties();
						
						if (props != null && !pref.equals(props.get("preferredLayoutAlgorithm"))) 
							props.setProperty("preferredLayoutAlgorithm", pref);
		            }
				}
	        });
		}
		
		return prefAlgorithmCmb;
	}
	
	void setNetworkView(CyNetworkView view) {
		if (layoutAttrPnl == null)
			return;
		
		layoutAttrPnl.removeAll();
		layoutAttrTunable = new LayoutAttributeTunable();
		
		if (view != null) {
			List<String> attributeList = getAttributeList(view.getModel(), currentLayout.getSupportedNodeAttributeTypes(), currentLayout.getSupportedEdgeAttributeTypes());
			
			if (attributeList.size() > 0) {
				layoutAttrTunable.layoutAttribute = new ListSingleSelection<String>(attributeList);
				layoutAttrTunable.layoutAttribute.setSelectedValue(attributeList.get(0));
				JPanel panel = taskMgr.getConfiguration(null, layoutAttrTunable);
				setPanelsTransparent(panel);
				layoutAttrPnl.add(panel);
				panel.invalidate();
			}
		}
	}

	private boolean hasSelectedNodes(final CyNetworkView view) {
		if (view == null)
			return false;
		
		final CyNetwork network = view.getModel();
		final CyTable table = network.getDefaultNodeTable();
		
		return table.countMatchingRows(CyNetwork.SELECTED, Boolean.TRUE) > 0;
	}
	
	private List<String> getAttributeList(CyNetwork network, Set<Class<?>> allowedNodeAttributeTypes, Set<Class<?>> allowedEdgeAttributeTypes) {
		List<String> attributes = new ArrayList<String>();
        Set<Class<?>> allowedTypes;
		CyTable table;
		if (allowedNodeAttributeTypes.size() > 0) {
			allowedTypes = allowedNodeAttributeTypes;
			table = network.getDefaultNodeTable();
		} else if (allowedEdgeAttributeTypes.size() > 0) {
			allowedTypes = allowedEdgeAttributeTypes;
			table = network.getDefaultEdgeTable();
		} else {
			return attributes;
		}
		
		for (final CyColumn column : table.getColumns()) {
            if (allowedTypes.contains(column.getType())) {
            	attributes.add(column.getName());
            }
		}
		
		if (attributes.size()>0)
			attributes.add(0, UNWEIGHTED);
        return attributes;
	}
	
	private String getLayoutAttribute() {
		if (layoutAttrTunable == null || layoutAttrTunable.layoutAttribute == null)
			return null;
		if (layoutAttrTunable.layoutAttribute.getSelectedValue().equals(UNWEIGHTED))
			return null;
		
		return layoutAttrTunable.layoutAttribute.getSelectedValue();
	}

	private Set<View<CyNode>> getLayoutNodes(CyLayoutAlgorithm layout, CyNetworkView networkView) {
		if (layout.getSupportsSelectedOnly() && selectedTunable.selectedNodesOnly) {
			Set<View<CyNode>> nodeViews = new HashSet<View<CyNode>>();
			CyNetwork network = networkView.getModel();
			for (View<CyNode> view : networkView.getNodeViews()) {
				if (network.getRow(view.getModel()).get(CyNetwork.SELECTED, Boolean.class) &&
						view.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) {
					nodeViews.add(view);
				}
			}
			return nodeViews;
		}
		return CyLayoutAlgorithm.ALL_NODE_VIEWS;
	}

	private NetworkViewTaskFactory wrapWithContext(final CyLayoutAlgorithm layout, final Object tunableContext) {
		return new NetworkViewTaskFactory() {
			@Override
			public boolean isReady(CyNetworkView networkView) {
				return layout.isReady(networkView, tunableContext, getLayoutNodes(layout, networkView), getLayoutAttribute());
			}
			
			@Override
			public TaskIterator createTaskIterator(CyNetworkView networkView) {
				return layout.createTaskIterator(networkView, tunableContext, getLayoutNodes(layout, networkView), getLayoutAttribute());
			}
		};
	}

	private class LayoutAlgorithmListCellRenderer extends DefaultListCellRenderer {
		
		private final static long serialVersionUID = 1202339874266209L;
		
		private String defaultText;
		
		LayoutAlgorithmListCellRenderer(final String defaultText) {
			this.defaultText = defaultText;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			// If this is a String, we don't want to allow selection.  If this is
			// index 0, we want to set the font 
			if (value instanceof CyLayoutAlgorithm) {
				setHorizontalAlignment(LEFT);
				setText(value.toString());
			} else {
				setText(defaultText);
				setHorizontalAlignment(CENTER);
				setForeground(Color.GRAY);
			}
			
			return this;
		}
	}
	
	private void setPanelsTransparent(final JPanel panel) {
		if (isAquaLAF()) {
			panel.setOpaque(false);
			
			for (int i = 0; i < panel.getComponentCount(); i++) {
				final Component c = panel.getComponent(i);
				
				if (c instanceof JPanel)
					setPanelsTransparent((JPanel)c);
			}
		}
	}
	
	public static class SelectedTunable {
		@Tunable(description="Layout only selected nodes:")
		public boolean selectedNodesOnly;
	}
	
	public static class LayoutAttributeTunable {
		@Tunable(description="Edge attribute that contains the weights:", gravity=1.0)
		public ListSingleSelection<String> layoutAttribute;
	}
}
