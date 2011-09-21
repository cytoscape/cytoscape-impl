



package de.mpg.mpi_inf.bioinf.netanalyzer;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;

import de.mpg.mpi_inf.bioinf.netanalyzer.CompareAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.PlotParameterAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.MapParameterAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.RemoveSelfLoopsAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.ConnComponentAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.BatchAnalysisAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.RemDupEdgesAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.SettingsAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.AnalyzeSubsetAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.Plugin;
import de.mpg.mpi_inf.bioinf.netanalyzer.AboutAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.LoadNetstatsAction;
import de.mpg.mpi_inf.bioinf.netanalyzer.AnalyzeNetworkAction;

import org.cytoscape.application.swing.CyAction;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkReaderManager cyNetworkViewReaderManagerServiceRef = getService(bc,CyNetworkReaderManager.class);
		
		Plugin plugin = new Plugin(cySwingApplicationServiceRef);
		AboutAction aboutAction = new AboutAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		AnalyzeNetworkAction analyzeNetworkAction = new AnalyzeNetworkAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		AnalyzeSubsetAction analyzeSubsetAction = new AnalyzeSubsetAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		BatchAnalysisAction batchAnalysisAction = new BatchAnalysisAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef,cyNetworkViewReaderManagerServiceRef);
		CompareAction compareAction = new CompareAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef,cyNetworkManagerServiceRef);
		ConnComponentAction connComponentAction = new ConnComponentAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		LoadNetstatsAction loadNetstatsAction = new LoadNetstatsAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		MapParameterAction mapParameterAction = new MapParameterAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
		PlotParameterAction plotParameterAction = new PlotParameterAction(cyApplicationManagerServiceRef,cySwingApplicationServiceRef);
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
		registerService(bc,compareAction,CyAction.class, new Properties());
		registerService(bc,connComponentAction,CyAction.class, new Properties());
		registerService(bc,remDupEdgesAction,CyAction.class, new Properties());
		registerService(bc,removeSelfLoopsAction,CyAction.class, new Properties());
		registerService(bc,aboutAction,CyAction.class, new Properties());
	}
}

