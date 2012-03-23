/*
  File: LayoutSettingsDialog.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  Dout of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.internal.layout.ui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.layout.CyLayoutContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.PanelTaskManager;


/**
 *
 * The LayoutSettingsDialog is a dialog that provides an interface into all of the
 * various settings for layout algorithms.  Each CyLayoutAlgorithm must return a single
 * JPanel that provides all of its settings.
 */
public class LayoutSettingsDialog extends JDialog implements ActionListener {
	private final static long serialVersionUID = 1202339874277105L;
	private TaskFactory currentLayout = null;

	// Dialog components
	private JLabel titleLabel; // Our title
	private JPanel mainPanel; // The main content pane
	private JPanel buttonBox; // Our action buttons (Save Settings, Cancel, Execute, Done)
	private JComboBox algorithmSelector; // Which algorithm we're using
	private JPanel algorithmPanel; // The panel this algorithm uses

	private CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	private CySwingApplication desktop;
	private CyApplicationManager appMgr;
	private PanelTaskManager taskManager;
	private CyProperty cytoscapePropertiesServiceRef;
	private DynamicTaskFactoryProvisioner factoryProvisioner;
	private boolean initialized;
	/**
	 *  Store the layout context.
	 */
	private Map<CyLayoutAlgorithm, CyLayoutContext> contextMap;

	/**
	 * Creates a new LayoutSettingsDialog object.
	 */
	public LayoutSettingsDialog(final CyLayoutAlgorithmManager cyLayoutAlgorithmManager, 
	                            final CySwingApplication desktop,
	                            final CyApplicationManager appMgr,
	                            final PanelTaskManager taskManager,
	                            final CyProperty cytoscapePropertiesServiceRef)
	{
		super(desktop.getJFrame(), "Layout Settings", false);

		initializeOnce(); // Initialize the components we only do once

		initComponents();
		
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		this.desktop = desktop;
		this.appMgr = appMgr;
		this.taskManager = taskManager;
		this.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		this.factoryProvisioner = new DynamicTaskFactoryProvisioner(appMgr);
		this.contextMap = new HashMap<CyLayoutAlgorithm, CyLayoutContext>();
		
		Properties props = (Properties)this.cytoscapePropertiesServiceRef.getProperties();
		
		//
		String pref = props.getProperty("preferredLayoutAlgorithm", "force-directed");		
		this.lbSelectLayoutAlgorithm.setText("Default preferred layout algorithm is "+pref);

		this.pack();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// Are we the source of the event?
		String command = e.getActionCommand();

		if (command.equals("done"))
			setVisible(false);
		else if (command.equals("execute")) {
			taskManager.execute(currentLayout.createTaskIterator());
		} else {
			// OK, initialize and display
			if (isVisible()) {
				requestFocus();
			} else {
				if (!initialized) {
					initialize();
					setLocationRelativeTo(desktop.getJFrame());
					pack();
				}
				setVisible(true);
				initialized = true;
			}
		}
	}


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnlLayoutSettings = new javax.swing.JPanel();
        pnlSetPreferredLayout = new javax.swing.JPanel();
        lbSelectLayoutAlgorithm = new javax.swing.JLabel();
        cmbLayoutAlgorithms = new javax.swing.JComboBox();
        pnlButtons = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.setName("tabPanelLayoutSettings"); // NOI18N

        pnlLayoutSettings.setName("pnlLayoutSettings"); // NOI18N
        pnlLayoutSettings.setLayout(new java.awt.GridBagLayout());
//        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(cytoscape3gui.Cytoscape3GUIApp.class).getContext().getResourceMap(LayoutSettingsDialog.class);
//        jTabbedPane1.addTab("Layout Settings", pnlLayoutSettings); // NOI18N
        jTabbedPane1.addTab("Layout Settings", this.mainPanel);
        

        pnlSetPreferredLayout.setName("pnlSetPreferredLayout"); // NOI18N
        pnlSetPreferredLayout.setLayout(new java.awt.GridBagLayout());

        lbSelectLayoutAlgorithm.setText("Please select preferred layout algorithm."); // NOI18N
        lbSelectLayoutAlgorithm.setName("lbSelectLayoutAlgorithm"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 10, 5, 0);
        pnlSetPreferredLayout.add(lbSelectLayoutAlgorithm, gridBagConstraints);

        cmbLayoutAlgorithms.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
        cmbLayoutAlgorithms.setName("cmbLayoutAlgorithms"); // NOI18N
        cmbLayoutAlgorithms.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbLayoutAlgorithmsItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        pnlSetPreferredLayout.add(cmbLayoutAlgorithms, gridBagConstraints);

        pnlButtons.setName("pnlButtons"); // NOI18N

        btnOK.setText("OK"); // NOI18N
        btnOK.setName("btnOK"); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        pnlButtons.add(btnOK);

        btnCancel.setText("Cancel"); // NOI18N
        btnCancel.setName("btnCancel"); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(btnCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 20, 10);
        pnlSetPreferredLayout.add(pnlButtons, gridBagConstraints);

        jTabbedPane1.addTab("Set preferred layout", pnlSetPreferredLayout); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jTabbedPane1, gridBagConstraints);

        pack();
    }// </editor-fold>

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {
		CyLayoutAlgorithm layout = (CyLayoutAlgorithm) this.cmbLayoutAlgorithms.getSelectedItem();

		String pref = layout.getName(); 
		
		Properties props = (Properties) this.cytoscapePropertiesServiceRef.getProperties();
		if(props != null) 
			props.setProperty("preferredLayoutAlgorithm", pref);

		this.dispose();
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
    	// Cancel button is clicked
    	this.dispose();
    }

    // Enable the OK button only if an algorithm is selected
    private void cmbLayoutAlgorithmsItemStateChanged(java.awt.event.ItemEvent evt) {
		Object o = this.cmbLayoutAlgorithms.getSelectedItem();
		// if it's a string, that means it's the instructions
		if (!(o instanceof String)) {
			this.btnOK.setEnabled(true);
		}
		else {
			this.btnOK.setEnabled(false);
		}
    }

    // Variables declaration - do not modify
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JComboBox cmbLayoutAlgorithms;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbSelectLayoutAlgorithm;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlLayoutSettings;
    private javax.swing.JPanel pnlSetPreferredLayout;
    // End of variables declaration

	
	
	
	private void initializeOnce() {
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		// Create our main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Create a panel for the list of algorithms
		JPanel algorithmSelectorPanel = new JPanel();
		algorithmSelector = new JComboBox();
		algorithmSelector.addActionListener(new AlgorithmActionListener());
		algorithmSelectorPanel.add(algorithmSelector);

		Border selBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(selBorder, "Layout Algorithm");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		algorithmSelectorPanel.setBorder(titleBorder);
		mainPanel.add(algorithmSelectorPanel);

		// Create a panel for algorithm's content
		this.algorithmPanel = new JPanel();
		mainPanel.add(algorithmPanel);

		// Create a panel for our button box
		this.buttonBox = new JPanel();

		JButton doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(this);

		JButton executeButton = new JButton("Execute Layout");
		executeButton.setActionCommand("execute");
		executeButton.addActionListener(this);

		buttonBox.add(executeButton);
		buttonBox.add(doneButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		mainPanel.add(buttonBox);
//		setContentPane(mainPanel);
	}

	private void initialize() {
		// Populate the algorithm selector
		algorithmSelector.removeAllItems();

		// Add the "instructions"
		algorithmSelector.setRenderer(new MyItemRenderer());
		algorithmSelector.addItem("Select algorithm to view settings");

		for ( CyLayoutAlgorithm algo : cyLayoutAlgorithmManager.getAllLayouts()) 
			algorithmSelector.addItem(algo);
		
		// For the tabbedPanel "Set preferred Layout"
		this.cmbLayoutAlgorithms.removeAllItems();
		
		// Add the "instructions"
		this.cmbLayoutAlgorithms.setRenderer(new MyItemRenderer());
		this.cmbLayoutAlgorithms.addItem("Select preferred algorithm");

		for ( CyLayoutAlgorithm algo : cyLayoutAlgorithmManager.getAllLayouts()) 
			this.cmbLayoutAlgorithms.addItem(algo);	
	}

	private class AlgorithmActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object o = algorithmSelector.getSelectedItem();
			// if it's a string, that means it's the instructions
			if (!(o instanceof String)) {
				final CyLayoutAlgorithm newLayout = (CyLayoutAlgorithm)o;
				//Checking if the context has already been charged, if so there is no need to do it again
				CyLayoutContext context = contextMap.get(newLayout);
				if (context == null)
				{
					context = newLayout.createLayoutContext();
					contextMap.put(newLayout, context);
				}
				TaskFactory provisioner = factoryProvisioner.createFor(wrapWithContext(newLayout, context));
				if (!provisioner.isReady()) {
					throw new IllegalArgumentException("Layout is not fully configured");
				}
				
				JPanel tunablePanel = taskManager.getConfiguration(provisioner, context);

				if (tunablePanel == null){
					JOptionPane.showMessageDialog(LayoutSettingsDialog.this, "Can not change setting for this algorithm, because tunable info is not avialable!", "Warning", JOptionPane.WARNING_MESSAGE);
					algorithmPanel.removeAll();
				}
				else {
					algorithmPanel.removeAll();
					algorithmPanel.add(tunablePanel);					
				}
				currentLayout = provisioner;
				LayoutSettingsDialog.this.pack();
			}
		}
	}

	private <T extends CyLayoutContext> NetworkViewTaskFactory wrapWithContext(final CyLayoutAlgorithm<T> layout, final T tunableContext) {
		return new NetworkViewTaskFactory() {
			@Override
			public boolean isReady(CyNetworkView networkView) {
				tunableContext.setNetworkView(networkView);
				return layout.isReady(tunableContext);
			}
			
			@Override
			public TaskIterator createTaskIterator(CyNetworkView networkView) {
				tunableContext.setNetworkView(networkView);
				return layout.createTaskIterator(tunableContext);
			}
		};
	}
	
	private class MyItemRenderer extends JLabel implements ListCellRenderer {
		private final static long serialVersionUID = 1202339874266209L;
		public MyItemRenderer() {
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			// If this is a String, we don't want to allow selection.  If this is
			// index 0, we want to set the font 
			Font f = getFont();

			if (value.getClass() == String.class) {
				setFont(f.deriveFont(Font.PLAIN));
				setText((String) value);
				setHorizontalAlignment(CENTER);
				setForeground(Color.GRAY);
				setEnabled(false);
			} else {
				setForeground(list.getForeground());
				setHorizontalAlignment(LEFT);
				setEnabled(true);

				if (isSelected) {
					setFont(f.deriveFont(Font.BOLD));
				} else {
					setFont(f.deriveFont(Font.PLAIN));
				}

				setText(value.toString());
			}

			return this;
		}
	}
}
