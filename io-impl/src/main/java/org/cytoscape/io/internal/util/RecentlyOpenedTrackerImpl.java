package org.cytoscape.io.internal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;


public class RecentlyOpenedTrackerImpl implements RecentlyOpenedTracker {
	
	private static final int MAX_TRACK_COUNT = 5;
	private static final String USER_HOME_DIR = System.getProperty("user.home");
	
	private final String trackerFileName;
	private final LinkedList<URL> trackerURLs;
	
	private final File propDir;
	
	/**
	 * Creates a "recently opened" file tracker.
	 * 
	 * @param trackerFileName
	 *            the name of the file in the Cytoscape config directory to read
	 *            saved file names from.
	 */
	public RecentlyOpenedTrackerImpl(final String trackerFileName, final CyApplicationConfiguration config) throws IOException {
		this.trackerFileName = trackerFileName;
		this.propDir = config.getSettingLocation();
		trackerURLs = new LinkedList<URL>();

		final File input = new File(propDir, trackerFileName);
		if (!input.exists())
			input.createNewFile();

		final BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while ((line = reader.readLine()) != null && trackerURLs.size() < MAX_TRACK_COUNT) {
			final String newURL = line.trim();
			if (newURL.length() > 0)
				trackerURLs.addLast(new URL(newURL));
		}
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.task.internal.session.RecentlyOpenedTracker#getRecentlyOpenedURLs()
	 */
	@Override
	public synchronized List<URL> getRecentlyOpenedURLs() {
		return Collections.unmodifiableList(trackerURLs);
	}


	/* (non-Javadoc)
	 * @see org.cytoscape.task.internal.session.RecentlyOpenedTracker#add(java.net.URL)
	 */
	@Override
	public synchronized void add(final URL newURL) {
		trackerURLs.remove(newURL);
		if (trackerURLs.size() == MAX_TRACK_COUNT)
			trackerURLs.removeLast();
		trackerURLs.addFirst(newURL);
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.task.internal.session.RecentlyOpenedTracker#writeOut()
	 */
	@Override
	public void writeOut() throws FileNotFoundException {
		final PrintWriter writer = new PrintWriter(new File(propDir, trackerFileName));
		for (final URL trackerURL : trackerURLs)
			writer.println(trackerURL.toString());
		writer.close();
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.task.internal.session.RecentlyOpenedTracker#getMostRecentAddition()
	 */
	@Override
	public synchronized URL getMostRecentlyOpenedURL() {
		if (trackerURLs.isEmpty())
			return null;
		else
			return trackerURLs.getFirst();
	}
}
