package org.cytoscape.filter.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterSettingsManager implements SessionAboutToBeSavedListener, SessionAboutToBeLoadedListener, SessionLoadedListener {
	static final Logger logger = LoggerFactory.getLogger(FilterSettingsManager.class);
	static final String SESSION_NAMESPACE = "org.cytoscape.filter";
	
	final FilterPanel filterPanel;
	final TransformerPanel transformerPanel;
	private FilterIO filterIo;
	
	public FilterSettingsManager(FilterPanel filterPanel, TransformerPanel transformerPanel, FilterIO filterIo) {
		this.filterPanel = filterPanel;
		this.transformerPanel = transformerPanel;
		this.filterIo = filterIo;
	}
	
	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent e) {
		filterPanel.getController().reset(filterPanel);
		transformerPanel.getController().reset(transformerPanel);
		addDefaultsIfEmpty();
	}
	
	private void addDefaultsIfEmpty() {
		FilterPanelController filterPanelController = filterPanel.getController();
		if (filterPanelController.getElementCount() == 0) {
			filterPanelController.addNewElement("Default filter");
		}
		
		TransformerPanelController transformerPanelController = transformerPanel.getController();
		if (transformerPanelController.getElementCount() == 0) {
			transformerPanelController.addNewElement("Default chain");
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		List<File> files = event.getLoadedSession().getAppFileListMap().get(SESSION_NAMESPACE);
		if (files == null) {
			return;
		}
		
		filterPanel.getController().reset(filterPanel);
		transformerPanel.getController().reset(transformerPanel);
		
		for (File file : files) {
			try {
				if (file.getName().equals("filters.json")) {
					filterIo.readTransformers(file, filterPanel);
				} else if (file.getName().equals("filterChains.json")) {
					filterIo.readTransformers(file, transformerPanel);
				}
			} catch (IOException e) {
				logger.error("Unexpected error", e);
			}
		}
		
		addDefaultsIfEmpty();
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		FilterPanelController filterPanelController = filterPanel.getController();
		TransformerPanelController transformerPanelController = transformerPanel.getController();

		List<File> files = new ArrayList<>();
		try {
			File root = File.createTempFile(SESSION_NAMESPACE, ".temp");
			root.delete();
			root.mkdir();
			root.deleteOnExit();
			
			File filtersFile = new File(root, "filters.json");
			filterIo.writeFilters(filtersFile, filterPanelController.getNamedTransformers());
			files.add(filtersFile);
			
			File filterChainsFile = new File(root, "filterChains.json");
			filterIo.writeFilters(filterChainsFile, transformerPanelController.getNamedTransformers());
			files.add(filterChainsFile);
			
			for (File file : files) {
				file.deleteOnExit();
			}
			event.addAppFiles(SESSION_NAMESPACE, files);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
	}
}
