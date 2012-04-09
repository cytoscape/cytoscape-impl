package org.cytoscape.task.internal.session;


import org.cytoscape.session.CySession;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import java.io.File;
import java.util.List;


/**
 * A utility Task implementation that writes a {@link org.cytoscape.session.CySession} to a file.
 * @CyAPI.Final.Class
 */
public final class CySessionWriter extends AbstractTask implements CyWriter {
	private final CySession session; 
	private final CySessionWriterManager writerMgr; 
	private File outputFile; 

	/**
	 * Constructs this CySessionWriter.
	 * @param writerMgr The {@link org.cytoscape.io.write.CySessionWriterManager} contains single expected
	 * {@link org.cytoscape.io.write.CySessionWriterFactory} to use to write the file.
	 * @param session The {@link org.cytoscape.session.CySession} to be written out. 
	 * @param outputFile The file the {@link org.cytoscape.session.CySession} should be written to.
 	 */
	public CySessionWriter(CySessionWriterManager writerMgr, CySession session, File outputFile) {
		if ( writerMgr == null )
			throw new NullPointerException("Writer Manager is null");
		this.writerMgr = writerMgr;

		if ( session == null )
			throw new NullPointerException("Session Manager is null");
		this.session = session;

		if ( outputFile == null )
			throw new NullPointerException("Output File is null");
		this.outputFile = outputFile;
	}

	/**
	 * The method that will actually write the specified session to the specified
	 * file.
	 * @param tm The {@link org.cytoscape.work.TaskMonitor} provided by the TaskManager execution environment.
	 */
	public final void run(TaskMonitor tm) throws Exception {
		List<CyFileFilter> filters = writerMgr.getAvailableWriterFilters();
		if ( filters == null || filters.size() < 1)
			throw new NullPointerException("No Session file filters found");
		if ( filters.size() > 1 )
			throw new IllegalArgumentException("Found too many session filters!");

		if (!HasFileExtension(outputFile.getName()))
			outputFile = new File(outputFile.getPath() + ".cys");

		CyWriter writer = writerMgr.getWriter(session,filters.get(0),outputFile); 
		if ( writer == null )
			throw new NullPointerException("No CyWriter found for specified file type!");

		insertTasksAfterCurrentTask( writer );
	}

	private static boolean HasFileExtension(final String pathName) {
		final int lastDotPos = pathName.lastIndexOf('.');
		final int lastSlashPos = pathName.lastIndexOf(File.separatorChar);
		return lastSlashPos < lastDotPos; // Yes, this also works if one or both of lastSlashPos and lastDotPos are -1!
	}
}
