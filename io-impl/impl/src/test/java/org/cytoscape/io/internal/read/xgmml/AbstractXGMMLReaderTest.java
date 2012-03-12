package org.cytoscape.io.internal.read.xgmml;

import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractXGMMLReaderTest {

	@Test
	public void testIsXGMMLTransparency() {
		assertTrue(AbstractXGMMLReader.isXGMMLTransparency("nodeTransparency"));
		assertTrue(AbstractXGMMLReader.isXGMMLTransparency("edgeTransparency"));
	}

	@Test
	public void testIsOldFont() {
		assertTrue(AbstractXGMMLReader.isOldFont("nodeLabelFont"));
		assertTrue(AbstractXGMMLReader.isOldFont("cy:nodeLabelFont"));
		assertTrue(AbstractXGMMLReader.isOldFont("edgeLabelFont"));
		assertTrue(AbstractXGMMLReader.isOldFont("cy:edgeLabelFont"));
	}

	@Test
	public void testConvertXGMMLTransparencyValue() {
		assertEquals("0", AbstractXGMMLReader.convertXGMMLTransparencyValue("0"));
		assertEquals("0", AbstractXGMMLReader.convertXGMMLTransparencyValue("0.0"));
		assertEquals("255", AbstractXGMMLReader.convertXGMMLTransparencyValue("1.0"));
		assertEquals("26", AbstractXGMMLReader.convertXGMMLTransparencyValue("0.1"));
		assertEquals("128", AbstractXGMMLReader.convertXGMMLTransparencyValue("0.5"));
	}

	@Test
	public void testConvertOldFontValue() {
		assertEquals("ACaslonPro,bold,18", AbstractXGMMLReader.convertOldFontValue("ACaslonPro-Bold-0-18"));
		assertEquals("SansSerif,plain,12", AbstractXGMMLReader.convertOldFontValue("SansSerif-0-12.1"));
		assertEquals("SansSerif,bold,12", AbstractXGMMLReader.convertOldFontValue("SansSerif.bold-0.0-12.0"));
		assertEquals("SansSerif,bold,12", AbstractXGMMLReader.convertOldFontValue("SansSerif,bold,12"));
	}
}
