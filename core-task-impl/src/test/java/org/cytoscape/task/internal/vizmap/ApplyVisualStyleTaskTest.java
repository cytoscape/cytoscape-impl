package org.cytoscape.task.internal.vizmap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.StyleSheet;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

public class ApplyVisualStyleTaskTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() throws Exception {
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();
		
		TaskMonitor tm = mock(TaskMonitor.class);
		
		final CyNetworkView view = nvts.getNetworkView();
		
		final VisualMappingManager vmm = mock(VisualMappingManager.class);
		ApplyVisualStyleTask task = new ApplyVisualStyleTask(view, vmm);
		
		final List<VisualStyle> vsList = new ArrayList<VisualStyle>();
		VisualStyle style1 = mock(VisualStyle.class);
		vsList.add(style1);
		task.styles = new ListSingleSelection<VisualStyle>(vsList);
		task.styles.setSelectedValue(style1);
		task.run(tm);
		
		verify(style1, times(1)).apply(view);
	}

}
