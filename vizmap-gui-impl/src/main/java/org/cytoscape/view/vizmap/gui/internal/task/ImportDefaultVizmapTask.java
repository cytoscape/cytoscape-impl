package org.cytoscape.view.vizmap.gui.internal.task;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDefaultVizmapTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ImportDefaultVizmapTask.class);
	private static final String PRESET_VIZMAP_FILE = "default_vizmap.xml";

	private final VisualMappingManager vmm;
	private final VizmapReaderManager vizmapReaderMgr;

	private final File vizmapFile;
	private final CyEventHelper eventHelper;

	public ImportDefaultVizmapTask(final VizmapReaderManager vizmapReaderMgr, final VisualMappingManager vmm,
			final CyApplicationConfiguration config, final CyEventHelper eventHelper) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.eventHelper = eventHelper;
		this.vizmapFile = new File(config.getSettingLocation(), PRESET_VIZMAP_FILE);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final VizmapReader reader;

		if (vizmapFile.exists() == false) {
			// get the file from resource
			final URL url = this.getClass().getClassLoader().getResource(PRESET_VIZMAP_FILE);
			reader = vizmapReaderMgr.getReader(url.toURI(), url.getPath());
		} else {
			reader = vizmapReaderMgr.getReader(vizmapFile.toURI(), vizmapFile.getName());
		}

		logger.debug("Default vizmap file = " + vizmapFile.getName());

		if (reader == null)
			throw new NullPointerException("Failed to find Default Vizmap loader.");

		insertTasksAfterCurrentTask(reader, new AddVisualStylesTask(reader, vmm, eventHelper));
	}

	private static final class AddVisualStylesTask extends AbstractTask {

		private final VizmapReader reader;
		private final VisualMappingManager vmMgr;
		private final CyEventHelper helper;

		public AddVisualStylesTask(VizmapReader reader, VisualMappingManager vmMgr, final CyEventHelper eventHelper) {
			this.reader = reader;
			this.vmMgr = vmMgr;
			this.helper = eventHelper;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("Loading preset Visual Styles...");
			final Set<VisualStyle> styles = reader.getVisualStyles();

			if (styles != null) {
				int count = 1;
				int total = styles.size();

				for (VisualStyle vs : styles) {
					if (cancelled)
						break;
					taskMonitor.setStatusMessage(count + " of " + total + ": " + vs.getTitle());
					vmMgr.addVisualStyle(vs);
					taskMonitor.setProgress(count / total);
					count++;
				}

				if (cancelled) {
					// remove recently added styles
					for (VisualStyle vs : styles)
						vmMgr.removeVisualStyle(vs);

					taskMonitor.setProgress(1.0);
				}

				final VisualStyle defStyle = vmMgr.getDefaultVisualStyle();
				helper.fireEvent(new SelectedVisualStyleSwitchedEvent(this, null, defStyle));
			}
		}
	}
}
