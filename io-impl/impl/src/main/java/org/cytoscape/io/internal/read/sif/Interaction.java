package org.cytoscape.io.internal.read.sif;

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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class for representing one line of SIF file.
 */
final class Interaction {
	private String source;
	private List<String> targets;
	private String interactionType;

	Interaction(final String rawText, final String delimiter) {
		final StringTokenizer strtok = new StringTokenizer(rawText, delimiter);
		int counter = 0;
		targets = new ArrayList<>();

		while (strtok.hasMoreTokens()) {
			if (counter == 0)
				source = strtok.nextToken().trim();
			else if (counter == 1)
				interactionType = strtok.nextToken().trim();
			else
				targets.add(strtok.nextToken().trim());

			counter++;
		}
	}

	/**
	 * @return The source node identifier string.
	 */
	final String getSource() {
		return source;
	}

	/**
	 * @return The interaction type string.
	 */
	String getType() {
		return interactionType;
	}

	/**
	 * @return The array of target node identifier strings.
	 */
	List<String> getTargets() {
		return targets;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(interactionType);
		sb.append("::");
		sb.append(source);
		sb.append("::");

		final int targetSize = targets.size();
		for (int i = 0; i < targetSize; i++) {
			sb.append(targets.get(i));

			if (i < (targetSize - 1))
				sb.append(",");
		}

		return sb.toString();
	}
}
