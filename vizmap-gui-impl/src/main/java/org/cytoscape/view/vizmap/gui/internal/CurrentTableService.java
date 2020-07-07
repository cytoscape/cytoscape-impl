package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
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

	public VisualLexicon getCurrentVisualLexicon(VisualProperty<?> vp) {
		Class<?> type = vp.getTargetDataType();
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		if(type == CyColumn.class || type == CyTable.class) {
			return appMgr.getDefaultTableViewRenderer()
				.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		} else {
			return appMgr.getCurrentNetworkViewRenderer()
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		}
	}
	
	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {
		return vizMapperMediator.getCurrentVisualPropertySheetItem();
	}
	
	public VizMapperProperty<?, ?, ?> getCurrentVizMapperProperty() {
		return vizMapperMediator.getCurrentVizMapperProperty();
	}
	
	public AttributeSet getAttributeSet(VisualProperty<?> vp) {
		Class<?> type = vp.getTargetDataType();
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		if(type == CyColumn.class || type == CyTable.class) {
			CyTable table = appMgr.getCurrentTable();
			return table == null ? null : attrProxy.getAttributeSet(table);
		} else {
			CyNetwork currentNet = appMgr.getCurrentNetwork();
			return currentNet == null ? null : attrProxy.getAttributeSet(currentNet, vp.getTargetDataType());
		}
	}
	
	public CyTable getCurrentTable(Class<? extends CyIdentifiable> targetDataType) {
		CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
		if(targetDataType == CyColumn.class || targetDataType == CyTable.class) {
			return appMgr.getCurrentTable();
		} else {
			CyNetworkTableManager netTblMgr = servicesUtil.get(CyNetworkTableManager.class);
			return netTblMgr.getTable(appMgr.getCurrentNetwork(), targetDataType, CyNetwork.DEFAULT_ATTRS);
		}
	}
	
}