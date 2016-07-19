package org.cytoscape.browser.internal.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class CyDropListener implements DropTargetListener {

	private final JComponent parentPanel; // the zone that accepts the drop
	private Border originalBorder;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyDropListener.class);

	public CyDropListener(final JComponent parentPanel, final CyServiceRegistrar serviceRegistrar) {
		this.parentPanel = parentPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		parentPanel.setTransferHandler(new TransferHandler() {
	        @Override
	        public boolean canImport(TransferHandler.TransferSupport info) {
	        	return isAcceptable(info);
	        }
	        @Override
	        public boolean importData(TransferHandler.TransferSupport info) {
	            return info.isDrop() && !isAcceptable(info);
	        }
	    });
		
		new DropTarget(parentPanel,this);
	}

	@Override
	public void dragEnter(DropTargetDragEvent evt) {
		originalBorder = parentPanel.getBorder();
		parentPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2));
	}

	@Override
	public void dragExit(DropTargetEvent evt) {
		parentPanel.setBorder(originalBorder);
	}

	@Override
	public void dragOver(DropTargetDragEvent evt) {
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent evt) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent evt) {
		parentPanel.setBorder(originalBorder);
        
		if (!isAcceptable(evt)) {
			evt.rejectDrop();
        	return;
		}
		
		evt.acceptDrop(evt.getDropAction());
		final Transferable t = evt.getTransferable();
		
		if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {       
            // Get the fileList that is being dropped.
	        List<File> data;
	        
	        try {
	            data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
	        } catch (Exception e) { 
	        	logger.error("Cannot load table files by Drag-and-Drop.", e);
	        	return; 
	        }
	        
	        new Thread(() -> {
	        	loadFiles(data);
	        }).start();
        }
	}
	
	private void loadFiles(final List<File> data) {
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		final LoadTableFileTaskFactory factory = serviceRegistrar.getService(LoadTableFileTaskFactory.class);

		if (factory != null)
			loadFiles(data.iterator(), taskManager, factory);
	}
	
	private void loadFiles(final Iterator<File> iterator, final DialogTaskManager taskManager,
			final LoadTableFileTaskFactory factory) {
		while (iterator.hasNext()) {
			final File file = iterator.next();
			
			if (!file.isDirectory()) {
				try {
					taskManager.execute(factory.createTaskIterator(file), new TaskObserver() {
						@Override
						public void taskFinished(ObservableTask task) {
						}
						@Override
						public void allFinished(FinishStatus finishStatus) {
							// Load the other files recursively
							loadFiles(iterator, taskManager, factory);
						}
					});
				} catch (Exception e) {
					logger.error("Cannot load table file by Drag-and-Drop.", e);
				}
				
				return;
			}
		}
	}

	private static boolean isAcceptable(DropTargetDropEvent evt) {
		return evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	private static boolean isAcceptable(TransferHandler.TransferSupport info) {
		return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}
}
