package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import static org.cytoscape.work.ServiceProperties.*;

/**
 * Creates Visual Style Bypass menu.
 * 
 */
public final class BypassManager {
	
	private final CyServiceRegistrar registrar;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;

	public BypassManager(final CyServiceRegistrar registrar, final EditorManager editorManager,
			final VisualMappingManager vmm) {
		this.registrar = registrar;
		this.editorManager = editorManager;
		this.vmm = vmm;
	}

	public void addBypass(RenderingEngineFactory<?> factory, Map props) {
		// TODO: Replace this with Marker interface.
		if (props.containsValue("ding") == false)
			return;

		final VisualLexicon lexicon = factory.getVisualLexicon();

		final VisualProperty<?> nodeRoot = BasicVisualLexicon.NODE;
		final VisualProperty<?> edgeRoot = BasicVisualLexicon.EDGE;

		// Tree traversal
		final VisualLexiconNode nodeRootNode = lexicon.getVisualLexiconNode(nodeRoot);
		final VisualLexiconNode edgeRootNode = lexicon.getVisualLexiconNode(edgeRoot);
		buildMenu(nodeRootNode, edgeRootNode, lexicon);
	}

	public void removeBypass(RenderingEngineFactory<?> factory, Map props) {
		// TODO: implement this
	}

	private void buildMenu(final VisualLexiconNode rootNode, final VisualLexiconNode rootEdge,
			final VisualLexicon lexicon) {
		// Create root menu
		final Properties nodeProp = new Properties();
		nodeProp.setProperty("preferredTaskManager", "menu");
		nodeProp.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
		nodeProp.setProperty(MENU_GRAVITY, "-1");
		final NodeBypassContextMenuFactory ntf = new NodeBypassContextMenuFactory(rootNode, editorManager, vmm, lexicon);
		registrar.registerService(ntf, CyNodeViewContextMenuFactory.class, nodeProp);

		final Properties edgeProp = new Properties();
		edgeProp.setProperty("preferredTaskManager", "menu");
		edgeProp.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
		edgeProp.setProperty(MENU_GRAVITY, "-1");
		final EdgeBypassContextMenuFactory etf = new EdgeBypassContextMenuFactory(rootEdge, editorManager, vmm, lexicon);
		registrar.registerService(etf, CyEdgeViewContextMenuFactory.class, edgeProp);
	}
}
