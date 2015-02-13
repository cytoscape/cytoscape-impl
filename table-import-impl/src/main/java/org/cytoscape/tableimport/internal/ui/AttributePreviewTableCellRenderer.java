package org.cytoscape.tableimport.internal.ui;

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

//import cytoscape.data.CyAttributes;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Cell and table header renderer for preview table.
 *
 * @author kono
 */
public class AttributePreviewTableCellRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = -8441554470062692796L;
	
	public static final int PARAMETER_NOT_EXIST = -1;
	
	private int primaryKey;
	private List<Integer> aliases;
	private int ontology;
	private int taxon;
	private boolean[] importFlag;

	/*
	 * For network import
	 */
	private int source;
	private int target;
	private int interaction;

	/**
	 * Creates a new AttributePreviewTableCellRenderer object. Primary Key is required.
	 */
	public AttributePreviewTableCellRenderer(int primaryKey, List<Integer> aliases,
	                                         int ontology, int taxon, boolean[] importFlag) {
		super();
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		if (importFlag != null)
			this.importFlag = importFlag;
		
		this.source = PARAMETER_NOT_EXIST;
		this.target = PARAMETER_NOT_EXIST;
		this.interaction = PARAMETER_NOT_EXIST;

		this.primaryKey = primaryKey;
		this.ontology = ontology;
		this.taxon = taxon;

		if (aliases == null) {
			this.aliases = new ArrayList<Integer>();
		} else {
			this.aliases = aliases;
			
			for (int i : aliases)
				syncImportFlag(i);
		}

		syncImportFlag(primaryKey);
		syncImportFlag(ontology);
		syncImportFlag(taxon);
	}

	public void setImportFlag(int index, boolean flag) {
		if (importFlag != null && importFlag.length > index)
			importFlag[index] = flag;
	}

	public boolean isImportFlag(int index) {
		if (importFlag != null && importFlag.length > index)
			return importFlag[index];

		return false;
	}

	public void setAliasFlag(Integer i, boolean flag) {
		if (aliases.contains(i) && flag == false) {
			aliases.remove(i);
		} else if (!aliases.contains(i) && flag == true) {
			aliases.add(i);
			syncImportFlag(i);
		}
	}

	public int getPrimaryKeyIndex() {
		return primaryKey;
	}
	
	public void setSourceIndex(int idx) {
		source = idx;
		syncImportFlag(idx);
	}

	public void setInteractionIndex(int idx) {
		interaction = idx;
		syncImportFlag(idx);
	}

	public void setTargetIndex(int idx) {
		target = idx;
		syncImportFlag(idx);
	}

	public int getSourceIndex() {
		return source;
	}

	public int getInteractionIndex() {
		return interaction;
	}

	public int getTargetIndex() {
		return target;
	}
	
	public int getOntologyIndex() {
		return ontology;
	}
	
	public int getTaxonIndex() {
		return taxon;
	}
	
	public boolean isAlias(final int idx) {
		return aliases.contains(idx);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		setFont(getFont().deriveFont(11.0f));

		if (importFlag == null || table.getModel().getColumnCount() != importFlag.length) {
			importFlag = new boolean[table.getColumnCount()];
			Arrays.fill(importFlag, true);
		}

		if (importFlag[column]) {
			setForeground(table.getForeground());
		} else {
			setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		if (column == primaryKey) {
			setFont(getFont().deriveFont(Font.BOLD));
		} else {
			setFont(getFont().deriveFont(Font.PLAIN));
		}
		
		setText((value == null) ? "" : value.toString());

		return this;
	}
	
	private void syncImportFlag(final int idx) {
		if (idx >= 0)
			setImportFlag(idx, true);
	}
}
