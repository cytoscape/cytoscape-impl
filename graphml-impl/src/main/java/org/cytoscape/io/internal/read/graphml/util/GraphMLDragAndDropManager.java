package org.cytoscape.io.internal.read.graphml.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GraphMLDragAndDropManager {
	// TODO: FIXME!
//	private static final GraphMLDragAndDropManager manager = new GraphMLDragAndDropManager();
//	
//	public static GraphMLDragAndDropManager getManager() {
//		return manager;
//	}
//	
//	private GraphMLDropTarget target = null;
//
//	public void activateTarget() {
//		if (target == null) {
//			target = new GraphMLDropTarget();
//			final CytoscapeDesktop desktop = Cytoscape.getDesktop();
//			desktop.setDropTarget(target);
//		}
//	}
//	
//	// For drag and drop
//	private static DataFlavor urlFlavor;
//	
//	static {
//		try {
//			urlFlavor = new DataFlavor(
//					"application/x-java-url; class=java.net.URL");
//		} catch (ClassNotFoundException cnfe) {
//			cnfe.printStackTrace();
//		}
//	}
//	
//	/**
//	 * D & D
//	 * 
//	 * @author Kozo.Nishida
//	 * 
//	 */
//	private class GraphMLDropTarget extends DropTarget {
//
//		private static final long serialVersionUID = -2221101934302445958L;
//		
//		public void drop(DropTargetDropEvent dtde) {
//			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
//			final Transferable trans = dtde.getTransferable();
//			boolean gotData = false;
//			try {
//				if (trans.isDataFlavorSupported(urlFlavor)) {
//					URL url = (URL) trans.getTransferData(urlFlavor);
//					// Add image
//					gotData = true;
//					System.out.println("This is valid URL: " + url.toString());
//					LoadNetworkTask.loadURL(url, true);
//				} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//					String s = (String) trans.getTransferData(DataFlavor.stringFlavor);
//					
//					URL url = new URL(s);
//					gotData = true;
//					System.out.println("This is String.  Got DD: " + url.toString());
//					LoadNetworkTask.loadURL(url, true);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				dtde.dropComplete(gotData);
//			}
//		}
//		
//		private void test(URL url) throws IOException {
//			String header = null;
//			InputStream is = null;
//			
//			try {
//				BufferedReader br = null;
//				
//				URLConnection connection = url.openConnection();
//				is = connection.getInputStream();
//				try {
//					br = new BufferedReader(new InputStreamReader(is));
//				} finally {
//					if(br != null) {
//						br.close();
//					}
//				}
//			} finally {
//				if (is != null) {
//					is.close();
//				}
//			}
//
//		}
//		
//	}
//	
}
