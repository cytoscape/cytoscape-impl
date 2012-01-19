package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.util.VisualPropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BypassManager {

	private static final Logger logger = LoggerFactory.getLogger(BypassManager.class);

	// TODO: this should be a public string or enum managed in swing app.
	private static final String MENU_KEY = "preferredMenu";
	private static final String PARENT_MENU_ITEM = "Visual Bypass";

	private final CyServiceRegistrar registrar;
	private final EditorManager editorManager;
	private final SelectedVisualStyleManager selectedManager;

	public BypassManager(final CyServiceRegistrar registrar, final EditorManager editorManager,
			final SelectedVisualStyleManager selectedManager) {
		this.registrar = registrar;
		this.editorManager = editorManager;
		this.selectedManager = selectedManager;
	}

	public void addBypass(RenderingEngineFactory<?> factory, Map props) {
		if (props.containsValue("ding") == false)
			return;

		final VisualLexicon lexicon = factory.getVisualLexicon();

		final VisualProperty<?> nodeRoot = MinimalVisualLexicon.NODE;
		final VisualProperty<?> edgeRoot = MinimalVisualLexicon.EDGE;

		// Tree traversal
		final VisualLexiconNode nodeRootNode = lexicon.getVisualLexiconNode(nodeRoot);
		final VisualLexiconNode edgeRootNode = lexicon.getVisualLexiconNode(edgeRoot);
		
		depthFirst(PARENT_MENU_ITEM, nodeRootNode);
		depthFirst(PARENT_MENU_ITEM, edgeRootNode);
	}

	private void depthFirst(String menuText, final VisualLexiconNode node) {
		final Collection<VisualLexiconNode> children = node.getChildren();
		for (VisualLexiconNode child : children) {
			final VisualProperty<?> vp = child.getVisualProperty();
			
			// Ignore incompatible VP
			if(VisualPropertyFilter.isCompatible(vp) == false)
				continue;
			
			final String newMenu = menuText + "." + vp.getDisplayName();
			if (child.getChildren().size() == 0) {
				// Leaf
				final Properties vpProp = new Properties();
				vpProp.put(MENU_KEY, newMenu);
				vpProp.put("useCheckBoxMenuItem", "true");
				vpProp.put("targetVP", vp.getIdString());

				if (vp.getTargetDataType().equals(CyNode.class)) {
					final NodeViewTaskFactory ntf = new NodeBypassMenuTaskFactory(null, vp,
							editorManager.getValueEditor(vp.getRange().getType()), selectedManager);
					registrar.registerService(ntf, NodeViewTaskFactory.class, vpProp);
				} else if (vp.getTargetDataType().equals(CyEdge.class)) {
					final EdgeViewTaskFactory etf = new EdgeBypassMenuTaskFactory(null, vp,
							editorManager.getValueEditor(vp.getRange().getType()), selectedManager);
					registrar.registerService(etf, EdgeViewTaskFactory.class, vpProp);
				}
				logger.debug("Bypass context menu registered: " + vp.getDisplayName());
			} else {
				depthFirst(newMenu, child);
			}
		}
	}

	public void removeBypass(RenderingEngineFactory<?> factory, Map props) {
		// TODO: implement this
	}

}
