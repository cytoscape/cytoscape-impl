package org.cytoscape.work.internal.submenu;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.SubmenuTunableHandler; 
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;

import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.util.List; 
import java.util.Collections; 
import java.util.ArrayList; 
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SubmenuTunableHandlerImpl extends AbstractTunableHandler implements SubmenuTunableHandler {

	private JMenuItem menuItem; 
	
	private DialogTaskManager dtm; 
	private TaskFactory tf;
	private static final String TEMP_TITLE = "Temporary Title"; 
	private static final Logger logger = LoggerFactory.getLogger(SubmenuTunableHandlerImpl.class);

	public SubmenuTunableHandlerImpl(final Field field, final Object instance, final Tunable tunable) {
		super(field,instance,tunable);
	}

	public SubmenuTunableHandlerImpl(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter,setter,instance,tunable);
	}

	public void setExecutionParams(DialogTaskManager dtm, TaskFactory tf) {
		this.dtm = dtm;
		this.tf = tf;
	}

	public void handle() {
		try {
		Object o = getValue();
		List<String> menuTitles;
		if ( o == null ) 
			menuTitles = Collections.EMPTY_LIST;
		else
			menuTitles = ((ListSingleSelection<String>)o).getPossibleValues();

		if ( menuTitles.size() <= 0 ) {
			// no list means no menu
			menuItem = null; 
		} else if ( menuTitles.size() == 1 ) {
			// assume the lone entry in the list is the title
			menuItem = new SubmenuItem(menuTitles.get(0),this,dtm,tf);		
		} else {
			// The temporary title will be replaced
			menuItem = new JMenu(TEMP_TITLE);
			for ( String title : menuTitles )
				((JMenu)menuItem).add(new SubmenuItem(title,this,dtm,tf));		
		}

		} catch (Exception e) { e.printStackTrace(); }
	}


	public void chosenMenu(String s) {
		try {
			final ListSingleSelection<String> listSingleSelection = (ListSingleSelection<String>)getValue();
			if ( listSingleSelection != null )
				listSingleSelection.setSelectedValue(s);
		} catch (Exception e) { 
			logger.debug("problem registering the chosen menu :" + s, e);
		}
	}

	public JMenuItem getSubmenuItem() {
		return menuItem;
	}

	public String toString() {
		return "SubmenuTunableHandler: ListSingleSelection<String>"; 
	}
}
