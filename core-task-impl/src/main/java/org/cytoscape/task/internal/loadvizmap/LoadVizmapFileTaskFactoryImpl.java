package org.cytoscape.task.internal.loadvizmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadVizmapFileTaskFactoryImpl extends AbstractTaskFactory implements LoadVizmapFileTaskFactory {

	private static final Logger logger = LoggerFactory.getLogger(LoadVizmapFileTaskFactoryImpl.class);

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmMgr;
	private final SynchronousTaskManager<?> syncTaskManager;

	private LoadVizmapFileTask task;

	private final TunableSetter tunableSetter;

	public LoadVizmapFileTaskFactoryImpl(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmMgr,
			SynchronousTaskManager<?> syncTaskManager, TunableSetter tunableSetter) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmMgr = vmMgr;
		this.syncTaskManager = syncTaskManager;
		this.tunableSetter = tunableSetter;
	}

	@Override
	public TaskIterator createTaskIterator() {
		task = new LoadVizmapFileTask(vizmapReaderMgr, vmMgr);
		return new TaskIterator(2, task);
	}

	public Set<VisualStyle> loadStyles(File f) {
		// Set up map containing values to be assigned to tunables.
		// The name "file" is the name of the tunable field in
		// LoadVizmapFileTask.
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", f);

		syncTaskManager.setExecutionContext(m);
		syncTaskManager.execute(createTaskIterator());

		return task.getStyles();
	}

	public Set<VisualStyle> loadStyles(InputStream is) {
		// Save the contents of inputStream in a tmp file
		File f = this.getFileFromStream(is);
		return this.loadStyles(f);
	}

	@Override
	public TaskIterator createTaskIterator(File file) {

		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("file", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m);
	}

	// Read the inputStream and save the content in a tmp file
	private File getFileFromStream(InputStream is) {

		File returnFile = null;

		// Get the contents from inputStream
		ArrayList<String> list = new ArrayList<String>();

		BufferedReader bf = null;
		String line;

		try {
			bf = new BufferedReader(new InputStreamReader(is));
			while (null != (line = bf.readLine())) {
				list.add(line);
			}
		} catch (IOException e) {
			logger.error("Could not read the VizMap file.", e);
		} finally {
			try {
				if (bf != null)
					bf.close();
			} catch (IOException e) {
				logger.error("Could not Close the stream.", e);
				bf = null;
			}
		}

		if (list.size() == 0)
			return null;

		// Save the content to a tmp file
		Writer output = null;
		try {
			returnFile = File.createTempFile("visualStyles", ".props", new File(System.getProperty("java.io.tmpdir")));
			returnFile.deleteOnExit();

			// use buffering
			output = new BufferedWriter(new FileWriter(returnFile));
			// FileWriter always assumes default encoding is OK!
			for (int i = 0; i < list.size(); i++) {
				output.write(list.get(i) + "\n");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
					output = null;
				} catch (IOException e) {
					logger.error("Could not close stream.", e);
					output = null;
				}

			}
		}
		return returnFile;
	}
}
