package org.cytoscape.io.internal.write.graphics;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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
		
		final OutputStream os = new BufferedOutputStream(new FileOutputStream("target/itextTest.pdf"));
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
