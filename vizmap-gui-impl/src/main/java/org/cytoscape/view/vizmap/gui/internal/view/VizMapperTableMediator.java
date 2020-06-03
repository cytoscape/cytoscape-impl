package org.cytoscape.view.vizmap.gui.internal.view;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class VizMapperTableMediator extends AbstractVizMapperMediator {

	public static final String NAME = "VizMapperTableMediator";
	
	@SuppressWarnings("unchecked")
	private static final Class<? extends CyIdentifiable>[] SHEET_TYPES = new Class[] { CyColumn.class };
	
	private final VizMapperTableDialog vizMapperTableDialog;
	private VisualLexicon currentLexicon;
	private VisualStyle currentStyle;
	
	public VizMapperTableMediator(VizMapperTableDialog vizMapperTableDialog, ServicesUtil servicesUtil, VizMapPropertyBuilder propBuilder) {
		super(NAME, vizMapperTableDialog, servicesUtil, propBuilder, SHEET_TYPES);
		this.vizMapperTableDialog = vizMapperTableDialog;
	}
	
	@Override
	public final void onRegister() {
		super.onRegister();
		servicesUtil.registerAllServices(vizMapperTableDialog, new Properties());
	}
	
	public void showDialogFor(CyColumn column) {
		this.currentStyle = vmProxy.getVisualStyle(column);
		this.currentLexicon = vmProxy.getRenderingEngine(column).getVisualLexicon();
		
		updateVisualPropertySheets();
		
		var swingApplication = servicesUtil.get(CySwingApplication.class);
		SwingUtilities.invokeLater(() -> {
			vizMapperTableDialog.setLocationRelativeTo(swingApplication.getJFrame());
			vizMapperTableDialog.setVisible(true);
		});
	}
	
	private void updateVisualPropertySheets() {
		updateVisualPropertySheets(getVisualStyle(), false, true);
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ };
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
		return currentLexicon;
	}
	
	@Override
	protected VisualStyle getVisualStyle() {
		return currentStyle;
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
