/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedListener;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedListener;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyFilter;
import org.cytoscape.view.vizmap.gui.internal.util.VizMapperUtil;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog for editing default visual property values.<br>
 * This is a modal dialog.
 * 
 * <p>
 * Basic idea is the following:
 * <ul>
 * <li>Build dummy network with 2 nodes and 1 edge.</li>
 * <li>Edit the default appearence of the dummy network</li>
 * <li>Create a image from the dummy.</li>
 * </ul>
 * </p>
 * 
 */
public class DefaultViewEditorImpl extends JDialog implements DefaultViewEditor, SelectedVisualStyleSwitchedListener,
		LexiconStateChangedListener {

	private final static long serialVersionUID = 1202339876675416L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultViewEditorImpl.class);

	private static final int ICON_WIDTH = 48;
	private static final int ICON_HEIGHT = 48;

	private final Map<Class<? extends CyIdentifiable>, Set<VisualProperty<?>>> vpSets;
	private final Map<Class<? extends CyIdentifiable>, JList> listMap;

	private final CyApplicationManager cyApplicationManager;

	private final EditorManager editorFactory;
	private final VisualMappingManager vmm;
	private final SelectedVisualStyleManager selectedManager;

	private final VizMapperUtil util;

	private final DefaultViewPanelImpl mainView;

	private final CyEventHelper cyEventHelper;

	private DependencyTable depTable;

	public DefaultViewEditorImpl(final DefaultViewPanelImpl mainView, final EditorManager editorFactory,
			final CyApplicationManager cyApplicationManager, final VisualMappingManager vmm,
			final SelectedVisualStyleManager selectedManager, final VizMapperUtil util,
			final CyEventHelper cyEventHelper) {
		super();

		if (mainView == null)
			throw new NullPointerException("DefaultViewPanel is null.");

		if (vmm == null)
			throw new NullPointerException("Visual Mapping Manager is null.");

		this.cyEventHelper = cyEventHelper;

		this.vmm = vmm;
		this.util = util;
		this.selectedManager = selectedManager;
		vpSets = new HashMap<Class<? extends CyIdentifiable>, Set<VisualProperty<?>>>();
		listMap = new HashMap<Class<? extends CyIdentifiable>, JList>();

		this.cyApplicationManager = cyApplicationManager;
		this.setModal(true);
		this.mainView = mainView;
		this.editorFactory = editorFactory;

		updateVisualPropertyLists();

		initComponents();
		buildList();

		// Listening to resize event.
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				defaultObjectTabbedPane.repaint();
				mainView.updateView();
			}
		});

	}

	private void updateVisualPropertyLists() {
		vpSets.clear();

		vpSets.put(CyNode.class, getLeafNodes(util.getVisualPropertySet(CyNode.class)));
		vpSets.put(CyEdge.class, getLeafNodes(util.getVisualPropertySet(CyEdge.class)));
		vpSets.put(CyNetwork.class, getNetworkLeafNodes(util.getVisualPropertySet(CyNetwork.class)));
	}

	private Set<VisualProperty<?>> getLeafNodes(final Collection<VisualProperty<?>> props) {

		final Set<VisualLexicon> lexSet = vmm.getAllVisualLexicon();

		final Set<VisualProperty<?>> propSet = new TreeSet<VisualProperty<?>>(new VisualPropertyComparator());

		for (VisualLexicon lexicon : lexSet) {
			for (VisualProperty<?> vp : props) {
				if (lexicon.getVisualLexiconNode(vp).getChildren().size() == 0)
					propSet.add(vp);
			}
		}
		return propSet;

	}

	private Set<VisualProperty<?>> getNetworkLeafNodes(final Collection<VisualProperty<?>> props) {
		final Set<VisualLexicon> lexSet = vmm.getAllVisualLexicon();

		final Set<VisualProperty<?>> propSet = new TreeSet<VisualProperty<?>>(new VisualPropertyComparator());

		for (VisualLexicon lexicon : lexSet) {
			for (VisualProperty<?> vp : props) {
				if (lexicon.getVisualLexiconNode(vp).getChildren().size() == 0
						&& lexicon.getVisualLexiconNode(vp).getParent().getVisualProperty() == BasicVisualLexicon.NETWORK)
					propSet.add(vp);
			}
		}
		return propSet;

	}

	@Override
	public void showEditor(Component parent) {
		updateVisualPropertyLists();
		buildList();

		updateDependencyTable();

		mainView.updateView();
		setSize(900, 450);
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void updateDependencyTable() {

		final VisualStyle selectedStyle = selectedManager.getCurrentVisualStyle();
		final Set<VisualPropertyDependency<?>> dependencies = selectedStyle.getAllVisualPropertyDependencies();
		final DependencyTableModel depTableModel = new DependencyTableModel();

		for (VisualPropertyDependency<?> dep : dependencies) {
			final Object[] newRow = new Object[2];
			newRow[0] = dep.isDependencyEnabled();
			newRow[1] = dep.getDisplayName();
			depTableModel.addRow(newRow);
		}

		depTable = new DependencyTable(cyApplicationManager, cyEventHelper, depTableModel, dependencies);
		dependencyScrollPane.setViewportView(depTable);
		depTable.repaint();
	}

	private void initComponents() {
		jXPanel1 = new JXPanel();
		jXTitledPanel1 = new org.jdesktop.swingx.JXTitledPanel();
		defaultObjectTabbedPane = new javax.swing.JTabbedPane();
		nodeScrollPane = new javax.swing.JScrollPane();
		dependencyScrollPane = new javax.swing.JScrollPane();

		nodeList = new JXList();
		edgeList = new JXList();
		edgeScrollPane = new javax.swing.JScrollPane();
		globalScrollPane = new javax.swing.JScrollPane();
		applyButton = new javax.swing.JButton();

		networkList = new JXList();

		listMap.put(CyNode.class, nodeList);
		listMap.put(CyEdge.class, edgeList);
		listMap.put(CyNetwork.class, networkList);

		cancelButton = new javax.swing.JButton();
		cancelButton.setVisible(false);

		nodeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listActionPerformed(e);
			}
		});

		edgeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listActionPerformed(e);
			}
		});

		networkList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listActionPerformed(e);
			}
		});

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jXTitledPanel1.setTitle("Default Visual Properties");

		jXTitledPanel1.setTitleFont(new java.awt.Font("SansSerif", 1, 12));
		jXTitledPanel1.setMinimumSize(new java.awt.Dimension(300, 27));
		jXTitledPanel1.setPreferredSize(new java.awt.Dimension(300, 27));
		defaultObjectTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

		nodeScrollPane.setViewportView(nodeList);
		edgeScrollPane.setViewportView(edgeList);
		globalScrollPane.setViewportView(networkList);
		dependencyScrollPane.setViewportView(depTable);

		defaultObjectTabbedPane.addTab("Node", nodeScrollPane);
		defaultObjectTabbedPane.addTab("Edge", edgeScrollPane);
		defaultObjectTabbedPane.addTab("Network", globalScrollPane);

		defaultObjectTabbedPane.addTab("Dependency", dependencyScrollPane);

		GroupLayout jXTitledPanel1Layout = new GroupLayout(jXTitledPanel1.getContentContainer());
		jXTitledPanel1.getContentContainer().setLayout(jXTitledPanel1Layout);
		jXTitledPanel1Layout.setHorizontalGroup(jXTitledPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(defaultObjectTabbedPane, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE));
		jXTitledPanel1Layout.setVerticalGroup(jXTitledPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(defaultObjectTabbedPane, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE));

		applyButton.setText("Apply");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
				if (view != null)
					applyNewStyle(view);
				dispose();
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				dispose();
			}
		});

		GroupLayout jXPanel1Layout = new GroupLayout(jXPanel1);
		jXPanel1.setLayout(jXPanel1Layout);
		jXPanel1Layout.setHorizontalGroup(jXPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jXPanel1Layout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								jXPanel1Layout
										.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addGroup(
												jXPanel1Layout.createSequentialGroup()
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(cancelButton)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(applyButton))
										.addComponent(mainView, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jXTitledPanel1, GroupLayout.PREFERRED_SIZE, 198, Short.MAX_VALUE)
						.addGap(12, 12, 12)));
		jXPanel1Layout
				.setVerticalGroup(jXPanel1Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								GroupLayout.Alignment.TRAILING,
								jXPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jXPanel1Layout
														.createParallelGroup(GroupLayout.Alignment.TRAILING)
														.addComponent(jXTitledPanel1, GroupLayout.Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
														.addGroup(
																jXPanel1Layout
																		.createSequentialGroup()
																		.addComponent(mainView,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jXPanel1Layout
																						.createParallelGroup(
																								GroupLayout.Alignment.BASELINE)
																						.addComponent(cancelButton)
																						.addComponent(applyButton))))
										.addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jXPanel1,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jXPanel1,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		pack();
	} // </editor-fold>

	private <V> void listActionPerformed(MouseEvent e) {
		final Object source = e.getSource();
		final JList list;

		if (source instanceof JList)
			list = (JList) source;
		else
			return;

		V newValue = null;

		@SuppressWarnings("unchecked")
		final VisualProperty<V> vp = (VisualProperty<V>) list.getSelectedValue();

		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
			final VisualStyle selectedStyle = selectedManager.getCurrentVisualStyle();
			final V defaultVal = selectedStyle.getDefaultValue(vp);
			try {
				if (defaultVal != null)
					newValue = editorFactory.showVisualPropertyValueEditor(this, vp, defaultVal);
				else
					newValue = editorFactory.showVisualPropertyValueEditor(this, vp, vp.getDefault());
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if (newValue != null) {
				// Got new value. Apply to the dummy view.
				selectedStyle.setDefaultValue(vp, newValue);
				mainView.updateView();
				mainView.repaint();
			}

			repaint();
		}
	}

	private void applyNewStyle(final CyNetworkView view) {
		final VisualStyle selectedStyle = selectedManager.getCurrentVisualStyle();

		vmm.setVisualStyle(selectedStyle, view);
		selectedStyle.apply(view);
		view.updateView();
	}

	// Variables declaration - do not modify
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JScrollPane nodeScrollPane;
	private javax.swing.JScrollPane edgeScrollPane;
	private javax.swing.JScrollPane globalScrollPane;

	// New from 3.0
	private javax.swing.JScrollPane dependencyScrollPane;

	private javax.swing.JTabbedPane defaultObjectTabbedPane;
	private JXList nodeList;
	private JXList edgeList;
	private JXList networkList;
	private org.jdesktop.swingx.JXPanel jXPanel1;

	private org.jdesktop.swingx.JXTitledPanel jXTitledPanel1;

	// End of variables declaration

	/**
	 * Populate the list model based on current lexicon tree structure.
	 */
	private void buildList() {

		final VisualPropCellRenderer renderer = new VisualPropCellRenderer();
		final RenderingEngine<CyNetwork> currentEngine = this.cyApplicationManager.getCurrentRenderingEngine();
		if (currentEngine == null)
			return;

		final VisualLexicon lex = currentEngine.getVisualLexicon();
		
		final VisualStyle selectedStyle = selectedManager.getCurrentVisualStyle();

		for (Class<? extends CyIdentifiable> key : vpSets.keySet()) {
			final DefaultListModel model = new DefaultListModel();
			final JList list = listMap.get(key);

			list.setModel(model);
			final Set<VisualProperty<?>> vps = vpSets.get(key);
			for (final VisualProperty<?> vp : vps) {

				// Check supported or not.
				if (VisualPropertyFilter.isCompatible(vp) == false)
					continue;

				// Filter based on mode
				if (PropertySheetUtil.isAdvancedMode() == false) {
					if (PropertySheetUtil.isBasic(vp) == false)
						continue;
				}

				// Do not allow editing of the following two VP
				if (vp.getDisplayName().contains("Edge Target Arrow Selected Paint")
						|| vp.getDisplayName().contains("Edge Source Arrow Selected Paint")) {
					continue;
				}

				// Filter based on dependency:
				final VisualLexiconNode treeNode = lex.getVisualLexiconNode(vp);
				if (treeNode != null)
					model.addElement(vp);
				
				// Override dependency
				final Set<VisualPropertyDependency<?>> dependencies = selectedStyle.getAllVisualPropertyDependencies();
				for (VisualPropertyDependency<?> dep : dependencies) {
					if (dep.isDependencyEnabled()) {
						Set<VisualProperty<?>> props = dep.getVisualProperties();
						final VisualProperty<?> parentVP = dep.getParentVisualProperty();
						if (model.contains(parentVP) == false)
							model.addElement(parentVP);
						
						for (VisualProperty<?> prop : props)
							model.removeElement(prop);
					}
				}
			}
			list.setCellRenderer(renderer);
		}
	}

	private final class VisualPropCellRenderer extends JLabel implements ListCellRenderer {

		private static final long serialVersionUID = -1325179272895141114L;

		private final Font SELECTED_FONT = new Font("SansSerif", Font.ITALIC, 14);
		private final Font NORMAL_FONT = new Font("SansSerif", Font.BOLD, 12);
		private final Color SELECTED_COLOR = new Color(10, 50, 180, 20);
		private final Color SELECTED_FONT_COLOR = new Color(0, 150, 255, 150);

		private final int ICON_GAP = 55;

		VisualPropCellRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			final VisualStyle selectedStyle = selectedManager.getCurrentVisualStyle();

			Icon icon = null;
			VisualProperty<Object> vp = null;

			if (value instanceof VisualProperty<?>) {
				vp = (VisualProperty<Object>) value;

				final RenderingEngine<?> presentation = cyApplicationManager.getCurrentRenderingEngine();

				if (presentation != null) {
					final Object defValue = selectedStyle.getDefaultValue(vp);
					icon = presentation.createIcon(vp, selectedStyle.getDefaultValue(vp), ICON_WIDTH, ICON_HEIGHT);

					if (defValue != null)
						setToolTipText(defValue.toString());
				}
			}

			setText(vp.getDisplayName());

			setIcon(icon);
			setFont(isSelected ? SELECTED_FONT : NORMAL_FONT);

			this.setVerticalTextPosition(CENTER);
			this.setVerticalAlignment(CENTER);
			this.setIconTextGap(ICON_GAP);

			setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
			setForeground(isSelected ? SELECTED_FONT_COLOR : list.getForeground());

			if (icon != null) {
				setPreferredSize(new Dimension(250, icon.getIconHeight() + 24));
			} else {
				setPreferredSize(new Dimension(250, 55));
			}

			this.setBorder(new DropShadowBorder());

			return this;
		}
	}

	@Override
	public Component getDefaultView(final VisualStyle vs) {
		mainView.updateView(vs);
		return mainView;
	}

	public void handleEvent(SelectedVisualStyleSwitchedEvent e) {

		final VisualStyle selectedStyle = e.getNewVisualStyle();
		setTitle("Default Appearance for " + selectedStyle.getTitle());

	}

	private static class VisualPropertyComparator implements Comparator<VisualProperty<?>> {

		@Override
		public int compare(VisualProperty<?> vp1, VisualProperty<?> vp2) {
			String name1 = vp1.getDisplayName();
			String name2 = vp2.getDisplayName();

			return name1.compareTo(name2);
		}

	}

	@Override
	public void handleEvent(LexiconStateChangedEvent e) {
		logger.debug("Def editor got Lexicon update event.");
		buildList();

		mainView.updateView();
		mainView.repaint();

	}
}
