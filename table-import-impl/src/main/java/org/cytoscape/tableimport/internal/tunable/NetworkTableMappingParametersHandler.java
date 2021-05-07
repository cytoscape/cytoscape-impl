package org.cytoscape.tableimport.internal.tunable;

import java.awt.BorderLayout;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.NetworkTableMappingParameters;
import org.cytoscape.tableimport.internal.task.TableImportContext;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class NetworkTableMappingParametersHandler extends AbstractGUITunableHandler {

	private final TableImportContext tableImportContext;
	private final ImportType dialogType;
    private final CyServiceRegistrar serviceRegistrar;
    
	private ImportTablePanel importTablePanel;
	private NetworkTableMappingParameters ntmp;
    
	protected NetworkTableMappingParametersHandler(
			Field field,
			Object instance,
			Tunable tunable, 
			ImportType dialogType,
			TableImportContext tableImportContext,
			CyServiceRegistrar serviceRegistrar
	) {
		super(field, instance, tunable);
		this.dialogType = dialogType;
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}
	
	protected NetworkTableMappingParametersHandler(
			Method getter,
			Method setter,
			Object instance,
			Tunable tunable,
			ImportType dialogType,
			TableImportContext tableImportContext,
			CyServiceRegistrar serviceRegistrar
	) {
		super(getter, setter, instance, tunable);
		this.dialogType = dialogType;
		this.tableImportContext = tableImportContext;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}
	
	private void init() {
		try {
			ntmp = (NetworkTableMappingParameters) getValue();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		panel = new JPanel(new BorderLayout());
		
		try {
			importTablePanel = new ImportTablePanel(dialogType, ntmp.is, ntmp.fileType, null, tableImportContext,
					serviceRegistrar);
		} catch (Exception e) {
			final JLabel errorLabel = new JLabel(
					"<html><h3>Error: Could not Initialize Preview.</h3>" +
					"<p>The selected file may be empty or contain invalid entries.<br>" +
					"Please check the contents of the original file and try again.</p></html>"
			);
			errorLabel.setForeground(LookAndFeelUtil.getErrorColor());
			errorLabel.setHorizontalTextPosition(JLabel.CENTER);
			errorLabel.setHorizontalAlignment(JLabel.CENTER);
			errorLabel.setFont(errorLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));

			panel.add(errorLabel, BorderLayout.CENTER);
			
			return;
		}
		
		panel.add(importTablePanel, BorderLayout.CENTER);
	}
	
	@Override
	public void handle() {
		try {
			if (importTablePanel != null) {
				ntmp = importTablePanel.getNetworkTableMappingParameters();
				setValue(ntmp);
			}
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
