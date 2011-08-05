/*
 File: VizMapListener.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

//----------------------------------------------------------------------------
// $Revision: 12924 $
// $Date: 2008-02-04 11:10:30 -0800 (Mon, 04 Feb 2008) $
// $Author: mes $
//----------------------------------------------------------------------------
package org.cytoscape.view.vizmap.gui.internal;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;


public class VizMapListener implements PropertyChangeListener {
	public static final String VIZMAP_PROPS_FILE_NAME = "vizmap.props";

	private VisualMappingManager vmm;
	private final CyApplicationManager applicationManager;
	
	public VizMapListener(VisualMappingManager vmm, CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
		this.vmm = vmm;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		//FIXME  Need to be replaced by the new event framework.
//		if (e.getPropertyName() == VisualMappingManager.SAVE_VIZMAP_PROPS) {
//			// This section is for saving VS in a vizmap.props file.
//			// If signal contains no new value, Cytoscape consider it as a
//			// default file. Otherwise, save it as a user file.
//			File propertiesFile = null;
//
//			if (e.getNewValue() == null)
//				propertiesFile = CytoscapeInit.getConfigFile(VIZMAP_PROPS_FILE_NAME);
//			else
//				propertiesFile = new File((String) e.getNewValue());
//
//			if (propertiesFile != null) {
//				Set test = CalculatorCatalogFactory.getCalculatorCatalog().getVisualStyleNames();
//				Iterator it = test.iterator();
//				System.out.println("Saving the following Visual Styles: ");
//
//				while (it.hasNext())
//					System.out.println("    - " + it.next().toString());
//
//				CalculatorIO.storeCatalog(CalculatorCatalogFactory.getCalculatorCatalog(),
//				                          propertiesFile);
//				System.out.println("Vizmap saved to: " + propertiesFile);
//			}
//		} else if ((e.getPropertyName() == VisualMappingManager.VIZMAP_RESTORED)
//		           || (e.getPropertyName() == VisualMappingManager.VIZMAP_LOADED)) {
//			// This section is for restoring VS from a file.
//
//			// only clear the existing vizmap.props if we're restoring
//			// from a session file
//			if (e.getPropertyName() == VisualMappingManager.VIZMAP_RESTORED)
//				CalculatorCatalogFactory.getCalculatorCatalog().clearProps();
//
//			// get the new vizmap.props and apply it the existing properties
//			Object vizmapSource = e.getNewValue();
//			System.out.println("vizmapSource: '" + vizmapSource.toString() + "'");
//
//			Properties props = new Properties();
//
//			try {
//				InputStream is = null;
//
//				if (vizmapSource.getClass() == URL.class)
//					// is = ((URL) vizmapSource).openStream();
//					// Use URLUtil to get the InputStream since we might be using a proxy server 
//					// and because pages may be cached:
//					is = URLUtil.getBasicInputStream((URL) vizmapSource);
//				else if (vizmapSource.getClass() == String.class) {
//					// if its a RESTORED event the vizmap
//					// file will be in a zip file.
//					if (e.getPropertyName() == VisualMappingManager.VIZMAP_RESTORED) {
//						is = ZipUtil.readFile((String) vizmapSource, ".*vizmap.props");
//
//						// if its a LOADED event the vizmap file
//						// will be a normal file.
//					} else
//						is = FileUtil.getInputStream((String) vizmapSource);
//				}
//
//				if (is != null) {
//					props.load(is);
//					is.close();
//				}
//			} catch (FileNotFoundException e1) {
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//
//			CalculatorCatalogFactory.getCalculatorCatalog().appendProps(props);
//
//			System.out.println("Applying visual styles from: " + vizmapSource.toString());
//
//			// In the situation where the old visual style has been overwritten
//			// with a new visual style of the same name, then make sure it is
//			// reapplied.
//			vmm.setVisualStyle(vmm.getVisualStyle().getName());
//			vmm.setVisualStyleForView(applicationManager.getCurrentNetworkView(), vmm.getVisualStyle());
//			//Cytoscape.redrawGraph(applicationManager.getCurrentNetworkView());
//		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param vmm DOCUMENT ME!
	 */
	public void setVmm(VisualMappingManager vmm) {
		this.vmm = vmm;
	}

}
