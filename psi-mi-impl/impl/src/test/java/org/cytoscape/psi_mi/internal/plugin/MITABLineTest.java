package org.cytoscape.psi_mi.internal.plugin;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.Properties;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MITABLineTest {

	File file;
	BufferedReader is;

	@Before
	public void setUp() throws Exception {
		file = new File("src/test/resources/testData/BIOGRID-ORGANISM-Bos_taurus-3.1.74.mitab");
		is = new BufferedReader(new FileReader(file));
	}

	@Test
// #ID Interactor A    ID Interactor B Alt IDs Interactor A    Alt IDs Interactor B    Aliases Interactor A    Aliases Interactor B    Interaction Detection Method    Publication 1st Author  Publication Identifiers Taxid Interactor A  Taxid Interactor B  Interaction Types   Source Database Interaction Identifiers Confidence Values
//	entrez gene/locuslink:280906|GRID:158296    entrez gene/locuslink:281119|GRID:158481    entrez gene/locuslink:RB1   entrez gene/locuslink:DNMT1|entrez gene/locuslink:BOS_7601  -   entrez gene/locuslink:DNMT(gene name synonym)   psi-mi:"MI:0004"(affinity chromatography technology)    "Robertson KD (2000)"   pubmed:10888886 taxid:9913  taxid:9913  psi-mi:"MI:0915"(physical association)  psi-mi:"MI:0463"(GRID)  GRID:261841 -
	public void testMITABLine() throws Exception {
		MITABLine mline = new MITABLine();
		
		String line;

		while ((line = is.readLine()) != null) {
			if ( line.startsWith("#") )
				continue;

			mline.readLine(line);
			
			assertEquals("280906",mline.sourceRawID);
			assertEquals("281119",mline.targetRawID);
			assertTrue(mline.srcAliases.contains("158296"));
			assertTrue(mline.srcAliases.contains("RB1"));
			assertTrue(mline.tgtAliases.contains("158481"));
			assertTrue(mline.tgtAliases.contains("DNMT1"));
			assertTrue(mline.tgtAliases.contains("BOS_7601"));
			assertEquals(1,mline.detectionMethods.size());
			assertEquals(1,mline.detectionDBs.size());
			assertTrue(mline.authors.contains("\"Robertson KD (2000)\""));
			assertEquals("pubmed",mline.publicationDBs.get(0));
			assertEquals("10888886",mline.publicationValues.get(0));
			assertEquals("9913",mline.srcTaxonIDs.get(0));
			assertEquals("9913",mline.tgtTaxonIDs.get(0));
			assertEquals("\"MI:0915\"(physical association)",mline.interactionTypes.get(0));
			assertEquals("\"MI:0463\"(GRID)",mline.sourceIDs.get(0));
			assertEquals("261841",mline.interactionIDs.get(0));
			assertEquals(0,mline.edgeScoreStrings.size());

			break;
		}
		
	}


	@Test
// #ID Interactor A    ID Interactor B Alt IDs Interactor A    Alt IDs Interactor B    Aliases Interactor A    Aliases Interactor B    Interaction Detection Method    Publication 1st Author  Publication Identifiers Taxid Interactor A  Taxid Interactor B  Interaction Types   Source Database Interaction Identifiers Confidence Values
//entrez gene/locuslink:326601|GRID:160074    entrez gene/locuslink:819210|GRID:4545  entrez gene/locuslink:H3F3A|entrez gene/locuslink:BOS_15646 entrez gene/locuslink:BRM|entrez gene/locuslink:At2g46020   entrez gene/locuslink:H3F3B(gene name synonym)  entrez gene/locuslink:ARABIDOPSIS THALIANA BRAHMA(gene name synonym)|entrez gene/locuslink:T3F17.33(gene name synonym)|entrez gene/locuslink:CHA2(gene name synonym)|entrez gene/locuslink:CHROMATIN REMODELING 2(gene name synonym)|entrez gene/locuslink:ATBRM(gene name synonym)|entrez gene/locuslink:BRAHMA(gene name synonym)|entrez gene/locuslink:CHR2(gene name synonym)   psi-mi:"MI:0047"(far western blotting)  "Farrona S (2007)"  pubmed:17825834 taxid:9913  taxid:3702  psi-mi:"MI:0407"(direct interaction)    psi-mi:"MI:0463"(GRID)  GRID:271838 -
	public void testMITABLine3() throws Exception {
		MITABLine mline = new MITABLine();
		
		String line;

		int lineNum = 0;
		while ((line = is.readLine()) != null) {
			if ( line.startsWith("#") )
				continue;
			if ( lineNum++ < 3 )
				continue;
			mline.readLine(line);
			
			assertEquals("326601",mline.sourceRawID);
			assertEquals("819210",mline.targetRawID);
			assertTrue(mline.srcAliases.contains("160074"));
			assertTrue(mline.srcAliases.contains("H3F3A"));
			assertTrue(mline.srcAliases.contains("BOS_15646"));
			assertTrue(mline.srcAliases.contains("H3F3B(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("4545"));
			assertTrue(mline.tgtAliases.contains("BRM"));
			assertTrue(mline.tgtAliases.contains("At2g46020"));
			assertTrue(mline.tgtAliases.contains("ARABIDOPSIS THALIANA BRAHMA(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("T3F17.33(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("CHA2(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("CHROMATIN REMODELING 2(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("ATBRM(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("BRAHMA(gene name synonym)"));
			assertTrue(mline.tgtAliases.contains("CHR2(gene name synonym)"));
			assertEquals("\"MI:0047\"(far western blotting)",mline.detectionMethods.get(0));
			assertEquals(1,mline.detectionDBs.size());
			assertTrue(mline.authors.contains("\"Farrona S (2007)\""));
			assertEquals("pubmed",mline.publicationDBs.get(0));
			assertEquals("17825834",mline.publicationValues.get(0));
			assertEquals("9913",mline.srcTaxonIDs.get(0));
			assertEquals("3702",mline.tgtTaxonIDs.get(0));
			assertEquals("\"MI:0407\"(direct interaction)",mline.interactionTypes.get(0));
			assertEquals("\"MI:0463\"(GRID)",mline.sourceIDs.get(0));
			assertEquals("271838",mline.interactionIDs.get(0));
			assertEquals(0,mline.edgeScoreStrings.size());

			break;
		}
	}
}
