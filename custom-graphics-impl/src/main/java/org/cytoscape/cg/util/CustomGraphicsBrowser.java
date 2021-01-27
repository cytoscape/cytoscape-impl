package org.cytoscape.cg.util;

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

import org.cytoscape.cg.event.CustomGraphicsLibraryUpdatedEvent;
import org.cytoscape.cg.event.CustomGraphicsLibraryUpdatedListener;
import org.cytoscape.cg.internal.image.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.internal.image.BitmapCustomGraphics;
import org.cytoscape.cg.internal.image.SVGCustomGraphics;
import org.cytoscape.cg.internal.ui.CustomGraphicsCellRenderer;
import org.cytoscape.cg.internal.ui.CustomGraphicsListModel;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.jdesktop.swingx.JXList;

/**
 * Display list of images available as custom graphics
 */
@SuppressWarnings("serial")
public class CustomGraphicsBrowser extends JXList implements CustomGraphicsLibraryUpdatedListener {

	private CustomGraphicsListModel model;
	
	private final CyServiceRegistrar serviceRegistrar;

	// For drag and drop
	private static DataFlavor urlFlavor;

	static {
		try {
			urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	public CustomGraphicsBrowser(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;

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
		var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
		var graphics = manager.getAllCustomGraphics();

		for (var cg : graphics) {
			if (cg instanceof NullCustomGraphics == false)
				model.addElement(cg);
		}
	}

	private AbstractURLImageCustomGraphics<?> addCustomGraphics(String urlStr) {
		var manager = serviceRegistrar.getService(CustomGraphicsManager.class);
		
		try {
			var url = new URL(urlStr);
			var id = manager.getNextAvailableID();
			var cg = urlStr.toLowerCase().endsWith(".svg")
					? new SVGCustomGraphics(id, urlStr, url)
					: new BitmapCustomGraphics(id, urlStr, url);
			
			if (cg != null) {
				manager.addCustomGraphics(cg, url);
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
