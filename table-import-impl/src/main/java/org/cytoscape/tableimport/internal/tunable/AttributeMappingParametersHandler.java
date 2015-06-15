package org.cytoscape.tableimport.internal.tunable;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.reader.AttributeMappingParameters;
import org.cytoscape.tableimport.internal.ui.ImportTablePanel;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;

public class AttributeMappingParametersHandler extends AbstractGUITunableHandler {

	private ImportType dialogType;
	private ImportTablePanel importTablePanel;
	private AttributeMappingParameters amp;
	private final CyServiceRegistrar serviceRegistrar;


	protected AttributeMappingParametersHandler(final Field field, final Object obj, final Tunable tunable,
			final ImportType dialogType, final CyServiceRegistrar serviceRegistrar) {
		super(field, obj, tunable);
		
		this.dialogType = dialogType;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}

	protected AttributeMappingParametersHandler(final Method getter, final Method setter, final Object instance,
			final Tunable tunable, final ImportType dialogType, final CyServiceRegistrar serviceRegistrar) {
		super(getter, setter, instance, tunable);
		
		this.dialogType = dialogType;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}

	private void init() {
		try {
			amp = (AttributeMappingParameters) getValue();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}

		panel = new JPanel(new BorderLayout());

		try {
			importTablePanel = new ImportTablePanel(dialogType, amp.is, amp.fileType, null, serviceRegistrar);
		} catch (Exception e) {
			JLabel errorLabel1 = new JLabel(
					"<html><h2>Error: Could not Initialize Preview.</h2>  <p>The selected file may contain invalid entries.  "
					+ "  Please check the contents of original file.</p></html>");
			errorLabel1.setForeground(Color.RED);
			errorLabel1.setHorizontalTextPosition(JLabel.CENTER);
			errorLabel1.setHorizontalAlignment(JLabel.CENTER);

			panel.add(errorLabel1);
			
			return;
		}

		panel.add(importTablePanel, BorderLayout.CENTER);
	}

	@Override
	public void handle() {
		try {
			amp = importTablePanel.getAttributeMappingParameters();
			setValue(amp);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
