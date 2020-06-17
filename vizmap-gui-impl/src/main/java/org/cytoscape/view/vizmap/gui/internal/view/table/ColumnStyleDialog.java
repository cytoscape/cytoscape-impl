package org.cytoscape.view.vizmap.gui.internal.view.table;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

@SuppressWarnings("serial")
@Deprecated
public class ColumnStyleDialog extends JDialog {
	
	private final CyServiceRegistrar registrar;
	
	private final CyColumn column;
	private final CyTable table;
	private final CyColumnView columnView;
	private final CyTableView tableView;
	private final RenderingEngine<CyTable> renderingEngine;
	
	@SuppressWarnings("unchecked")
	public ColumnStyleDialog(CyColumn column, CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.column = column;
		this.table = column.getTable();
		setMinimumSize(new Dimension(500, 500));
		setMaximumSize(new Dimension(700, 600));
		setTitle("Column Style: " + table.getTitle());
		setModal(true);
		
		// get View objects
		CyTableViewManager tableViewManager = registrar.getService(CyTableViewManager.class);
		tableView = tableViewManager.getTableView(table);
		columnView = (CyColumnView) tableView.getColumnView(column);
		
		// get the rendering engine
		RenderingEngineManager renderingEngineManager = registrar.getService(RenderingEngineManager.class);
		renderingEngine = (RenderingEngine<CyTable>) renderingEngineManager.getRenderingEngines(tableView).iterator().next();
		
		createContents();
	}
	
	
	private void createContents() {
		JPanel targetColPanel = createTargetColumnPanel();
		JPanel vpPanel = createVPPanel();
		JPanel stylePanel = createStyleEditPanel();
		
		setLayout(new BorderLayout());
		add(targetColPanel, BorderLayout.NORTH);
		add(vpPanel, BorderLayout.WEST);
		add(stylePanel, BorderLayout.CENTER);
	}
	
	
	private JPanel createTargetColumnPanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Target Column:");
		JLabel targetColumnLabel = new JLabel();
		
		var presetationManager = registrar.getService(CyColumnPresentationManager.class);
		presetationManager.setLabel(column.getName(), targetColumnLabel);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE, true)
				.addComponent(label)
				.addComponent(targetColumnLabel)
		);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(targetColumnLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		return panel;
	}

	private JPanel createVPPanel() {
		DefaultListModel<VisualProperty<?>> model = new DefaultListModel<>();
		JList<VisualProperty<?>> vpList = new JList<>(model);
		vpList.setCellRenderer(new VPListCellRenderer());
		
		addVpsToList(model, BasicTableVisualLexicon.CELL);
		addVpsToList(model, BasicTableVisualLexicon.COLUMN);
		
		JScrollPane scrollPane = new JScrollPane(vpList);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}
	
	private void addVpsToList(DefaultListModel<VisualProperty<?>> model, VisualProperty<?> rootVp) {
		VisualLexicon lexicon = renderingEngine.getVisualLexicon();
		Collection<VisualProperty<?>> vps = lexicon.getAllDescendants(rootVp);
		for(VisualProperty<?> vp : vps) {
			if(lexicon.isSupported(vp)) {
				model.addElement(vp);
			}
		}
	}
	
	private class VPListCellRenderer extends DefaultListCellRenderer {
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean focus) {
			super.getListCellRendererComponent(list, value, index, isSelected, focus);
			VisualProperty vp = (VisualProperty) value;
			setText(vp.getDisplayName());
			return this;
		}
	}

	private JPanel createStyleEditPanel() {
		JPanel panel = new JPanel();
		return panel;
	}
}
