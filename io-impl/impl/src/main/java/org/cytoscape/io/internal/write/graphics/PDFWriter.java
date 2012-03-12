package org.cytoscape.io.internal.write.graphics;

import java.awt.Graphics2D;
import java.io.OutputStream;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
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

	private static final Logger logger = LoggerFactory.getLogger(PDFWriter.class);

	@Tunable(description="Export text as font")
	public boolean exportTextAsFont = true;
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
	
	private final Double width;
	private final Double height;
	private final RenderingEngine<?> engine;
	
	private final OutputStream stream;

	public PDFWriter(final RenderingEngine<?> engine, final OutputStream stream) {
		if (engine == null)
			throw new NullPointerException("Rendering Engine is null.");
		if (stream == null)
			throw new NullPointerException("Stream is null.");

		this.engine = engine;
		this.stream = stream;

		width = engine.getViewModel().getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH);
		height = engine.getViewModel().getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT);

		logger.debug("PDFWriter created.");
	}
	

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO should be accomplished with renderer properties
		// view.setPrintingTextAsShape(!exportTextAsFont);

		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Creating PDF image...");

		logger.debug("PDF Rendering start");
		final Rectangle pageSize = PageSize.LETTER;
		final Document document = new Document(pageSize);

		logger.debug("Document created: " + document);
		
		final PdfWriter writer = PdfWriter.getInstance(document, stream);
		document.open();
		
		taskMonitor.setProgress(0.1);
		
		final PdfContentByte canvas = writer.getDirectContent();
		logger.debug("CB0 created: " + canvas.getClass());
		
		final float pageWidth = pageSize.getWidth();
		final float pageHeight = pageSize.getHeight();
		
		logger.debug("Page W: " + pageWidth + " Page H: " + pageHeight);
		final DefaultFontMapper fontMapper = new DefaultFontMapper();
		logger.debug("FontMapper created = " + fontMapper);
		Graphics2D g = null;
		logger.debug("!!!!! Enter block 2");
		
		engine.setProperties("exportTextAsShape", new Boolean(!exportTextAsFont).toString());
		
		taskMonitor.setProgress(0.2);
		
		if (exportTextAsFont) {
			g = canvas.createGraphics(pageWidth, pageHeight, new DefaultFontMapper());
		} else {
			g = canvas.createGraphicsShapes(pageWidth, pageHeight);
		}
		
		taskMonitor.setProgress(0.4);
		
		logger.debug("##### G2D created: " + g);
		
		double imageScale = Math.min(pageSize.getWidth() / width, pageSize.getHeight() / height);
		g.scale(imageScale, imageScale);

		logger.debug("##### Start Rendering Phase 2: " + engine.toString());
		engine.printCanvas(g);
		logger.debug("##### Canvas Rendering Done: ");
			
		taskMonitor.setProgress(0.8);
		
		g.dispose();
		document.close();
		writer.close();
		
		stream.close();

		logger.debug("PDF rendering finished.");
		taskMonitor.setProgress(1.0);
	}
	
}
