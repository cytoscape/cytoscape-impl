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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
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
@SuppressWarnings("serial")
public class LayoutSettingsDialog extends JDialog implements ActionListener {
	
	private static final String UNWEIGHTED = "(none)";
	
	private CyLayoutAlgorithm currentLayout;
	private TaskFactory currentAction;

	private JPanel settingsPnl;
	private JPanel buttonPnl;
	private JComboBox<CyLayoutAlgorithm> algorithmCmb;
	private JPanel algorithmPnl;
	private JPanel layoutAttrPnl;
	private JButton makeDefaultBtn;
    private JButton applyBtn;
    private JButton doneBtn;
    private JCheckBox selectedOnlyChk;

	private LayoutSettingsManager layoutSettingsMgr;
	private LayoutEdgeAttributeTunable layoutEdgeAttrTunable;
	private LayoutNodeAttributeTunable layoutNodeAttrTunable;
	
	private boolean initialized;
	private boolean initializing;
	
	private Set<CyLayoutAlgorithm> tunablesToSave = new HashSet<>();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * Creates a new LayoutSettingsDialog object.
	 */
	public LayoutSettingsDialog(
			final Window owner,
			final LayoutSettingsManager layoutSettingsMgr,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(owner, "Layout Settings", ModalityType.MODELESS);

		this.layoutSettingsMgr = layoutSettingsMgr;
		this.serviceRegistrar = serviceRegistrar;
		
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		pack();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// fired when user closes window using (x) button
				saveLayoutContexts();
			}
			@Override
			public void windowActivated(WindowEvent e) {
				// Update the combo-box selection in case the preferred layout has been changed by another task/action
				// after this dialog has been initialized (e.g. by an app or by using commands)
				updateMakeDefaultBtn();
			}
		});
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				// fired when user clicks "Done" button
				saveLayoutContexts();
			}
			@Override
			public void componentShown(ComponentEvent e) {
				tunablesToSave.add(currentLayout);
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Initialize and display
		if (isVisible()) {
			requestFocus();
		} else {
			if (!initialized) {
				initialize();
				updateMakeDefaultBtn();
				setLocationRelativeTo(getOwner());
				pack();
			}
			setVisible(true);
			initialized = true;
		}
		
		setNetworkView(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView());
	}
	
    void addLayout(CyLayoutAlgorithm layout) {
    	if (initialized) // Initialize again
			initialize();
    }

    void removeLayout(final CyLayoutAlgorithm layout) {
    	if (initialized) {
			if (currentLayout == layout)
				getAlgorithmPnl().removeAll();
			
			initialize();
    	}
    }

    private void initComponents() {
    	final JPanel contentPane = new JPanel();
    	final GroupLayout layout = new GroupLayout(contentPane);
    	contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getSettingsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getSettingsPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getButtonPnl(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
    	
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getApplyBtn().getAction(), getDoneBtn().getAction());
		getRootPane().setDefaultButton(getApplyBtn());
		
        pack();
    }

	private void initialize() {
		initializing = true;
		
		try {
			final Collator collator = Collator.getInstance(Locale.getDefault());
			final TreeSet<CyLayoutAlgorithm> allLayouts = new TreeSet<>(new Comparator<CyLayoutAlgorithm>() {
				@Override
				public int compare(CyLayoutAlgorithm o1, CyLayoutAlgorithm o2) {
					return collator.compare(o1.toString(), o2.toString());
				}
			});
			CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
			allLayouts.addAll(layoutMgr.getAllLayouts());
			if(currentLayout == null) {
				currentLayout = layoutMgr.getDefaultLayout();
			}
			
			// Populate the algorithm selector
			getAlgorithmCmb().removeAllItems();
			for (CyLayoutAlgorithm algo : allLayouts) {
				getAlgorithmCmb().addItem(algo);
			}
		} finally {
			initializing = false;
		}
		
		if (currentLayout != null)
			getAlgorithmCmb().setSelectedItem(currentLayout);
		else if (getAlgorithmCmb().getModel().getSize() > 0)
			getAlgorithmCmb().setSelectedIndex(0);
		
		updateMakeDefaultBtn();
	}
	
	private void updateMakeDefaultBtn() {
		CyLayoutAlgorithm defLayout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();
		boolean enabled = defLayout != null && !defLayout.equals(getAlgorithmCmb().getSelectedItem());
		getMakeDefaultBtn().setEnabled(enabled);
	}
	
	private JButton getMakeDefaultBtn() {
		if(makeDefaultBtn == null) {
			makeDefaultBtn = new JButton("Make Default");
			makeDefaultBtn.addActionListener(e -> {
				if (initializing)
					return; 
				CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
				CyLayoutAlgorithm layout = (CyLayoutAlgorithm) getAlgorithmCmb().getSelectedItem();
				if (layout != null && !layout.equals(layoutMgr.getDefaultLayout())) {
					layoutMgr.setDefaultLayout(layout);
				}
				initialize();
			});
		}
		return makeDefaultBtn;
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
				.addGroup(layout.createSequentialGroup()
					.addComponent(getAlgorithmCmb())
					.addComponent(getMakeDefaultBtn())
				)
				.addComponent(getAlgorithmPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(algoLbl)
				.addGroup(layout.createParallelGroup()
					.addComponent(getAlgorithmCmb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getMakeDefaultBtn())
				)
				.addComponent(getAlgorithmPnl(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return settingsPnl;
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

	public JPanel getButtonPnl() {
		if (buttonPnl == null) {
			buttonPnl = new JPanel();
			buttonPnl.setLayout(new BoxLayout(buttonPnl, BoxLayout.LINE_AXIS));
			buttonPnl.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
			
			buttonPnl.add(getSelectedOnlyChk());
			buttonPnl.add(Box.createHorizontalGlue());
			buttonPnl.add(getApplyBtn());
			buttonPnl.add(getDoneBtn());
		}
		
		return buttonPnl;
	}
	
	private JButton getApplyBtn() {
		if (applyBtn == null) {
			applyBtn = new JButton(new AbstractAction("Apply Layout") {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Object context = currentLayout.getDefaultLayoutContext();
					final PanelTaskManager taskMgr = serviceRegistrar.getService(PanelTaskManager.class);
					
					if (taskMgr.validateAndApplyTunables(context))
						taskMgr.execute(currentAction.createTaskIterator());
				}
			});
		}
		
		return applyBtn;
	}
	
	public JButton getDoneBtn() {
		if (doneBtn == null) {
			doneBtn = new JButton(new AbstractAction("Done") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		
		return doneBtn;
	}
	
	private JComboBox<CyLayoutAlgorithm> getAlgorithmCmb() {
		if (algorithmCmb == null) {
			algorithmCmb = new JComboBox<>();
			algorithmCmb.setRenderer(new LayoutAlgorithmListCellRenderer("-- Select algorithm to view settings --"));
			
			algorithmCmb.addActionListener(e -> {
				if (initializing)
					return;
				
				final Object o = algorithmCmb.getSelectedItem();
				
				if (o instanceof CyLayoutAlgorithm) {
					currentLayout = (CyLayoutAlgorithm) o;
					final PanelTaskManager taskMgr = serviceRegistrar.getService(PanelTaskManager.class);
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					
					//Checking if the context has already been charged, if so there is no need to do it again
					final Object context = currentLayout.getDefaultLayoutContext();
					tunablesToSave.add(currentLayout);

					final DynamicTaskFactoryProvisioner factoryProvisioner = serviceRegistrar.getService(DynamicTaskFactoryProvisioner.class);
					final TaskFactory provisioner = factoryProvisioner.createFor(wrapWithContext(currentLayout, context));
					final JPanel tunablePnl = taskMgr.getConfiguration(provisioner, context);

					layoutAttrPnl = new JPanel();
					layoutAttrPnl.setLayout(new BoxLayout(layoutAttrPnl, BoxLayout.PAGE_AXIS));
					layoutAttrPnl.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
					
					final CyNetworkView view = appMgr.getCurrentNetworkView();
					setNetworkView(view);

					getAlgorithmPnl().removeAll();
					getAlgorithmPnl().add(layoutAttrPnl);
					
					if (tunablePnl != null) {
						tunablePnl.setAlignmentX(Component.CENTER_ALIGNMENT);
						setPanelsTransparent(tunablePnl);
						getAlgorithmPnl().add(tunablePnl);
					}
					
					boolean selectedNodes = currentLayout.getSupportsSelectedOnly() && hasSelectedNodes(view);
					getSelectedOnlyChk().setVisible(selectedNodes);
					getSelectedOnlyChk().setSelected(selectedNodes);
					
					currentAction = provisioner;
					LayoutSettingsDialog.this.pack();
				}
				
				updateMakeDefaultBtn();
			});
		}
		
		return algorithmCmb;
	}
	
	private JCheckBox getSelectedOnlyChk() {
		if(selectedOnlyChk == null) {
			selectedOnlyChk = new JCheckBox("Layout only selected nodes");
		}
		return selectedOnlyChk;
	}
	
	
	void setNetworkView(final CyNetworkView view) {
		getApplyBtn().setEnabled(view != null);
		
		if (layoutAttrPnl == null)
			return;
		
		boolean haveNodeAttribute = (currentLayout.getSupportedNodeAttributeTypes() != null && currentLayout.getSupportedNodeAttributeTypes().size() > 0);
		boolean haveEdgeAttribute = (currentLayout.getSupportedEdgeAttributeTypes() != null && currentLayout.getSupportedEdgeAttributeTypes().size() > 0);
		layoutAttrPnl.removeAll();
		if (currentLayout.getSupportedEdgeAttributeTypes() != null && currentLayout.getSupportedEdgeAttributeTypes().size() > 0)
			layoutEdgeAttrTunable = new LayoutEdgeAttributeTunable();
		else if (currentLayout.getSupportedNodeAttributeTypes() != null && currentLayout.getSupportedNodeAttributeTypes().size() > 0)
			layoutNodeAttrTunable = new LayoutNodeAttributeTunable();
		
		if (view != null) {
			List<String> attributeList = getAttributeList(view.getModel(),
					currentLayout.getSupportedNodeAttributeTypes(), currentLayout.getSupportedEdgeAttributeTypes());
			
			if (attributeList.size() > 0) {
				final PanelTaskManager taskMgr = serviceRegistrar.getService(PanelTaskManager.class);
				JPanel panel = null;
				if (haveEdgeAttribute) {
					layoutEdgeAttrTunable = new LayoutEdgeAttributeTunable();
					layoutEdgeAttrTunable.layoutAttribute = new ListSingleSelection<String>(attributeList);
					layoutEdgeAttrTunable.layoutAttribute.setSelectedValue(attributeList.get(0));
					panel = taskMgr.getConfiguration(null, layoutEdgeAttrTunable);
				} else if (haveNodeAttribute) {
					layoutNodeAttrTunable = new LayoutNodeAttributeTunable();
					layoutNodeAttrTunable.layoutAttribute = new ListSingleSelection<String>(attributeList);
					layoutNodeAttrTunable.layoutAttribute.setSelectedValue(attributeList.get(0));
					panel = taskMgr.getConfiguration(null, layoutNodeAttrTunable);
				}
				
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
		CyTable table; if (allowedNodeAttributeTypes.size() > 0) { allowedTypes = allowedNodeAttributeTypes;
			table = network.getDefaultNodeTable();
		} else if (allowedEdgeAttributeTypes.size() > 0) {
			allowedTypes = allowedEdgeAttributeTypes;
			table = network.getDefaultEdgeTable();
		} else {
			return attributes;
		}
		
		for (final CyColumn column : table.getColumns()) {
						if (column.getName().equals(CyNetwork.SELECTED) ||
						    column.getName().equals(CyNetwork.SUID))
							continue;

            if (allowedTypes.contains(column.getType())) {
            	attributes.add(column.getName());
            }
		}

		Collections.sort(attributes);
		
		if (attributes.size()>0)
			attributes.add(0, UNWEIGHTED);
        return attributes;
	}
	
	private String getLayoutAttribute() {
		if ((layoutEdgeAttrTunable != null && layoutEdgeAttrTunable.layoutAttribute != null)) {
			if (layoutEdgeAttrTunable.layoutAttribute.getSelectedValue().equals(UNWEIGHTED))
				return null;
			return layoutEdgeAttrTunable.layoutAttribute.getSelectedValue();
		} else if ((layoutNodeAttrTunable != null && layoutNodeAttrTunable.layoutAttribute != null)) {
			if (layoutNodeAttrTunable.layoutAttribute.getSelectedValue().equals(UNWEIGHTED))
				return null;
			return layoutNodeAttrTunable.layoutAttribute.getSelectedValue();
		}

		return null;
	}

	private Set<View<CyNode>> getLayoutNodes(CyLayoutAlgorithm layout, CyNetworkView networkView) {
		if (layout.getSupportsSelectedOnly() && getSelectedOnlyChk().isSelected()) {
			Set<View<CyNode>> nodeViews = new HashSet<>();
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
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			CyLayoutAlgorithm defaultLayout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();
			
			// If this is a String, we don't want to allow selection.  If this is
			// index 0, we want to set the font 
			if (value instanceof CyLayoutAlgorithm) {
				setHorizontalAlignment(LEFT);
				if(value.equals(defaultLayout)) {
					setText(value.toString() + " (default)");
				} else {
					setText(value.toString());
				}
			} else {
				setText(defaultText);
				setHorizontalAlignment(CENTER);
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
	
	private void saveLayoutContexts() {
		final PanelTaskManager taskMgr = serviceRegistrar.getService(PanelTaskManager.class);

		for (CyLayoutAlgorithm layout : tunablesToSave) {
			layoutSettingsMgr.saveLayoutContext(taskMgr, layout);
		}

		tunablesToSave.clear();
	}
	
	
	public static class LayoutEdgeAttributeTunable {
		@Tunable(description="Edge column that contains the weights:", gravity=1.0)
		public ListSingleSelection<String> layoutAttribute;
	}
	
	public static class LayoutNodeAttributeTunable {
		@Tunable(description="Node column to use:", gravity=1.0)
		public ListSingleSelection<String> layoutAttribute;
	}
}
