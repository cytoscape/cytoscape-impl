package org.cytoscape.internal.prefs;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.cytoscape.internal.prefs.lib.HBox;

public class PrefsStyles extends AbstractPrefsPanel {

	protected PrefsStyles(Cy3PreferencesRoot dlog) {
		super(dlog, "vizmap");
	}

	@Override
	public void initUI() {
		super.initUI();
		HBox box = new HBox(true, true, makeNodeList(), makeEdgeList(), makeNetworkList());
		box.setMinimumSize(new Dimension(150, 400));
		box.setMaximumSize(new Dimension(650, 400));
		box.setPreferredSize(new Dimension(650, 400));
		add(box);
	}

	//===============================================================
	private Box makeNodeList() {
		DefaultListModel<String> model = new DefaultListModel<String>();
		JList<String> tablOfColumns = new JList<String>(model);
		model.addElement("NODE_BORDER_PAINT");
		model.addElement("NODE_BORDER_WIDTH");
		model.addElement("NODE_FILL_COLOR");
		model.addElement("NODE_LABEL");
		model.addElement("NODE_LABEL_COLOR");
		model.addElement("NODE_LABEL_FONT_SIZE");
		model.addElement("NODE_SHAPE");
		model.addElement("--------------------------------------");
		model.addElement("NODE_SIZE");
		model.addElement("NODE_WIDTH");
		model.addElement("NODE_HEIGHT");
		model.addElement("NODE_TRANSPARENCY");
		model.addElement("nodeSizeLocked");
		model.addElement("NODE_CUSTOMGRAPHICS_1");
		JScrollPane container = new JScrollPane(tablOfColumns);
		tablOfColumns.setDragEnabled(true);
        MyMouseAdaptor myMouseAdaptor = new MyMouseAdaptor(tablOfColumns, model);
        tablOfColumns.addMouseListener(myMouseAdaptor);
        tablOfColumns.addMouseMotionListener(myMouseAdaptor);
        Box tableListColumn = Box.createVerticalBox();
		tableListColumn.add(container);
		return tableListColumn;
	}

	private Box makeEdgeList() {
		DefaultListModel<String> model = new DefaultListModel<String>();
		JList tablOfColumns = new JList();
		tablOfColumns.setModel(model);
		model.addElement("EDGE_LABEL");
		model.addElement("EDGE_LABEL_COLOR");
		model.addElement("EDGE_LABEL_FONT_SIZE");
		model.addElement("EDGE_LINE_TYPE");
		model.addElement("EDGE_UNSELECTED_PAINT");
		model.addElement("EDGE_SOURCE_ARROW_SHAPE");
		model.addElement("EDGE_SOURCE_ARROW_UNSELECTED_PAINT");
		model.addElement("EDGE_TARGET_ARROW_UNSELECTED_PAINT");
		model.addElement("EDGE_TARGET_ARROW_SHAPE");
		model.addElement("--------------------------------------");
		model.addElement("EDGE_STROKE_UNSELECTED_PAINT");
		model.addElement("EDGE_TRANSPARENCY");
		model.addElement("EDGE_WIDTH");
//		tablOfColumns.getColumnModel().getColumn(0).setHeaderValue("Edge Styles");
		JScrollPane container = new JScrollPane(tablOfColumns);
		Box tableListColumn = Box.createVerticalBox();
		tableListColumn.add(container);
		return tableListColumn;
	   }

	private Box makeNetworkList() {
		DefaultListModel<String> model = new DefaultListModel<String>();
		JList tablOfColumns = new JList();
		tablOfColumns.setModel(model);
		model.addElement("NETWORK_BACKGROUND_PAINT");
		model.addElement("NETWORK_TITLE");
		model.addElement("NETWORK_NODE_SELECTION");
		model.addElement("NETWORK_EDGE_SELECTION");
		model.addElement("--------------------------------------");
//		tablOfColumns.getColumnModel().getColumn(0).setHeaderValue("Network Styles");
		JScrollPane container = new JScrollPane(tablOfColumns);
		Box tableListColumn = Box.createVerticalBox();
		tableListColumn.add(container);
		return tableListColumn;
	}
	//--------------------------------------------------------------------------
    private class MyMouseAdaptor extends MouseInputAdapter {
        private boolean mouseDragging = false;
        private int dragSourceIndex;
        JList<String> myList;
        DefaultListModel<String> myListModel;
        
        MyMouseAdaptor(JList<String> theList, DefaultListModel<String> mod)
        {
        	super();
        	myList = theList;
        	myListModel = mod;
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
                    int dragTargetIndex = currentIndex;
//                    int dragTargetIndex = dragSourceIndex +  (movingUp ? -1 : 1);  // MOVING DOWN //myList.getSelectedIndex();
                    String dragElement = myListModel.getElementAt(dragSourceIndex);
                    if (dragSourceIndex < dragTargetIndex) dragTargetIndex--;
                    myListModel.remove(dragSourceIndex);
                    myListModel.insertElementAt(dragElement, dragTargetIndex);
                    myList.getSelectionModel().setSelectionInterval(dragTargetIndex, dragTargetIndex);
                    dragSourceIndex = currentIndex;
                }
            }
        }
    }	
 }
