package de.mpg.mpi_inf.bioinf.netanalyzer;

import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_URL;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import de.mpg.mpi_inf.bioinf.netanalyzer.task.AnalyzeNetworkByNetworkAnalyzerTaskFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ResultPanelFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.VisualStyleBuilder;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	
	@Override
	public void start(BundleContext bc) {

		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkReaderManager cyNetworkViewReaderManagerServiceRef = getService(bc,CyNetworkReaderManager.class);
		
		CyNetworkViewManager viewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory vsFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		
		VisualMappingFunctionFactory continupousMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		
		// Create network from selection
		NewNetworkSelectedNodesAndEdgesTaskFactory newNetworkSelectedNodesEdgesTaskFactoryServiceRef = getService(bc,NewNetworkSelectedNodesAndEdgesTaskFactory.class);
		DialogTaskManager taskManager = getService(bc, DialogTaskManager.class);
		
		final ResultPanelFactory resultPanel = new ResultPanelFactory(cyServiceRegistrarServiceRef);
		registerAllServices(bc, resultPanel, new Properties());
		
		// FIXME refactor this code
		Plugin plugin = new Plugin(cySwingApplicationServiceRef);
		
		// Builder object for custom Visual Style
		VisualStyleBuilder vsBuilder = new VisualStyleBuilder(vsFactoryServiceRef, passthroughMappingFactoryRef, continupousMappingFactoryRef);
		
		Map<String,String> analyzerActionProps = new HashMap<String, String>();
		analyzerActionProps.put(ID,"analyzeNetworkAction");
		analyzerActionProps.put(PREFERRED_MENU,"Tools.NetworkAnalyzer.Network Analysis");
		analyzerActionProps.put(TITLE,"Analyze Network");
		analyzerActionProps.put(MENU_GRAVITY,"9.0");
		analyzerActionProps.put(TOOL_BAR_GRAVITY,"9.8");
		analyzerActionProps.put(LARGE_ICON_URL,getClass().getResource("/networkAnalyzer24.png").toString());
		analyzerActionProps.put(SMALL_ICON_URL,getClass().getResource("/networkAnalyzer16.png").toString());
		analyzerActionProps.put(IN_TOOL_BAR,"false");
		analyzerActionProps.put(TOOLTIP,"Analyze Network");
		analyzerActionProps.put(ENABLE_FOR, "network");
		AnalyzeNetworkAction analyzeNetworkAction = new AnalyzeNetworkAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, viewManagerServiceRef, vsBuilder, vmmServiceRef, analyzerActionProps, viewManagerServiceRef, resultPanel);

		AnalyzeNetworkByNetworkAnalyzerTaskFactory analyzeNetworkTaskFactory = new AnalyzeNetworkByNetworkAnalyzerTaskFactory();
		Properties selectAllEdgesTaskFactoryProps = new Properties();
		selectAllEdgesTaskFactoryProps.setProperty(ID, "analyzeNetworkByNetworkAnalyzerTaskFactory");
		registerAllServices(bc,analyzeNetworkTaskFactory, selectAllEdgesTaskFactoryProps);
		
		LoadNetstatsAction loadNetstatsAction = new LoadNetstatsAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, viewManagerServiceRef, vsBuilder, vmmServiceRef, resultPanel);
		MapParameterAction mapParameterAction = new MapParameterAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, viewManagerServiceRef, vsBuilder, vmmServiceRef, analyzeNetworkAction);
		
		AboutAction aboutAction = new AboutAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		AnalyzeSubsetAction analyzeSubsetAction = new AnalyzeSubsetAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, analyzeNetworkAction);
		BatchAnalysisAction batchAnalysisAction = new BatchAnalysisAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef,cyNetworkViewReaderManagerServiceRef, loadNetstatsAction);
		
		// Disabled because similar function is available from Network Merge
		//CompareAction compareAction = new CompareAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef);
		ConnComponentAction connComponentAction = new ConnComponentAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, newNetworkSelectedNodesEdgesTaskFactoryServiceRef, taskManager);
		
		PlotParameterAction plotParameterAction = new PlotParameterAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef, analyzeNetworkAction);
		RemDupEdgesAction remDupEdgesAction = new RemDupEdgesAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef);
		RemoveSelfLoopsAction removeSelfLoopsAction = new RemoveSelfLoopsAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef);
		SettingsAction settingsAction = new SettingsAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		
		
		
		registerService(bc,analyzeNetworkAction,CyAction.class, new Properties());
		registerService(bc,analyzeSubsetAction,CyAction.class, new Properties());
		registerService(bc,batchAnalysisAction,CyAction.class, new Properties());
		registerService(bc,loadNetstatsAction,CyAction.class, new Properties());
		registerService(bc,plotParameterAction,CyAction.class, new Properties());
		registerService(bc,mapParameterAction,CyAction.class, new Properties());
		registerService(bc,settingsAction,CyAction.class, new Properties());
		//registerService(bc,compareAction,CyAction.class, new Properties());
		registerService(bc,connComponentAction,CyAction.class, new Properties());
		registerService(bc,remDupEdgesAction,CyAction.class, new Properties());
		registerService(bc,removeSelfLoopsAction,CyAction.class, new Properties());
		registerService(bc,aboutAction,CyAction.class, new Properties());
	}
}
