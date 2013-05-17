 package org.cytoscape.view.vizmap.gui.internal.view;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualStyle;

@Deprecated
public class VizMapperModel extends AbstractVizMapperModel {

	private final SortedSet<VisualStyle> visualStyles;
	private VisualStyle currentVisualStyle;
	private final Map<Class<? extends CyIdentifiable>, VisualPropertySheetModel> vpSheetModelMap;
	
	private final CyApplicationManager appMgr;
	private final RenderingEngineManager rendEngMgr;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperModel(final CyApplicationManager appMgr,
						  final RenderingEngineManager rendEngMgr) {
		assert appMgr != null;
		assert rendEngMgr != null;
		
		this.appMgr = appMgr;
		this.rendEngMgr = rendEngMgr;
		
		visualStyles = new TreeSet<VisualStyle>(
				new Comparator<VisualStyle>() {
					@Override
					public int compare(final VisualStyle vs1, final VisualStyle vs2) {
						// Locale-specific sorting
						final Collator collator = Collator.getInstance(Locale.getDefault());
						collator.setStrength(Collator.PRIMARY);
						
						return collator.compare(vs1.getTitle(), vs2.getTitle());
					}
				}
		);
		vpSheetModelMap = new HashMap<Class<? extends CyIdentifiable>, VisualPropertySheetModel>();
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public SortedSet<VisualStyle> getVisualStyles() {
		return Collections.unmodifiableSortedSet(visualStyles);
	}
	
	public VisualStyle getCurrentVisualStyle() {
		return currentVisualStyle;
	}
	
	public void setCurrentVisualStyle(final VisualStyle style) {
		if (style != currentVisualStyle) {
			currentVisualStyle = style;
		}
	}

	public Set<VisualPropertySheetModel> getVisualPropertySheetModels() {
		final Set<VisualPropertySheetModel> set = new LinkedHashSet<VisualPropertySheetModel>();
		
		// Order by: Node, Edge, Network
		if (vpSheetModelMap.get(CyNode.class) != null)
			set.add(vpSheetModelMap.get(CyNode.class));
		if (vpSheetModelMap.get(CyEdge.class) != null)
			set.add(vpSheetModelMap.get(CyEdge.class));
		if (vpSheetModelMap.get(CyNetwork.class) != null)
			set.add(vpSheetModelMap.get(CyNetwork.class));
		
		return set;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	

//	private RenderingEngine<CyNetwork> createDefaultRenderingEngine() {
//		// TODO: get from preview network or API, if possible
//		final CyNetwork net = netFactory.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
//		final CyNetworkView view = netViewFactory.createNetworkView(net);
//		
//		return rendEngFactory.createRenderingEngine(new JPanel(), view);
//	}
}
