package org.cytoscape.view.vizmap.gui.internal;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyTable extends JTable {

	private static final long serialVersionUID = -8052559216229363239L;

	private static final Logger logger = LoggerFactory.getLogger(DependencyTable.class);

	private final DefaultTableModel model;

	private final Collection<VisualPropertyDependency<?>> depList;
	private final CyApplicationManager appManager;

	final CyEventHelper cyEventHelper;
	
	public DependencyTable(final CyApplicationManager appManager, final CyEventHelper cyEventHelper,
			final DefaultTableModel model, final Collection<VisualPropertyDependency<?>> depList) {
		if (appManager == null)
			throw new NullPointerException();
		if (cyEventHelper == null)
			throw new NullPointerException();
		if (model == null)
			throw new NullPointerException();

		this.appManager = appManager;
		this.cyEventHelper = cyEventHelper;
		this.model = model;
		this.depList = depList;
		this.setModel(model);

		setAppearence();

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == MouseEvent.BUTTON1) {
					processMouseClick();
				}
			}
		});
	}

	private void processMouseClick() {
		final int selected = this.getSelectedRow();
		if(selected == -1)
			return;
		
		final Object selectedRowString = this.getValueAt(selected, 1);
				
		VisualPropertyDependency dep = null;
		for(VisualPropertyDependency dependency: depList) {
			if(dependency.getDisplayName().equals(selectedRowString.toString())) {
				dep = dependency;
				break;
			}
		}
		
		// Enable/disable dependency
		final boolean isDepend = (Boolean) model.getValueAt(selected, 0);
		dep.setDependency(isDepend);
		
		final Set<VisualProperty<?>> group = dep.getVisualProperties();
		
//		final RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
//		if (engine != null)	{
//			final VisualLexicon lexicon = engine.getVisualLexicon();
//			
//			for (final VisualProperty<?> vp : group)
//				lexicon.getVisualLexiconNode(vp).setDependency(isDepend);
//		}

		// Update other GUI component
		if (isDepend)
			this.cyEventHelper.fireEvent(new LexiconStateChangedEvent(this, null, group));
		else
			this.cyEventHelper.fireEvent(new LexiconStateChangedEvent(this, group, null));
	}

	private void setAppearence() {
		this.setRowHeight(30);
		this.setFont(new Font("SansSerif", Font.BOLD, 14));

		this.getColumnModel().setColumnSelectionAllowed(false);
		this.getTableHeader().setReorderingAllowed(false);

		getColumnModel().getColumn(0).setMaxWidth(50);
		getColumnModel().getColumn(0).setResizable(false);
		getColumnModel().getColumn(1).setMaxWidth(550);
		getColumnModel().getColumn(1).setResizable(false);

		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
}
