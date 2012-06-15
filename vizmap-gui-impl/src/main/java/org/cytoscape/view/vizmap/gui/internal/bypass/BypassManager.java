package org.cytoscape.view.vizmap.gui.internal.bypass;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;
import org.cytoscape.work.ServiceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates Visual Style Bypass menu.
 * 
 */
public final class BypassManager {

	private static final Logger logger = LoggerFactory.getLogger(BypassManager.class);

	private static final String PARENT_MENU_ITEM = "Visual Bypass";

	private final CyServiceRegistrar registrar;
	private final EditorManager editorManager;
	private final VisualMappingManager vmm;
	private final CyApplicationManager appManager;

	public BypassManager(final CyServiceRegistrar registrar, final EditorManager editorManager,
			final VisualMappingManager vmm, final CyApplicationManager appManager) {
		this.registrar = registrar;
		this.editorManager = editorManager;
		this.vmm = vmm;
		this.appManager = appManager;
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

		
		registerResetTask(CyNode.class);
		registerResetTask(CyEdge.class);
		depthFirst(PARENT_MENU_ITEM, nodeRootNode);
		depthFirst(PARENT_MENU_ITEM, edgeRootNode);
		
		
	}

	public void removeBypass(RenderingEngineFactory<?> factory, Map props) {
		// TODO: implement this
	}
	
	private void registerResetTask(Class<? extends CyIdentifiable> type) {
		final Properties vpProp = new Properties();
		
		if (type.equals(CyNode.class)) {
			vpProp.put(ServiceProperties.PREFERRED_MENU, PARENT_MENU_ITEM + ".Reset All Node Bypass");
			final NodeViewTaskFactory ntf = new ResetNodeBypassTaskFactory(appManager);
			registrar.registerService(ntf, NodeViewTaskFactory.class, vpProp);
		} else if (type.equals(CyEdge.class)) {
			vpProp.put(ServiceProperties.PREFERRED_MENU, PARENT_MENU_ITEM + ".Reset All Edge Bypass");
			final EdgeViewTaskFactory etf = new ResetEdgeBypassTaskFactory(appManager);
			registrar.registerService(etf, EdgeViewTaskFactory.class, vpProp);
		}
	}

	private void depthFirst(String menuText, final VisualLexiconNode node) {
		
		double menu_gravity = 1.0;		
		HashMap<String, String> vpMap = new HashMap<String, String>();
		Vector<String> vp_names = new Vector<String>();
		
		final Collection<VisualLexiconNode> children = node.getChildren();
		
		//get the list of VP
		for (VisualLexiconNode child : children) {
			final VisualProperty<?> vp = child.getVisualProperty();

			// Ignore incompatible VP
			if (PropertySheetUtil.isCompatible(vp) == false)
				continue;

			if (child.getChildren().size() == 0) {
				// Leaf
				vp_names.add(vp.getDisplayName());
			}
		}	
		
		// do sorting
		Object[] names = vp_names.toArray();
		java.util.Arrays.sort(names);
		
		//Assign the menu_gravity for each VP
		for (int i =0; i< names.length; i++){
			menu_gravity += 1.0;
			vpMap.put(names[i].toString(), Double.toString(menu_gravity));
		}
		
		//
		for (VisualLexiconNode child : children) {
			final VisualProperty<?> vp = child.getVisualProperty();

			// Ignore incompatible VP
			if (PropertySheetUtil.isCompatible(vp) == false)
				continue;

			final String newMenu = menuText + "." + vp.getDisplayName();
			if (child.getChildren().size() == 0) {
				// Leaf
				final Properties vpProp = new Properties();
				vpProp.put(ServiceProperties.PREFERRED_MENU, newMenu);
				vpProp.put("useCheckBoxMenuItem", "true");
				vpProp.put("targetVP", vp.getIdString());
				vpProp.put(MENU_GRAVITY, vpMap.get(vp.getDisplayName()));
				
				if (vp.getTargetDataType().equals(CyNode.class)) {
					final NodeViewTaskFactory ntf = new NodeBypassMenuTaskFactory(null, vp,
							editorManager.getValueEditor(vp.getRange().getType()), vmm);
					registrar.registerService(ntf, NodeViewTaskFactory.class, vpProp);
				} else if (vp.getTargetDataType().equals(CyEdge.class)) {
					final EdgeViewTaskFactory etf = new EdgeBypassMenuTaskFactory(null, vp,
							editorManager.getValueEditor(vp.getRange().getType()), vmm);
					registrar.registerService(etf, EdgeViewTaskFactory.class, vpProp);
				}
				
				logger.debug("Bypass context menu registered: " + vp.getDisplayName());
			} else {
				depthFirst(newMenu, child);
			}
		}
	}

}
