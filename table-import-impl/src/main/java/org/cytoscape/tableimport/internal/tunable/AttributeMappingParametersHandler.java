package org.cytoscape.tableimport.internal.tunable;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

public class AttributeMappingParametersHandler  extends AbstractGUITunableHandler {
	
	private int dialogType;
    private CyTableManager tableManager;
    
	private ImportTablePanel importTablePanel;

	AttributeMappingParameters amp;

	protected AttributeMappingParametersHandler(final Field field, final Object obj, final Tunable t,
			final int dialogType, final CyTableManager tableManager) {
		
		super(field, obj, t);
		this.dialogType = dialogType;
		this.tableManager = tableManager;
		//importTablePanel = null;
		
		init();
		
		
	}
	
	protected AttributeMappingParametersHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable,
			final int dialogType,final CyTableManager tableManager){
		
		super(getter, setter, instance, tunable);
		this.dialogType = dialogType;
		this.tableManager = tableManager;
		
		init();
		
	}

	
	private void init() {

		try {
			amp = (AttributeMappingParameters) getValue();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		try {
			importTablePanel =
				new ImportTablePanel(dialogType, amp.is,
				                     amp.fileType, null,null, null, null,
				                     null, null, null, tableManager); 
		} catch (Exception e) {
			throw new IllegalStateException("Could not initialize ImportTablePanel.", e);
		}
		
		panel.add(importTablePanel);
		
	}
	@Override
	public void handle(){ 
		// TODO Auto-generated method stub
		try {
			amp = importTablePanel.getAttributeMappingParameters();
		
			setValue(amp);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
