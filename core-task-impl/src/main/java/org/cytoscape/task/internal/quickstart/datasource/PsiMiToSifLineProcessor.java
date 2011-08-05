package org.cytoscape.task.internal.quickstart.datasource;

public class PsiMiToSifLineProcessor implements LineProcessor {

	private static final String TAB = "\t";
	private static final String SEPARATOR = "\\|";
	private static final int COLUMN_COUNT = 15;
	
	public String processLine(final String line) {
		final String[] entry = line.split(TAB);
		
		// Validate entry list.
		if (entry == null || entry.length < COLUMN_COUNT)
			return null;

		String[] sourceID = entry[0].split(SEPARATOR);
		String[] targetID = entry[1].split(SEPARATOR);
		final String sourceRawID = sourceID[0].split(":")[1];
		final String targetRawID = targetID[0].split(":")[1];
		
		final String[] interactionID = entry[13].split(SEPARATOR);
		final String interaction = interactionID[0];
		if(interaction == null || interaction.length() == 0)
			return null;
		
		return sourceRawID + " " + interaction + " " + targetRawID;
	}
}
