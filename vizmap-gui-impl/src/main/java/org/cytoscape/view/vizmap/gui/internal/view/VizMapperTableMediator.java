package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_UPDATED;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.puremvc.java.multicore.interfaces.INotification;

public class VizMapperTableMediator extends AbstractVizMapperMediator {

	public static final String NAME = "VizMapperTableMediator";
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends CyIdentifiable>[] SHEET_TYPES = new Class[] { CyColumn.class };
	
	private final VizMapperTableDialog vizMapperTableDialog;
	private RenderingEngine<CyTable> currentRenderingEngine;
	private VisualStyle currentStyle;
	
	public VizMapperTableMediator(VizMapperTableDialog vizMapperTableDialog, ServicesUtil servicesUtil, VizMapPropertyBuilder propBuilder) {
		super(NAME, vizMapperTableDialog, servicesUtil, propBuilder, SHEET_TYPES);
		this.vizMapperTableDialog = vizMapperTableDialog;
	}
	
	public boolean isTableDialogOpen() {
		return vizMapperTableDialog.isVisible();
	}
	
	@Override
	public final void onRegister() {
		super.onRegister();
		servicesUtil.registerAllServices(vizMapperTableDialog, new Properties());
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ VISUAL_STYLE_UPDATED };
	}
	
	@Override
	public void handleNotification(final INotification notification) {
		final String id = notification.getName();
		final Object body = notification.getBody();
		
		if(VISUAL_STYLE_UPDATED.equals(id)) {
			if(body != null && body.equals(currentStyle)) {
				updateVisualPropertySheets((VisualStyle) body, false, false);
			}
		}
	}
	
	public void showDialogFor(CyColumn column) {
		this.currentStyle = vmProxy.getVisualStyle(column);
		this.currentRenderingEngine = vmProxy.getRenderingEngine(column);
		
		updateVisualPropertySheets();
		
		var swingApplication = servicesUtil.get(CySwingApplication.class);
		SwingUtilities.invokeLater(() -> {
			vizMapperTableDialog.setTargetColumn(column);
			vizMapperTableDialog.setModal(true);
			vizMapperTableDialog.setLocationRelativeTo(swingApplication.getJFrame());
			vizMapperTableDialog.setVisible(true);
		});
	}
	
	private void updateVisualPropertySheets() {
		updateVisualPropertySheets(getVisualStyle(), false, true);
	}
	
	@Override
	protected void updateMappingStatus(VisualPropertySheetItem<?> item) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Collection<VisualProperty<?>> getVisualPropertyList(VisualLexicon lexicon) {
		return lexicon.getAllDescendants(BasicTableVisualLexicon.CELL);
	}

	@Override
	protected Set<View<? extends CyIdentifiable>> getSelectedViews(Class<?> type) {
		return Collections.emptySet();
	}

	
	@Override
	protected VisualLexicon getVisualLexicon() {
		return currentRenderingEngine.getVisualLexicon();
	}
	
	@Override
	protected VisualStyle getVisualStyle() {
		return currentStyle;
	}
	
	@Override
	protected RenderingEngine<?> getRenderingEngine() {
		return currentRenderingEngine;
	}
	
	@Override
	protected boolean isSupported(VisualProperty<?>	vp) {
		return VizMapperProxy.isSupported(getVisualLexicon(), vp);
	}
	
	@Override
	protected boolean isSupported(VisualPropertyDependency<?> dep) {
		return VizMapperProxy.isSupported(getVisualLexicon(), dep);
	}
}
