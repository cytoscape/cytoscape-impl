package org.cytoscape.view.model.internal.debug;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;

@SuppressWarnings("serial")
public class PrintViewModelAction extends AbstractCyAction {

	private final CyServiceRegistrar registrar;
	
	public PrintViewModelAction(CyServiceRegistrar registrar) {
		super("Print View Model");
		this.registrar = registrar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CyApplicationManager applicationManager = registrar.getService(CyApplicationManager.class);
		
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		VisualLexicon lexicon = applicationManager.getCurrentRenderingEngine().getVisualLexicon();
		if(networkView == null || lexicon == null) {
			System.out.println("no current network");
			return;
		}
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		if(snapshot == null) {
			System.out.println("snapshot is null");
			return;
		}

		Map<?,List<VisualProperty<?>>> vpByType = 
				lexicon.getAllVisualProperties()
				.stream()
				.filter(vp -> !vp.getDisplayName().contains("Image/Chart"))
				.collect(Collectors.groupingBy(VisualProperty::getTargetDataType));
		
		// Print Defaults
		// Print Network VPs
//		System.out.println("Defaults");
//		printDefaults(snapshot, vpByType.get(CyNetwork.class));
//		printDefaults(snapshot, vpByType.get(CyNode.class));
//		printDefaults(snapshot, vpByType.get(CyEdge.class));
//		System.out.println("*****************");
		
		System.out.println("Values");
		for(View<CyNode> view : snapshot.getNodeViews()) {
			System.out.println("\n**Node** " + view.getSUID());
			printVPs(snapshot, view, vpByType.get(CyNode.class));
		}
		for(View<CyEdge> view : snapshot.getEdgeViews()) {
			System.out.println("\n**Edge** " + view.getSUID());
			printVPs(snapshot, view, vpByType.get(CyEdge.class));
		}
		System.out.println();
	}
	
	
	private static void printVPs(CyNetworkViewSnapshot snapshot, View<? extends CyIdentifiable> elem, List<VisualProperty<?>> vps) {
		for(VisualProperty<?> vp : vps) {
			if(elem.isSet(vp)) {
				Object value = elem.getVisualProperty(vp);
				boolean locked = elem.isValueLocked(vp);
				System.out.println(vp.getDisplayName() + " = " + value + (locked ? " (locked)" : " (value)"));
			} else {
				Object defaultValue = snapshot.getViewDefault(vp);
				if(defaultValue != null) {
					System.out.println(vp.getDisplayName() + " = " + defaultValue + " (default)");
				}
			}
		}
	}
	
	private static void printDefaults(CyNetworkViewSnapshot snapshot, List<VisualProperty<?>> vps) {
		for(VisualProperty<?> vp : vps) {
			Object defaultValue = snapshot.getViewDefault(vp);
			if(defaultValue != null) {
				System.out.println(vp.getDisplayName() + " = " + defaultValue);
			}
		}
	}

}
