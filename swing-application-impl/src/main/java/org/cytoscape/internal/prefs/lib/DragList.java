package org.cytoscape.internal.prefs.lib;

import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class DragList extends JList<String>
{
	private static final long serialVersionUID = 1L;
	public DragList()
	{
		super(new DefaultListModel<String>());
		MyMouseAdaptor myMouseAdaptor = new MyMouseAdaptor(this);
		addMouseListener(myMouseAdaptor);
		addMouseMotionListener(myMouseAdaptor);
	}
	static String SPACE = " ";
	public void setValues(String valList, String prefix)
	{
		DefaultListModel<String> model = (DefaultListModel<String>) getModel();
		String[] lines = valList.split(",");
		for (String line : lines)
		{
			if (line.startsWith(prefix))
				line = SPACE + line.substring(prefix.length());
			model.addElement(line);
		}
	}
	public String getValues(String prefix)
	{
		DefaultListModel<String> model = (DefaultListModel<String>) getModel();
		Object[] strings = model.toArray();
		StringBuilder builder = new StringBuilder();
		for (Object s : strings)
		{
			String str = "" + s;
			if (str.startsWith(SPACE))
				str = str.replace(SPACE , prefix);
			builder.append(str + ",");
		}
		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
}
    //--------------------------------------------------------------------------
 class MyMouseAdaptor extends MouseInputAdapter {
    private boolean mouseDragging = false;
    private int dragSourceIndex;
    DragList myList;
    DefaultListModel<String> myListModel;
    
    MyMouseAdaptor (DragList theList)
    {
	    	super();
	    	myList = theList;
	    	myListModel = (DefaultListModel<String>) myList.getModel();
    }
    
    @Override   public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            dragSourceIndex = myList.getSelectedIndex();
            mouseDragging = true;
        }
    }

    @Override  public void mouseReleased(MouseEvent e) {   mouseDragging = false;   }

    @Override  public void mouseDragged(MouseEvent e) {
            if (mouseDragging) {
                int currentIndex = myList.locationToIndex(e.getPoint());
                if (currentIndex != dragSourceIndex) {
                    boolean movingUp = currentIndex < dragSourceIndex;
                    int target = swap(dragSourceIndex, movingUp);
                    myList.getSelectionModel().setSelectionInterval(target, target);
                }
            }
    }
   private int swap(int idx, boolean movingUp)
        {
            	String dragElement = myListModel.getElementAt(idx);
            	myListModel.remove(idx);
            	int target = idx + (movingUp ? -1 : 1);
            	myListModel.insertElementAt(dragElement, target);
            dragSourceIndex = target;
            return target;
        }
    }	
