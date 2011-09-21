
package org.cytoscape.psi_mi.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;

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
		
		PsiMiCyFileFilter psiMiFilter = new PsiMiCyFileFilter("PSI-MI files (*.xml)",streamUtilRef);
		PsiMiCyFileFilter psiMi1Filter = new PsiMiCyFileFilter("PSI-MI Level 1",streamUtilRef);
		PsiMiCyFileFilter psiMi25Filter = new PsiMiCyFileFilter("PSI-MI Level 2.5",streamUtilRef);
		PsiMiTabCyFileFilter psiMiTabFilter = new PsiMiTabCyFileFilter();
		PsiMiNetworkViewTaskFactory psiMiNetworkViewTaskFactory = new PsiMiNetworkViewTaskFactory(psiMiFilter,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyLayoutsServiceRef);
		PsiMiNetworkWriterFactory psiMi1NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_1,psiMi1Filter);
		PsiMiNetworkWriterFactory psiMi25NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_2_5,psiMi25Filter);
		PsiMiTabReaderFactory psiMiTabReaderFactory = new PsiMiTabReaderFactory(psiMiTabFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyLayoutsServiceRef);
		
		
		Properties psiMiNetworkViewTaskFactoryProps = new Properties();
		psiMiNetworkViewTaskFactoryProps.setProperty("serviceType","psiMiNetworkViewTaskFactory");
		psiMiNetworkViewTaskFactoryProps.setProperty("readerDescription","PSI-MI Level 1/2.5 file reader");
		psiMiNetworkViewTaskFactoryProps.setProperty("readerId","psiMiNetworkViewReader");
		registerService(bc,psiMiNetworkViewTaskFactory,InputStreamTaskFactory.class, psiMiNetworkViewTaskFactoryProps);

		Properties psiMiTabReaderFactoryProps = new Properties();
		psiMiTabReaderFactoryProps.setProperty("serviceType","psiMiTabReaderFactory");
		psiMiTabReaderFactoryProps.setProperty("readerDescription","PSI-MI tab file reader");
		psiMiTabReaderFactoryProps.setProperty("readerId","psiMiTabReader");
		registerService(bc,psiMiTabReaderFactory,InputStreamTaskFactory.class, psiMiTabReaderFactoryProps);
		registerService(bc,psiMi1NetworkViewWriterFactory,CyNetworkViewWriterFactory.class, new Properties());
		registerService(bc,psiMi25NetworkViewWriterFactory,CyNetworkViewWriterFactory.class, new Properties());

	}
}

