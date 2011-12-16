package org.cytoscape.psi_mi.internal.plugin;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import psidev.psi.mi.xml.PsimiXmlLightweightReader;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.xmlindex.IndexedEntry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/** 
 * Tests basic functions of PSIMI Library
 *
 */
public class PSIMIXMLLibTest {

	@Before
	public void clean() {
		System.gc();
	}
	
	@Test
	public void PSIMILibraryJAXBTest() throws Exception {

		final PsimiXmlVersion xmlVersion = PsimiXmlVersion.VERSION_254;
		final File intactFile = new File("src/test/resources/testData/intact_21798944_arath-2011-2_04.xml");
		
		// Test JAXB reader
		long start = System.currentTimeMillis();
		final PsimiXmlReader reader1 = new PsimiXmlReader(xmlVersion);
		final EntrySet result1 = reader1.read(intactFile);
		System.out.println("JAXB loading Time = " + (System.currentTimeMillis()-start));
		assertNotNull(result1);
		assertEquals(1, result1.getEntries().size());
		final Entry entry = result1.getEntries().iterator().next();
		final Collection<Interaction> itrs = entry.getInteractions();
		assertEquals(400, itrs.size());
	}
	
	// This takes a long time, so execute it only when necessary.
	//@Test
	public void PSIMILibraryLightweightTest() throws Exception {

		final PsimiXmlVersion xmlVersion = PsimiXmlVersion.VERSION_25_UNDEFINED;
//		final File intactFile = new File("src/test/resources/testData/intact_21798944_arath-2011-2_04.xml");
		File intactFile = new File("src/test/resources/testData/HPRD_SINGLE_PSIMI_041210.xml");
		// Test Lightweight reader
		final long start = System.currentTimeMillis();
		final PsimiXmlLightweightReader reader = new PsimiXmlLightweightReader(intactFile, xmlVersion);
		final List<IndexedEntry> result = reader.getIndexedEntries();
		System.out.println("Lightweight Time = " + (System.currentTimeMillis()-start));
		assertNotNull(result);
	}

}
