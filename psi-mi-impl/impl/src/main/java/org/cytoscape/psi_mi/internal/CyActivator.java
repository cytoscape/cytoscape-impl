
package org.cytoscape.psi_mi.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;

import org.cytoscape.property.CyProperty;
import org.cytoscape.psi_mi.internal.plugin.PsiMiNetworkViewTaskFactory;
import org.cytoscape.psi_mi.internal.plugin.PsiMiTabReaderFactory;
import org.cytoscape.psi_mi.internal.plugin.PsiMiCyFileFilter;
import static org.cytoscape.psi_mi.internal.plugin.SchemaVersion.*;
import org.cytoscape.psi_mi.internal.plugin.PsiMiTabCyFileFilter;
import org.cytoscape.psi_mi.internal.plugin.PsiMiNetworkWriterFactory;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,CyRootNetworkManager.class);	
		
		// PsiMiCyFileFilter psiMiFilter = new
		// PsiMiCyFileFilter("PSI-MI files (*.xml)",streamUtilRef,
		// PsiMiCyFileFilter);
		PsiMiCyFileFilter psiMi1Filter = new PsiMiCyFileFilter("PSI-MI Level 1", streamUtilRef,
				PsiMiCyFileFilter.PSIMIVersion.PXIMI10);
		PsiMiCyFileFilter psiMi25Filter = new PsiMiCyFileFilter("PSI-MI Level 2.5", streamUtilRef,
				PsiMiCyFileFilter.PSIMIVersion.PSIMI25);
		PsiMiTabCyFileFilter psiMiTabFilter = new PsiMiTabCyFileFilter();
		
		PsiMiNetworkViewTaskFactory psiMi25NetworkViewTaskFactory = new PsiMiNetworkViewTaskFactory(
				PsiMiCyFileFilter.PSIMIVersion.PSIMI25, psiMi25Filter, cyNetworkFactoryServiceRef,
				cyNetworkViewFactoryServiceRef, cyLayoutsServiceRef, cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);
		PsiMiNetworkViewTaskFactory psiMi10NetworkViewTaskFactory = new PsiMiNetworkViewTaskFactory(
				PsiMiCyFileFilter.PSIMIVersion.PXIMI10, psiMi1Filter, cyNetworkFactoryServiceRef,
				cyNetworkViewFactoryServiceRef, cyLayoutsServiceRef, cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);

		PsiMiNetworkWriterFactory psiMi1NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_1,psiMi1Filter);
		PsiMiNetworkWriterFactory psiMi25NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_2_5,psiMi25Filter);
		PsiMiTabReaderFactory psiMiTabReaderFactory = new PsiMiTabReaderFactory(psiMiTabFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyLayoutsServiceRef, cyPropertyServiceRef,
				cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef);
		
		// For level 1
		Properties psiMi1NetworkViewTaskFactoryProps = new Properties();
		psiMi1NetworkViewTaskFactoryProps.setProperty("readerDescription","PSI-MI Level 1 file reader");
		psiMi1NetworkViewTaskFactoryProps.setProperty("readerId","psiMi1NetworkViewReader");
		registerService(bc,psiMi10NetworkViewTaskFactory,InputStreamTaskFactory.class, psiMi1NetworkViewTaskFactoryProps);
		
		// For level 2
		Properties psiMi25NetworkViewTaskFactoryProps = new Properties();
		psiMi25NetworkViewTaskFactoryProps.setProperty("readerDescription","PSI-MI Level 2.5 file reader");
		psiMi25NetworkViewTaskFactoryProps.setProperty("readerId","psiMi25NetworkViewReader");
		registerService(bc,psiMi25NetworkViewTaskFactory,InputStreamTaskFactory.class, psiMi25NetworkViewTaskFactoryProps);

		Properties psiMiTabReaderFactoryProps = new Properties();
		psiMiTabReaderFactoryProps.setProperty("readerDescription","PSI-MI tab file reader");
		psiMiTabReaderFactoryProps.setProperty("readerId","psiMiTabReader");
		registerService(bc,psiMiTabReaderFactory,InputStreamTaskFactory.class, psiMiTabReaderFactoryProps);
		registerService(bc,psiMi1NetworkViewWriterFactory,CyNetworkViewWriterFactory.class, new Properties());
		registerService(bc,psiMi25NetworkViewWriterFactory,CyNetworkViewWriterFactory.class, new Properties());
	}
}

