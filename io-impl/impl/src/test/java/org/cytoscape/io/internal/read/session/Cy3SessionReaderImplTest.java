package org.cytoscape.io.internal.read.session;

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
