package org.cytoscape.io.internal.write.graphics;

import static org.junit.Assert.assertNotNull;

import java.awt.Graphics2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.DefaultFontMapper;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfWriter;


public class PDFWriterTest {
	
	private static final Logger logger = LoggerFactory.getLogger(PDFWriterTest.class);

	
	@Test
	public void itextLibTest() throws Exception {
		
		final OutputStream os = new BufferedOutputStream(new FileOutputStream("itextTest.pdf"));
		final Rectangle pageSize = PageSize.LETTER;
		final Document document = new Document(pageSize);

		logger.debug("Document created: " + document);
		
		final PdfWriter writer = PdfWriter.getInstance(document, os);
		document.open();
		
		final PdfContentByte canvas = writer.getDirectContent();
		logger.debug("CB0 created: " + canvas.getClass());
		
		final float pageWidth = pageSize.getWidth();
		final float pageHeight = pageSize.getHeight();
		
		logger.debug("Page W: " + pageWidth + " Page H: " + pageHeight);
		final DefaultFontMapper fontMapper = new DefaultFontMapper();
		logger.debug("FontMapper created = " + fontMapper);
		final Graphics2D g = canvas.createGraphics(pageWidth, pageHeight);
		logger.debug("! G2D created = " + g);
		
		
		assertNotNull(g);
		g.dispose();
		
		document.close();
		os.close();
	}

}
