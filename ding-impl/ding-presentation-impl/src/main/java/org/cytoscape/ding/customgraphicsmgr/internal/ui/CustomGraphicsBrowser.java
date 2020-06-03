package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.image.AbstractURLImageCustomGraphics;
import org.cytoscape.ding.customgraphics.image.BitmapCustomGraphics;
import org.cytoscape.ding.customgraphics.image.SVGCustomGraphics;
import org.cytoscape.ding.customgraphicsmgr.internal.event.CustomGraphicsLibraryUpdatedEvent;
import org.cytoscape.ding.customgraphicsmgr.internal.event.CustomGraphicsLibraryUpdatedListener;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXList;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

/**
 * Display list of images available as custom graphics
 */
@SuppressWarnings("serial")
public class CustomGraphicsBrowser extends JXList implements CustomGraphicsLibraryUpdatedListener {

	private CustomGraphicsListModel model;
	private final CustomGraphicsManager pool;

	// For drag and drop
	private static DataFlavor urlFlavor;

	static {
		try {
			urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public CustomGraphicsBrowser(CustomGraphicsManager manager) {
		pool = manager;

		initComponents();
		addAllImages();
	}

	@Override
	public void handleEvent(CustomGraphicsLibraryUpdatedEvent e) {
		// Clear the model, and build new List from current pool of graphics
		model.removeAllElements();
		model.clear();

		addAllImages();
	}
	
	public void removeCustomGraphics(CyCustomGraphics<?> cg) {
		model.removeElement(cg);
	}
	
	private void initComponents() {
		this.setMaximumSize(new Dimension(300, 10000));
		model = new CustomGraphicsListModel();
		this.setModel(model);
		this.setCellRenderer(new CustomGraphicsCellRenderer());
		this.setDropTarget(new URLDropTarget());
	}

	/**
	 * Add on-memory images to Model.
	 */
	private void addAllImages() {
		var graphics = pool.getAllCustomGraphics();

		for (var cg : graphics) {
			if (cg instanceof NullCustomGraphics == false)
				model.addElement(cg);
		}
	}

	private AbstractURLImageCustomGraphics<?> addCustomGraphics(String urlStr) {
		try {
			var url = new URL(urlStr);
			var id = pool.getNextAvailableID();
			var cg = urlStr.toLowerCase().endsWith(".svg")
					? new SVGCustomGraphics(id, urlStr, url)
					: new BitmapCustomGraphics(id, urlStr, url);
			
			if (cg != null) {
				pool.addCustomGraphics(cg, url);
				model.addElement(cg);
			}
			
			return cg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private class URLDropTarget extends DropTarget {

		private Component parent;
		private Border originalBorder;
		private final Border dropBorder = BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2);
		
		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			var trans = dtde.getTransferable();
			boolean gotData = false;
			
			AbstractURLImageCustomGraphics<?> lastCG = null;
			
			try {
				if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					var fileList = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);

					for (var file : fileList)
						lastCG = addCustomGraphics(file.toURI().toURL().toString());
					
					gotData = true;
				} else if (trans.isDataFlavorSupported(urlFlavor)) {
					var url = (URL) trans.getTransferData(urlFlavor);
					// Add image
					addCustomGraphics(url.toString());
					gotData = true;
				} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					var s = (String) trans.getTransferData(DataFlavor.stringFlavor);

					var url = new URL(s);
					addCustomGraphics(url.toString());
					gotData = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dtde.dropComplete(gotData);
			}
			
			// Select and scroll to the last added image
			if (lastCG != null)
				setSelectedValue(lastCG, true);
		}
		
		@Override
		public synchronized void dragEnter(DropTargetDragEvent dtde) {
			super.dragEnter(dtde);

			parent = getParent();

			if (parent instanceof JViewport) // In this case, we want the scroll pane
				parent = parent.getParent();

			if (parent instanceof JComponent) {
				try {
					originalBorder = ((JComponent) parent).getBorder();
					((JComponent) parent).setBorder(dropBorder);
				} catch (Exception e) {
					// Just ignore, some components do not support setBorder()...
				}
			}
		}

		@Override
		public synchronized void dragExit(DropTargetEvent dte) {
			super.dragExit(dte);

			if (parent instanceof JComponent) {
				try {
					((JComponent) parent).setBorder(originalBorder);
				} catch (Exception e) {
					// Just ignore...
				}
			}
		}
	}
}
