/*
 * LegendDialog.java
 */
package org.cytoscape.view.vizmap.gui.internal.view.legend;

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
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.freehep.graphicsbase.util.export.ExportDialog;
import org.freehep.graphicsio.gif.GIFExportFileType;
import org.freehep.graphicsio.pdf.PDFExportFileType;
import org.freehep.graphicsio.raw.RawImageWriterSpi;
import org.freehep.graphicsio.svg.SVGExportFileType;

//TODO: not working. Should create utility class to generate legend from given mapping.

/**
 * Dialog for legend
 */
@SuppressWarnings("serial")
public class LegendDialog extends JDialog {

	protected final Color BACKGROUND_COLOR = Color.WHITE;
	protected final Color TITLE_COLOR = Color.BLACK;
	
	private final VisualStyle visualStyle;

	private JPanel panel;
	private JButton exportBtn;
	private JButton cancelBtn;
	private JScrollPane scrollPane;

	private final ServicesUtil servicesUtil;

	public LegendDialog(final VisualStyle vs, final ServicesUtil servicesUtil) {
		this.setModal(true);

		visualStyle = vs;
		this.servicesUtil = servicesUtil;

		initComponents();
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		IIORegistry reg = IIORegistry.getDefaultInstance();
		reg.registerApplicationClasspathSpis();

		// We need the RawImageWriter for PDFs and it doesn't register properly through OSGi
		reg.registerServiceProvider(new RawImageWriterSpi(), ImageWriterSpi.class);
	}

	public void showDialog(final Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel generateLegendPanel(final VisualStyle visualStyle) {
		// Setup Main Panel
		final JPanel legend = new JPanel();
		legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
		legend.setBackground(BACKGROUND_COLOR);

		final Collection<VisualMappingFunction<?, ?>> mappings = visualStyle.getAllVisualMappingFunctions();

		legend.setBorder(new TitledBorder(
				new LineBorder(TITLE_COLOR, 1),
				"Visual Legend for " + visualStyle.getTitle(),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.CENTER, 
				new Font("SansSerif", Font.BOLD, 16),
				TITLE_COLOR
		));

		createMappingLegends(mappings, legend);

		return legend;
	}

	@SuppressWarnings("rawtypes")
	private void createMappingLegends(final Collection<VisualMappingFunction<?, ?>> mappings, final JPanel legend) {
		for (VisualMappingFunction<?, ?> map : mappings) {
			final CyApplicationManager appMgr = servicesUtil.get(CyApplicationManager.class);
			final JPanel mappingLenegd;

			if (map instanceof ContinuousMapping) {
				mappingLenegd = new ContinuousLegendPanel(visualStyle, (ContinuousMapping) map, appMgr
						.getCurrentNetwork().getDefaultNodeTable(), servicesUtil);
			} else if (map instanceof DiscreteMapping) {
				mappingLenegd = new DiscreteLegendPanel((DiscreteMapping<?, ?>) map, servicesUtil);
			} else if (map instanceof DiscreteMapping) {
				mappingLenegd = new PassthroughLegendPanel((PassthroughMapping<?, ?>) map, servicesUtil);
			} else {
				continue;
			}

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
		this.setBackground(BACKGROUND_COLOR);
		this.setTitle("Visual Legend for " + visualStyle.getTitle() + " ");

		panel = generateLegendPanel(visualStyle);
		scrollPane = new JScrollPane(panel);

		exportBtn = new JButton(new AbstractAction("Export") {
			@Override
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(exportBtn, cancelBtn);

		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
    	contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(scrollPane)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane)
				.addComponent(buttonPanel)
		);
		
		setContentPane(contentPane);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), exportBtn.getAction(), cancelBtn.getAction());
		getRootPane().setDefaultButton(exportBtn);
		
		setPreferredSize(new Dimension(650, 500));
		pack();
		repaint();
	}

	private void export() {
		final ExportDialog export = new ExportDialog();
		export.addExportFileType(new SVGExportFileType());
		export.addExportFileType(new GIFExportFileType());
		// This should work, but I always get an error
		export.addExportFileType(new PDFExportFileType());
		
		export.showExportDialog(null, "Export legend as ...", panel, "export");
		dispose();
	}
}
