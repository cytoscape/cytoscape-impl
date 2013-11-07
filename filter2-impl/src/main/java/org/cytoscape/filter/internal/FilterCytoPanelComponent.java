package org.cytoscape.filter.internal;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.IconManager;
import org.cytoscape.filter.internal.view.LazyWorkQueue;
import org.cytoscape.filter.internal.view.SelectPanel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.internal.view.FilterWorker;
import org.cytoscape.filter.internal.view.TransformerWorker;
import org.cytoscape.filter.view.InteractivityChangedListener;

public class FilterCytoPanelComponent implements CytoPanelComponent2 {

	private static final String ID = "org.cytoscape.Filter";
	
	SelectPanel panel;

	public FilterCytoPanelComponent(TransformerManager transformerManager, TransformerViewManager transformerViewManager, CyApplicationManager applicationManager, IconManager iconManager, ModelMonitor modelMonitor) {
		LazyWorkQueue queue = new LazyWorkQueue();
		
		FilterWorker filterWorker = new FilterWorker(queue, applicationManager);
		final FilterPanelController filterPanelController = new FilterPanelController(transformerManager, transformerViewManager, filterWorker, modelMonitor);
		final FilterPanel filterPanel = new FilterPanel(filterPanelController, iconManager, filterWorker);
		filterPanel.setPreferredSize(new Dimension(450, 300));
		
		modelMonitor.addInteractivityChangedListener(new InteractivityChangedListener() {
			@Override
			public void handleInteractivityChanged(boolean isInteractive) {
				filterPanelController.setInteractive(isInteractive, filterPanel);
			}
		});
		
		TransformerWorker transformerWorker = new TransformerWorker(queue, applicationManager, transformerManager);
		TransformerPanelController transformerPanelController = new TransformerPanelController(transformerManager, transformerViewManager, filterPanelController, transformerWorker);
		TransformerPanel transformerPanel = new TransformerPanel(transformerPanelController, iconManager, transformerWorker);
		
		panel = new SelectPanel(filterPanel, transformerPanel);
	}
	
	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "Select";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
}
