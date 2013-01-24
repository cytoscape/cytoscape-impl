package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
    	int selectedIndex = list.getSelectedIndex();
		if(!map.containsKey(selectedIndex)){
       		map.put(selectedIndex,(T)list.getSelectedValue());
    	} else if(map.containsKey(selectedIndex)&& test!=selectedIndex){
    		map.remove(selectedIndex);
    	}
    	toggleSelection(selectedIndex);
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
