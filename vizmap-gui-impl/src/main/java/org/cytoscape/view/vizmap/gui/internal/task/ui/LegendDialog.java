/*
 * LegendDialog.java
 */
package org.cytoscape.view.vizmap.gui.internal.task.ui;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.legend.ContinuousMappingLegendPanel;
import org.cytoscape.view.vizmap.gui.internal.legend.DiscreteLegend;
import org.cytoscape.view.vizmap.gui.internal.legend.PassthroughLegend;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.freehep.graphicsio.gif.GIFExportFileType;
import org.freehep.graphicsio.svg.SVGExportFileType;
import org.freehep.util.export.ExportDialog;

/**
 * Dialog for legend
 */

// TODO: not working. Should create utility class to generate legend from given
// mapping.

public class LegendDialog extends JDialog {

	private final static long serialVersionUID = 1202339876783665L;

	private final VisualStyle visualStyle;
	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	private JPanel jPanel1;
	private JButton jButton1;
	private JButton jButton2;
	private JScrollPane jScrollPane1;

	private VisualMappingFunctionFactory continuousMappingFactory;

	public LegendDialog(final VisualStyle vs, final CyApplicationManager appManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {
		super();
		this.setModal(true);

		visualStyle = vs;
		this.appManager = appManager;
		this.vmm = vmm;
		this.continuousMappingFactory = continuousMappingFactory;

		initComponents();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void showDialog(final Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel generateLegendPanel(final VisualStyle visualStyle) {
		// Setup Main Panel
		final JPanel legend = new JPanel();
		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
		legend.setBackground(Color.white);

		final Collection<VisualMappingFunction<?, ?>> mappings = visualStyle.getAllVisualMappingFunctions();

		legend.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Visual Legend for "
				+ visualStyle.getTitle(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER, new Font(
				"SansSerif", Font.BOLD, 16), Color.DARK_GRAY));

		createMappingLegends(mappings, legend);

		return legend;
	}

	private void createMappingLegends(final Collection<VisualMappingFunction<?, ?>> mappings, final JPanel legend) {
		for (VisualMappingFunction<?, ?> map : mappings) {
			final JPanel mappingLenegd;

			if (map instanceof ContinuousMapping) {
				mappingLenegd = new ContinuousMappingLegendPanel(visualStyle, (ContinuousMapping) map, appManager
						.getCurrentNetwork().getDefaultNodeTable(), appManager, vmm, continuousMappingFactory);
			} else if (map instanceof DiscreteMapping) {
				mappingLenegd = new DiscreteLegend((DiscreteMapping<?, ?>) map, appManager);
			} else if (map instanceof DiscreteMapping) {
				mappingLenegd = new PassthroughLegend((PassthroughMapping<?, ?>) map);
			} else
				continue;

			// Add passthrough mappings to the top since they don't
			// display anything besides the title.
			if (map instanceof PassthroughMapping)
				legend.add(mappingLenegd, 0);
			else
				legend.add(mappingLenegd);

			// Set padding
			mappingLenegd.setBorder(new EmptyBorder(15, 30, 15, 30));
		}
	}

	private void initComponents() {
		this.setBackground(Color.white);
		this.setTitle("Visual Legend for " + visualStyle.getTitle());

		jPanel1 = generateLegendPanel(visualStyle);

		jScrollPane1 = new JScrollPane();
		jScrollPane1.setViewportView(jPanel1);

		jButton1 = new JButton();
		jButton1.setText("Export");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				export();
			}
		});

		jButton2 = new JButton();
		jButton2.setText("Done");
		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(jButton1);
		buttonPanel.add(jButton2);

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.add(jScrollPane1);
		containerPanel.add(buttonPanel);

		setContentPane(containerPanel);
		setPreferredSize(new Dimension(650, 500));
		pack();
		repaint();
	}

	private void export() {
		final ExportDialog export = new ExportDialog();
		export.addExportFileType(new SVGExportFileType());
		export.addExportFileType(new GIFExportFileType());
		
		export.showExportDialog(null, "Export legend as ...", jPanel1, "export");
		dispose();
	}
}
