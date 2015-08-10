package org.cytoscape.psi_mi.internal;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.psi_mi.internal.plugin.SchemaVersion.LEVEL_1;
import static org.cytoscape.psi_mi.internal.plugin.SchemaVersion.LEVEL_2_5;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.psi_mi.internal.plugin.PsiMiCyFileFilter;
import org.cytoscape.psi_mi.internal.plugin.PsiMiNetworkViewTaskFactory;
import org.cytoscape.psi_mi.internal.plugin.PsiMiNetworkWriterFactory;
import org.cytoscape.psi_mi.internal.plugin.PsiMiTabCyFileFilter;
import org.cytoscape.psi_mi.internal.plugin.PsiMiTabReaderFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
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
				PsiMiCyFileFilter.PSIMIVersion.PSIMI25, psiMi25Filter, cyApplicationManagerServiceRef, cyNetworkFactoryServiceRef,
				cyNetworkViewFactoryServiceRef, cyLayoutsServiceRef, cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);
		PsiMiNetworkViewTaskFactory psiMi10NetworkViewTaskFactory = new PsiMiNetworkViewTaskFactory(
				PsiMiCyFileFilter.PSIMIVersion.PXIMI10, psiMi1Filter, cyApplicationManagerServiceRef, cyNetworkFactoryServiceRef,
				cyNetworkViewFactoryServiceRef, cyLayoutsServiceRef, cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);

		PsiMiNetworkWriterFactory psiMi1NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_1,psiMi1Filter);
		PsiMiNetworkWriterFactory psiMi25NetworkViewWriterFactory = new PsiMiNetworkWriterFactory(LEVEL_2_5,psiMi25Filter);
		PsiMiTabReaderFactory psiMiTabReaderFactory = new PsiMiTabReaderFactory(psiMiTabFilter,cyApplicationManagerServiceRef,
				cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyLayoutsServiceRef,
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

