package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;

public class CurrentTableService {
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMediator vizMapperMediator;
	private final AttributeSetProxy attrProxy;
	

	public CurrentTableService(
			ServicesUtil servicesUtil, 
			VizMapperMediator vizMapperMediator, 
			AttributeSetProxy attrProxy
	) {
		this.servicesUtil = servicesUtil;
		this.vizMapperMediator = vizMapperMediator;
		this.attrProxy = attrProxy;
	}

	public VisualLexicon getCurrentVisualLexicon() {
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
//		if(vizMapperTableMediator.isTableDialogOpen()) {
//			return appMgr.getDefaultTableViewRenderer()
//				.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT)
//				.getVisualLexicon();
//		} else {
			return appMgr.getCurrentNetworkViewRenderer()
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
//		}
	}
	
	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {
//		if(vizMapperTableMediator.isTableDialogOpen())
//			return vizMapperTableMediator.getCurrentVisualPropertySheetItem();
//		else
			return vizMapperMediator.getCurrentVisualPropertySheetItem();
	}
	
	public VizMapperProperty<?, ?, ?> getCurrentVizMapperProperty() {
//		if(vizMapperTableMediator.isTableDialogOpen())
//			return vizMapperTableMediator.getCurrentVizMapperProperty();
//		else
			return vizMapperMediator.getCurrentVizMapperProperty();
	}
	
	public AttributeSet getAttributeSet(VisualProperty<?> vp) {
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
//		if(vizMapperTableMediator.isTableDialogOpen()) {
//			CyTable table = appMgr.getCurrentTable();
//			return table == null ? null : attrProxy.getAttributeSet(table);
//		} else {
			CyNetwork currentNet = appMgr.getCurrentNetwork();
			return currentNet == null ? null : attrProxy.getAttributeSet(currentNet, vp.getTargetDataType());
//		}
	}
	
	public CyTable getCurrentTable(Class<? extends CyIdentifiable> targetDataType) {
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
//		if(vizMapperTableMediator.isTableDialogOpen()) {
			return appMgr.getCurrentTable();
//		} else {
//			CyNetworkTableManager netTblMgr = servicesUtil.get(CyNetworkTableManager.class);
//			return netTblMgr.getTable(appMgr.getCurrentNetwork(), targetDataType, CyNetwork.DEFAULT_ATTRS);
//		}
	}
	
}
