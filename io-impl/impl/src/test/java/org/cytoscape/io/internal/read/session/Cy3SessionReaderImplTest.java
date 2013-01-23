package org.cytoscape.io.internal.read.session;

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Assert;
import org.junit.Test;

public class Cy3SessionReaderImplTest {
	@Test
	public void testEscaping() throws Exception {
		NetworkViewTestSupport support = new NetworkViewTestSupport();
		CyNetworkView view = support.getNetworkView();
		CyNetwork network = view.getModel();
		String title = "Network_With-Special Characters<>?:\"{}|=+()*&^%$#@![]|;',./\\";
		network.getRow(network).set(CyNetwork.NAME, title);
		
		{
			Matcher matcher = Cy3SessionReaderImpl.NETWORK_NAME_PATTERN.matcher(SessionUtil.getXGMMLFilename(network));
			Assert.assertTrue(matcher.matches());
			Assert.assertEquals(String.valueOf(network.getSUID()), matcher.group(1));
			Assert.assertEquals(title + ".xgmml", decode(matcher.group(3)));
		}
		{
			Matcher matcher = Cy3SessionReaderImpl.NETWORK_VIEW_NAME_PATTERN.matcher(SessionUtil.getXGMMLFilename(view));
			Assert.assertTrue(matcher.matches());
			Assert.assertEquals(String.valueOf(network.getSUID()), matcher.group(1));
			Assert.assertEquals(String.valueOf(view.getSUID()), matcher.group(2));
			Assert.assertEquals(title + ".xgmml", decode(matcher.group(4)));
		}
	}

	private String decode(String encodedText) throws UnsupportedEncodingException {
		return URLDecoder.decode(encodedText, "UTF-8");
	}
}
