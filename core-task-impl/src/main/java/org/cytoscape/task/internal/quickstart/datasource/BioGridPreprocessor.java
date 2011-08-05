package org.cytoscape.task.internal.quickstart.datasource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioGridPreprocessor extends AbstractFilePreprocessor {


	private static final String DB_NAME = "BioGRID";
	private static final String SOURCE_URL = "http://thebiogrid.org/downloads/archives/Release%20Archive/BIOGRID-3.1.74/BIOGRID-ORGANISM-3.1.74.mitab.zip";

	private URL sourceFileLocation;

	private boolean isLatest;

	public BioGridPreprocessor(final CyProperty<Properties> properties) {
		super(properties);
		
		this.isLatest = false;

		this.processor = new PsiMiToSifLineProcessor();
		try {
			sourceFileLocation = new URL(SOURCE_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void setSource(final URL sourceFileLocation) {
		this.sourceFileLocation = sourceFileLocation;
	}

	@Override
	public void processFile() throws IOException {

		boolean test = isUpToDate();
		if (!test) {
			processZipTextFile(sourceFileLocation);
		}
	}

	private boolean isUpToDate() throws IOException {
		final File[] files = this.dataFileDirectory.listFiles();
		boolean up2date = false;

		for (File file : files) {
			final String name = file.getName();
			final Pattern pattern = Pattern.compile("BIOGRID");
			final Matcher matcher = pattern.matcher(name);
			boolean test = matcher.find();
			if (test) {
				up2date = true;
				this.sourceMap.put(createFileName(name), file.toURI().toURL());
			}
		}
		return up2date;
	}


	@Override
	protected String createFileName(final String originalFileName) {
		final String sourceName = DB_NAME + ": " + originalFileName.split("-")[2] + " Interactome";
		return sourceName;
	}

	@Override
	public boolean isLatest() {
		// TODO Auto-generated method stub
		return true;
	}
}
