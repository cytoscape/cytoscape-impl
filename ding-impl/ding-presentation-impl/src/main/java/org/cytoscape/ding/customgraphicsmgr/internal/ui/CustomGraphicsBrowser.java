package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
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

	public CustomGraphicsBrowser(final CustomGraphicsManager manager) {
		pool = manager;

		initComponents();
		addAllImages();
	}

	private void initComponents() {
		this.setMaximumSize(new Dimension(300, 10000));
		model = new CustomGraphicsListModel();
		this.setModel(model);
		this.setCellRenderer(new CustomGraphicsCellRenderer());
		this.setDropTarget(new URLDropTarget());
	}

	public void removeCustomGraphics(final CyCustomGraphics<?> cg) {
		model.removeElement(cg);
	}

	/**
	 * Add on-memory images to Model.
	 */
	private void addAllImages() {
		final Collection<CyCustomGraphics> graphics = pool.getAllCustomGraphics();

		for (CyCustomGraphics<?> cg : graphics) {
			if (cg instanceof NullCustomGraphics == false)
				model.addElement(cg);
		}
	}

	private void addCustomGraphics(final String urlStr) {
		CyCustomGraphics<?> cg = null;
		
		try {
			cg = new URLImageCustomGraphics<>(pool.getNextAvailableID(), urlStr);
			if (cg != null) {
				pool.addCustomGraphics(cg, new URL(urlStr));
				model.addElement(cg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class URLDropTarget extends DropTarget {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			final Transferable trans = dtde.getTransferable();
			// dumpDataFlavors(trans);
			boolean gotData = false;
			
			try {
				if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

					final List<File> fileList = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);

					for (File file : fileList) {
						addCustomGraphics(file.toURI().toURL().toString());
					}
					gotData = true;
				} else if (trans.isDataFlavorSupported(urlFlavor)) {
					URL url = (URL) trans.getTransferData(urlFlavor);
					// Add image
					addCustomGraphics(url.toString());
					gotData = true;
				} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String s = (String) trans.getTransferData(DataFlavor.stringFlavor);

					URL url = new URL(s);
					addCustomGraphics(url.toString());
					gotData = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dtde.dropComplete(gotData);
			}
		}

		// This is for debugging
		private void dumpDataFlavors(Transferable trans) {
			DataFlavor[] flavors = trans.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				System.out.println("*** " + i + ": " + flavors[i]);
			}
		}
	}
	
	@Override
	public void handleEvent(CustomGraphicsLibraryUpdatedEvent e) {
		// Clear the model, and build new List from current pool of graphics
		model.removeAllElements();
		model.clear();

		addAllImages();
	}
}
