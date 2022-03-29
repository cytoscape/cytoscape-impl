package org.cytoscape.io.internal.write.graphics;

import static org.cytoscape.io.internal.write.graphics.PDFWriter.PreDefinedPageSize.*;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.awt.Graphics2D;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * PDF exporter by the iText library.
 */
public class PDFWriter extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
	private static final int UNITS_PER_INCH = 72;
	
	private static final String PORTRAIT  = "Portrait";
	private static final String LANDSCAPE = "Landscape";
	
	enum PreDefinedPageSize {
		AUTO("Auto"),
		CUSTOM("Custom"),
		LETTER("Letter", PageSize.LETTER),
		LEGAL("Legal", PageSize.LEGAL),
		TABLOID("Tabloid", PageSize.TABLOID),
		A0("A0", PageSize.A0),
		A1("A1", PageSize.A1),
		A2("A2", PageSize.A2),
		A3("A3", PageSize.A3),
		A4("A4", PageSize.A4),
		A5("A5", PageSize.A5);
		
		final Rectangle size;
		final String label;
		
		PreDefinedPageSize(String label, Rectangle size) {
			this.size = size;
			this.label = label;
		}
		PreDefinedPageSize(String label) {
			this(label, null);
		}
		
		static PreDefinedPageSize fromLabel(String label) {
			for(var ps : values()) {
				if(ps.label.equals(label)) {
					return ps;
				}
			}
			return null;
		}
	}
	
	

	// ----------------------------
	@Tunable(
			description = "Export text as font:",
			longDescription = "If true (the default value), texts will be exported as fonts.",
			groups = { "_Others" },
			gravity = 2.1
	)
	public boolean exportTextAsFont = true;
	
	
	
	// ----------------------------
	@Tunable(
			description = "Hide Labels:",
			longDescription = "If true then node and edge labels will not be visible in the image.",
			exampleStringValue = "true",
			groups = { "_Others" },
			gravity = 2.2
	)
	public boolean hideLabels;
	
	
	
	// ----------------------------
	public ListSingleSelection<String> pageSize;
	
	@Tunable(
			description = "Page Size:",
			longDescription = "Predefined standard page size, or choose custom.",
			exampleStringValue = "Letter",
			groups = { "Page Size" },
			gravity = 1.1
	)
	public ListSingleSelection<String> getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(ListSingleSelection<String> pageSize) {
		this.pageSize = pageSize;
	} 
	
	
	
	// ----------------------------
	public ListSingleSelection<String> orientation = new ListSingleSelection<>(PORTRAIT, LANDSCAPE);
	
	@Tunable(
			description = "Orientation:",
			longDescription = "Page orientation, portrait or landscape.",
			exampleStringValue = PORTRAIT,
			groups = { "Page Size" },
			dependsOn = "PageSize!=Custom",
			gravity = 1.2
	)
	public ListSingleSelection<String> getOrientation() {
		return orientation;
	}
	
	public void setOrientation(ListSingleSelection<String> orientation) {
		this.orientation = orientation;
	} 
	
	
	
	// ----------------------------
	public float customWidthInches = PageSize.LETTER.getWidth() / UNITS_PER_INCH;
	
	@Tunable(
			description = "Width (inches):",
			longDescription = "The width (in inches) of the exported image when pageSize=Custom.",
			exampleStringValue = "10.0",
			groups = { "Page Size" },
			params = "alignments=vertical",
			listenForChange = { "PageSize", "Orientation" },
			dependsOn = "PageSize=Custom",
			gravity = 1.3
	)
	public float getCustomWidthInches() {
		return getPageSizeInches(true);
	}
	
	public void setCustomWidthInches(float width) {
		this.customWidthInches = width;
	}
	
	
	
	// ----------------------------
	public float customHeightInches = PageSize.LETTER.getHeight() / UNITS_PER_INCH;
	
	@Tunable(
			description = "Height (inches):",
			longDescription = "The height (in inches) of the exported image when pageSize=Custom.",
			exampleStringValue = "10.0",
			groups = { "Page Size" },
			params = "alignments=vertical",
			listenForChange = { "PageSize", "Orientation" },
			dependsOn = "PageSize=Custom",
			gravity = 1.4
	)
	public float getCustomHeightInches() {
		return getPageSizeInches(false);
	}
	
	public void setCustomHeightInches(float height) {
		this.customHeightInches = height;
	}
	
	
	
	private float getPageSizeInches(boolean width) {
		PreDefinedPageSize pageSize = PreDefinedPageSize.fromLabel(this.pageSize.getSelectedValue());
		
		if(pageSize == AUTO) {
			float inches = (width ? networkWidth.floatValue() : networkHeight.floatValue()) / UNITS_PER_INCH;
			return new BigDecimal(inches).setScale(1, RoundingMode.HALF_UP).floatValue();
		} else if(pageSize == CUSTOM) {
			return width ? customWidthInches : customHeightInches;
		} else {
			if(LANDSCAPE.equals(orientation.getSelectedValue()))
				width = !width;
			float inches = (width ? pageSize.size.getWidth() : pageSize.size.getHeight()) / UNITS_PER_INCH;
			return new BigDecimal(inches).setScale(1, RoundingMode.HALF_UP).floatValue();
		}
	}
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
	
	private final Double networkWidth;
	private final Double networkHeight;
	private final RenderingEngine<?> engine;
	
	private final OutputStream stream;

	public PDFWriter(final RenderingEngine<?> engine, final OutputStream stream) {
		if (engine == null)
			throw new NullPointerException("Rendering Engine is null.");
		if (stream == null)
			throw new NullPointerException("Stream is null.");

		this.engine = engine;
		this.stream = stream;
		
		pageSize = new ListSingleSelection<>(
				AUTO.label, CUSTOM.label, LETTER.label, LEGAL.label, TABLOID.label, 
				A0.label, A1.label, A2.label, A3.label, A4.label, A5.label
		);
		pageSize.setSelectedValue(LETTER.label);

		networkWidth  = engine.getViewModel().getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH);
		networkHeight = engine.getViewModel().getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT);

		logger.debug("PDFWriter created.");
	}
	
	
	private Rectangle computePageDimensions() {
		PreDefinedPageSize pageSize = PreDefinedPageSize.fromLabel(this.pageSize.getSelectedValue());
		String orientation = this.orientation.getSelectedValue();
		
		if(pageSize == AUTO)
			return new Rectangle(networkWidth.floatValue(), networkHeight.floatValue());
		else if(pageSize == CUSTOM)
			return new Rectangle(customWidthInches * UNITS_PER_INCH, customHeightInches * UNITS_PER_INCH);
		else if(LANDSCAPE.equals(orientation))
			return new Rectangle(pageSize.size.getHeight(), pageSize.size.getWidth());
		else
			return pageSize.size;
	}
	

	@Override
	public void run(TaskMonitor tm) throws Exception {
		// TODO should be accomplished with renderer properties
		// view.setPrintingTextAsShape(!exportTextAsFont);

		tm.setTitle("PDF Writer");
		tm.setStatusMessage("Creating PDF image...");
		tm.setProgress(0.0);

		logger.debug("PDF Rendering start");
		
		final Rectangle dimensions = computePageDimensions();
		final Document document = new Document(dimensions);
		
		logger.debug("Document created: " + document);
		
		final PdfWriter writer = PdfWriter.getInstance(document, stream);
		document.open();
		
		tm.setProgress(0.1);
		
		final PdfContentByte canvas = writer.getDirectContent();
		logger.debug("CB0 created: " + canvas.getClass());
		
		final float pageWidth  = dimensions.getWidth();
		final float pageHeight = dimensions.getHeight();
		
		logger.debug("Page W: " + pageWidth + " Page H: " + pageHeight);
		final DefaultFontMapper fontMapper = new DefaultFontMapper();
		logger.debug("FontMapper created = " + fontMapper);
		Graphics2D g = null;
		logger.debug("!!!!! Enter block 2");
		
		
		tm.setProgress(0.2);
		
		if (exportTextAsFont) {
			g = canvas.createGraphics(pageWidth, pageHeight, new DefaultFontMapper());
		} else {
			g = canvas.createGraphicsShapes(pageWidth, pageHeight);
		}
		
		tm.setProgress(0.4);
		
		logger.debug("##### G2D created: " + g);
		
		double imageScale = Math.min(dimensions.getWidth() / networkWidth, dimensions.getHeight() / networkHeight);
		g.scale(imageScale, imageScale);

		logger.debug("##### Start Rendering Phase 2: " + engine.toString());
		var props = Map.of(
			"exportTextAsShape", String.valueOf(!exportTextAsFont),
			"exportHideLabels", String.valueOf(hideLabels),
			"pdf", "true"
		);
		engine.printCanvas(g, props);
		logger.debug("##### Canvas Rendering Done: ");
			
		tm.setProgress(0.8);
		
		g.dispose();
		document.close();
		writer.close();
		
		stream.close();

		logger.debug("PDF rendering finished.");
		tm.setProgress(1.0);
	}
	
}
