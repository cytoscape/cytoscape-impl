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
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSet;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;

/**
 * This class is kind of a mess. It gets data from all over the place.
 * Should probably put these methods in vmProxy and just delegate to that?
 */
public class CurrentTableService {
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMediator vizMapperMediator;
	private final AttributeSetProxy attrProxy;
	private final VizMapperProxy vmProxy;
	

	public CurrentTableService(
			ServicesUtil servicesUtil, 
			VizMapperMediator vizMapperMediator, 
			AttributeSetProxy attrProxy,
			VizMapperProxy vmProxy
	) {
		this.servicesUtil = servicesUtil;
		this.vizMapperMediator = vizMapperMediator;
		this.attrProxy = attrProxy;
		this.vmProxy = vmProxy;
	}

	public RenderingEngine<?> getRenderingEngine(VisualProperty<?> vp) {
		Class<?> type = vp.getTargetDataType();
		if(type == CyColumn.class || type == CyTable.class)
			return vmProxy.getCurrentTableRenderingEngine();
		else
			return vmProxy.getCurrentRenderingEngine();
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
	
	public VisualStyle getCurrentVisualStyle(Class<? extends CyIdentifiable> type) {
		return vizMapperMediator.getCurrentVisualStyle(type);
	}
	
	public VisualStyle getCurrentVisualStyle() {
		var type = getCurrentVisualPropertySheetItem().getModel().getTargetDataType();
		return getCurrentVisualStyle(type);
	}
	
	public CyColumn getCurrentColumn() {
		return vizMapperMediator.getCurrentColumn();
	}
}
