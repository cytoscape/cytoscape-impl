package org.cytoscape.work.internal.tunables.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.work.util.*;


public class CheckListManager<T> extends MouseAdapter implements ActionListener, ListSelectionListener{
	
    private ListSelectionModel selectionModel = new DefaultListSelectionModel(); 
    private JList list = new JList();
    ArrayList<T> arrayOut = new ArrayList<T>();
    ArrayList<Integer> arrayTest=null;
    ListMultipleSelection<T> LMS;
    int hotspot = new JCheckBox().getPreferredSize().width;
    Map<Integer,T> map = new HashMap<Integer,T>();
    int test;
 
    public CheckListManager(JList list,ListMultipleSelection<T> LMS){
    	this.LMS=LMS;
        this.list = list;
        list.setVisibleRowCount(4);
        list.setCellRenderer(new CheckListCellRenderer(list.getCellRenderer(), selectionModel)); 
        list.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED); 
        list.addMouseListener(this); 
        selectionModel.addListSelectionListener(this);
    } 
 
    public ListSelectionModel getSelectionModel(){ 
        return selectionModel; 
    } 
 
    private void toggleSelection(int index){ 
        if(index<0)
            return; 
        if(selectionModel.isSelectedIndex(index)){
            selectionModel.removeSelectionInterval(index, index);}
        else {
            selectionModel.addSelectionInterval(index, index);}
    }
 
    
    public Map<Integer,T> getMap(){
    	if(!map.containsKey(list.getSelectedIndex())){
       		map.put(list.getSelectedIndex(),(T)list.getSelectedValue());
    	} else if(map.containsKey(list.getSelectedIndex())&& test!=list.getSelectedIndex()){
    		map.remove(list.getSelectedIndex());
    	}
    	toggleSelection(list.getSelectedIndex());
    	test=list.getSelectedIndex();
    	return map;
    }
    
    
    
    public void mouseClicked(MouseEvent me){
        int index = list.locationToIndex(me.getPoint()); 
        if(index<0) 
            return; 
        if(me.getX()>list.getCellBounds(index, index).x+hotspot) 
            return; 
        toggleSelection(index);
    } 

 
    public void valueChanged(ListSelectionEvent e){ 
        list.repaint(list.getCellBounds(e.getFirstIndex(), e.getLastIndex()));
        
    } 
 

    public void actionPerformed(ActionEvent e){
        toggleSelection(list.getSelectedIndex());
    }
} 
