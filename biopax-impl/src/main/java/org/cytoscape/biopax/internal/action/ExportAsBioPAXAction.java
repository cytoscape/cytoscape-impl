/* Copyright 2008 - The Cytoscape Consortium (www.cytoscape.org)
 *
 * The Cytoscape Consortium is:
 * - Institute for Systems Biology
 * - University of California San Diego
 * - Memorial Sloan-Kettering Cancer Center
 * - Institut Pasteur
 * - Agilent Technologies
 *
 * Authors: B. Arman Aksoy, Thomas Kelder, Emek Demir
 * 
 * This file is part of PaxtoolsPlugin.
 *
 *  PaxtoolsPlugin is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  PaxtoolsPlugin is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.biopax.internal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.biopax.internal.util.BioPaxUtil;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is currently an experimental feature.
 * Networks that were previously imported
 * from a BioPAX file or web services may be saved.
 * All the modifications to this network made
 * within Cytoscape will be lost.
 * 
 * @author rodche
 *
 */
public class ExportAsBioPAXAction extends AbstractCyAction {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final FileUtil fileUtil;
	private final CyApplicationManager applicationManager;
	private final CyFileFilter bioPaxFilter;
	private final TaskManager taskManager;
	
	public ExportAsBioPAXAction(FileUtil fileUtil, CyApplicationManager applicationManager, 
			CyFileFilter bioPaxFilter, TaskManager taskManager) 
	{
		super("BioPAX", applicationManager,"network");
		setPreferredMenu("File.Export.Network");
		this.fileUtil = fileUtil;
		this.applicationManager = applicationManager;
		this.bioPaxFilter = bioPaxFilter;
		this.taskManager = taskManager;
	}

    /**
	 * User-initiated action to save the current BioPAX network 
	 * to a user-specified file.  If successfully saved, fires a
	 * PropertyChange event with property=Cytoscape.NETWORK_SAVED,
	 * old_value=null, and new_value=a three element Object array containing:
	 * <OL>
	 * <LI>first element = CyNetwork saved
	 * <LI>second element = URI of the location where saved
	 * <LI>third element = an Integer representing the format in which the
	 * Network was saved (e.g., Cytoscape.FILE_SIF).
	 * </OL>
	 * @param e ActionEvent Object.
	 */
    public void actionPerformed(ActionEvent event) {
		Collection<FileChooserFilter> filters = new ArrayList<FileChooserFilter>();
		filters.add(new FileChooserFilter("BioPAX format", "rdf"));
		File file = fileUtil.getFile((Component) event.getSource(), 
			"Save BioPAX Network (experimental feature)", FileUtil.SAVE, filters);
		if (file != null) {
			String fileName = file.getAbsolutePath();

			if (!fileName.endsWith(".xml"))
				fileName = fileName + ".xml";

			ExportAsBioPAXTaskFactory taskFactory = new ExportAsBioPAXTaskFactory(fileName, bioPaxFilter);
			try {
				FileOutputStream stream = new FileOutputStream(fileName);
				taskFactory.setOutputStream(stream);
				try {
					taskManager.execute(new TaskIterator(taskFactory.getWriterTask()));
				} finally {
					stream.close();
				}
			} catch (IOException e) {
				logger.error("Unexpected error", e);
			}
		}
    }

    public void menuSelected(MenuEvent e) {
        CyNetwork cyNetwork = applicationManager.getCurrentNetwork();

        if( BioPaxUtil.isBioPAXNetwork(cyNetwork) )
            updateEnableState(); 
        else
            setEnabled(false);
    }
}
