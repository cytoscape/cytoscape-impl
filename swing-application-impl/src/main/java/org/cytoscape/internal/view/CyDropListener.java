package org.cytoscape.internal.view;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.TransferHandler;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTarget;
import java.awt.Color;
import java.io.File;
import java.util.List;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.io.read.InputStreamTaskFactory;
import java.io.InputStream;
import java.io.FileInputStream;
import org.cytoscape.work.TaskManager;
import java.util.Arrays;
import javax.swing.SwingUtilities;

public class CyDropListener implements DropTargetListener {

	private JComponent panel; // the zone that accepts the drop
	private CyServiceRegistrar registrar; // how to pass the drop to a service
	private Border borderSave; // we show that a drag is ongoing by changeing
	static Border greenBorder=BorderFactory.createLineBorder(Color.green,2);
								// the border

	public static boolean isAcceptable(DropTargetDropEvent ev) {
		if (ev.isDataFlavorSupported(DataFlavor.imageFlavor))			return true;
		if (ev.isDataFlavorSupported(DataFlavor.stringFlavor))			return true;
		if (ev.isDataFlavorSupported(DataFlavor.javaFileListFlavor))	return true;		
		return false;
	}
	public static boolean isAcceptable(TransferHandler.TransferSupport info) {
		if (info.isDataFlavorSupported(DataFlavor.imageFlavor))			return true;
		if (info.isDataFlavorSupported(DataFlavor.stringFlavor))		return true;
		if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))	return true;		
		return false;
	}

	public CyDropListener(JComponent parentPanel, CyServiceRegistrar reg)
	{
// 		System.out.println("CyDropListener1");	
		panel = parentPanel;
		registrar = reg;
		parentPanel.setTransferHandler(new TransferHandler() {
	
		        @Override    public boolean canImport(TransferHandler.TransferSupport info) {
		        	return	isAcceptable(info);
		        }
	
		        @Override      public boolean importData(TransferHandler.TransferSupport info) {
		    		System.out.println("importData");
		            if (!info.isDrop())                 return false;
		            if (!isAcceptable(info))            return false;
	
		            Transferable t = info.getTransferable();
	            if (info.isDataFlavorSupported(DataFlavor.stringFlavor)) 
	            	{        
			    		System.out.println("annotate with Text: ");			//+ info.getTransferable().getTransferData()
			            try {
			            		System.out.println("" + t.getTransferData(DataFlavor.stringFlavor));
			            }
			            catch (Exception e) {  System.out.println("Exception"); }

	            	}
		            if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
		            	{        
	
		            // Get the fileList that is being dropped.
		            List<File> data;
		            try {
		                data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
		            } 
		            catch (Exception e) { return false; }
	//		            DefaultListModel model = (DefaultListModel) fileDropper.getModel();
		            for (File file : data) {
	//		                model.addElement(file);
		            }
		            return true;
		        }
		        return false;
	        }
	    });
		new DropTarget(parentPanel,this);
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
// 		System.out.println("Enter");
		borderSave = panel.getBorder();
		panel.setBorder(greenBorder);

	}

	@Override
	public void dragExit(DropTargetEvent dtde) {
// 		System.out.println("Exit");
		panel.setBorder(borderSave);
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
// 		System.out.println("move");

	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
// 		System.out.println("Drop it here!");
		panel.setBorder(borderSave);
		dtde.acceptDrop(dtde.getDropAction());
//		panel.getTransferHandler().importData(panel, dtde.getTransferable());
        if (!isAcceptable(dtde))            return;

        Transferable t = dtde.getTransferable();
        if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) 
    	{        
    		System.out.println("annotate with Text: ");			//+ info.getTransferable().getTransferData()
            try {
            		String txt = "" + t.getTransferData(DataFlavor.stringFlavor);
            		System.out.println("" + txt);
            }
            catch (Exception e) {  System.out.println("Text Exception: " + e.getMessage()); }
            return;

    	}
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) 
        {        
    		System.out.println("Drop file handler");
            // Get the fileList that is being dropped.
	        List<File> data;
	        try {
	            data = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
	        } 
	        catch (Exception e) { 
	        	System.out.println("Exception: " + e.getMessage()); 
	        	return; 
	        }
	        loadFiles(data);
	        return;
        }

	}
	
	private void loadFiles(List<File> data)
	{
        for (File file : data) {
            System.out.println((file.isDirectory() ? "import directory: "  : "import: ") + file.getName());
           if ( file.isDirectory())
           {
              loadFiles( Arrays.asList( file.listFiles()));
           }
           else
           {
                final InputStreamTaskFactory factory = registrar.getService(InputStreamTaskFactory.class);
        	    final TaskManager taskManager = registrar.getService(TaskManager.class);
                if (factory != null)
                {    
                    System.out.println("Factory: " + ((factory != null) ? factory.toString() : "MISSING"));

                   try
                   {
                        if (file.getName().toUpperCase().endsWith(".SIF"))
                        {
                            InputStream stream = new FileInputStream(file);
                    		SwingUtilities.invokeLater(new Runnable() {
			    	            public void run() {
                                    taskManager.execute(factory.createTaskIterator(stream, file.getName()));
			    	            }
			                });
               
                            System.out.println("DONE IMPORTING: " + file.getName());
                        }
                    }
                    catch (Exception e)     {
                    	System.out.println("Exception: " + e.getMessage()); 
                    }   
               }
           } 
        }
	}
	

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		System.out.println("dropActionChanged");

	}
}
