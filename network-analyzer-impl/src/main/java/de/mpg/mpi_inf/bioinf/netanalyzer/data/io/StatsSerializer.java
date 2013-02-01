package de.mpg.mpi_inf.bioinf.netanalyzer.data.io;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStats;

/**
 * Controller class providing methods for loading and saving <code>NetworkStats</code> instances.
 * 
 * @author Yassen Assenov
 */
public class StatsSerializer {

	/**
	 * Loads network parameters from file.
	 * 
	 * @param aFile
	 *            File containing network parameters.
	 * @return The loaded parameters encapsulated in a <code>NetworkStatistics</code> instance;
	 *         <code>null</code> if the file is not a valid network parameters file.
	 * @throws IOException
	 *             If I/O error occurs.
	 * @throws FileNotFoundException
	 *             If the specified file does not exist, is a directory rather than a regular file, or for
	 *             some other reason cannot be opened for reading.
	 */
	@SuppressWarnings("fallthrough")
	public static NetworkStats load(File aFile) throws IOException {
		NetworkStats stats = null;
		LineReader reader = null;
		try {
			reader = new Netstats2Reader(new FileReader(aFile));
			switch (getVersion(reader.readLine())) {
				case 1:
					reader = new Netstats1Reader((Netstats2Reader) reader);
					// Fall through to the next case:
				case 2:
					stats = new NetworkStats();
					stats.setTitle(reader.readLine());
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						if (SIMPLE_HEADER.equals(line)) {
							loadSimple(stats, reader);
						} else if (COMPLEX_HEADER.equals(line)) {
							loadComplex(stats, reader);
						}
					}
			}
		} catch (NumberFormatException ex) {
			stats = null;
		} finally {
			IOUtils.closeStream(reader);
		}
		return stats;
	}

	/**
	 * Saves the computed network parameters to file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aFileName
	 *            Name of file to be saved to.
	 * 
	 * @throws FileNotFoundException
	 *             If <code>aFileName</code> denotes a directory rather than a regular file, does not exist
	 *             but cannot be created, or cannot be opened for any other reason.
	 * @throws IOException
	 *             If I/O error occurs.
	 */
	public static void save(NetworkStats aStats, String aFileName) throws IOException {
		save(aStats, new FileWriter(aFileName));
	}

	/**
	 * Saves the computed network parameters to file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aFile
	 *            File to be saved to.
	 * 
	 * @throws FileNotFoundException
	 *             If <code>aFile</code> is a directory rather than a regular file, does not exist but cannot
	 *             be created, or cannot be opened for any other reason.
	 * @throws IOException
	 *             If I/O error occurs.
	 */
	public static void save(NetworkStats aStats, File aFile) throws IOException {
		save(aStats, new FileWriter(aFile));
	}

	/**
	 * Loads simple network parameters from file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aReader
	 *            Stream (open for reading) to load data from.
	 * 
	 * @throws IOException
	 *             If I/O error occurs, for example, if <code>aReader</code> could not be closed.
	 * @throws NumberFormatException
	 *             If the data read is invalid.
	 */
	private static void loadSimple(NetworkStats aStats, LineReader aReader) throws IOException,
			NumberFormatException {
		String line = aReader.readLine();
		int linesToRead = Integer.parseInt(line);
		for (int i = 0; i < linesToRead; ++i) {
			String[] data = aReader.readLine().split("\\s");
			if (!Messages.containsSimpleParam(data[0])) {
				throw new NumberFormatException();
			}
			String type = data[1];
			Object param = null;
			if ("Boolean".equals(type)) {
				param = new Boolean(data[2].equalsIgnoreCase("true"));
			} else if ("Double".equals(type)) {
				param = new Double(data[2]);
			} else if ("Integer".equals(type)) {
				param = new Integer(data[2]);
			} else if ("Long".equals(type)) {
				param = new Long(data[2]);
			} else if ("String".equals(type)) {
				param = data[2];
			} else {
				throw new NumberFormatException();
			}
			aStats.set(data[0], param);
		}
	}

	/**
	 * Loads complex network parameters from file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aReader
	 *            Stream (open for reading) to load data from.
	 * @throws IOException
	 *             If I/O error occurs, for example, if <code>aReader</code> is not open.
	 * @throws NumberFormatException
	 *             If the data read is invalid.
	 */
	private static void loadComplex(NetworkStats aStats, LineReader aReader) throws IOException {
		String line = aReader.readLine();
		final Object[] initParams = new Object[] { null, aReader };
		try {
			while (line != null) {
				String[] headerData = line.split("\\s");
				String paramID = headerData[0];
				String paramType = headerData[1];
				if (!paramType.equals(SettingsSerializer.getDefaultType(paramID))) {
					throw new NumberFormatException();
				}
				Class<?> type = Class.forName(getTypeName(headerData[1]));
				Constructor<?> con = type.getConstructor(ComplexParam.loadParams);
				initParams[0] = extractParams(headerData);
				aStats.set(paramID, con.newInstance(initParams));
				line = aReader.readLine();
			}
		} catch (SecurityException ex) {
			throw ex;
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			if (ex instanceof ArrayIndexOutOfBoundsException || ex instanceof ClassNotFoundException
					|| ex instanceof InvocationTargetException || ex instanceof NumberFormatException) {
				// Corrupt data
				throw new NumberFormatException();
			}
			// IllegalAccessException
			// IllegalArgumentException
			// InstantiationException
			// NoSuchMethodException
			throw new InnerException(ex);
		}
	}

	/**
	 * Saves all the computed network parameters to file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aWriter
	 *            Stream (open for writing) to save the parameters to.
	 * @throws IOException
	 *             If I/O error occurs, for example, if <code>aWriter</code> is not open.
	 */
	public static void save(NetworkStats aStats, FileWriter aWriter) throws IOException {
		aWriter.write(FILE_HEADERS[FILE_HEADERS.length - 1] + "\n");
		aWriter.write(aStats.getTitle() + "\n");
		saveSimple(aStats, aWriter);
		saveComplex(aStats, aWriter);
		aWriter.close();
	}

	/**
	 * Saves the computed simple network parameters to file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aWriter
	 *            Stream (open for writing) to save the parameters to.
	 * @throws IOException
	 *             If I/O error occurs, for example, if <code>aWriter</code> is not open.
	 */
	private static void saveSimple(NetworkStats aStats, FileWriter aWriter) throws IOException {
		String[] statNames = aStats.getComputedSimple();
		if (statNames.length == 0) {
			return;
		}
		aWriter.write(SIMPLE_HEADER + "\n" + statNames.length + "\n");
		for (int i = 0; i < statNames.length; ++i) {
			aWriter.write(statNames[i] + " ");
			Object value = aStats.get(statNames[i]);
			aWriter.write(value.getClass().getSimpleName() + " ");
			aWriter.write(value.toString() + "\n");
		}
	}

	/**
	 * Saves the computed complex network parameters to file.
	 * 
	 * @param aStats
	 *            Network parameters encapsulated in a <code>NetworkStats</code> instance.
	 * @param aWriter
	 *            Stream (open for writing) to save the parameters to.
	 * @throws IOException
	 *             If I/O error occurs, for example, if <code>aWriter</code> is not open.
	 */
	private static void saveComplex(NetworkStats aStats, FileWriter aWriter) throws IOException {
		String[] statNames = aStats.getComputedComplex();
		if (statNames.length == 0) {
			return;
		}
		aWriter.write(COMPLEX_HEADER + "\n");
		for (int i = 0; i < statNames.length; ++i) {
			final String name = statNames[i];
			final ComplexParam value = aStats.getComplex(name);
			aWriter.write(name + " " + value.getClass().getSimpleName() + " ");
			value.save(aWriter, true);
		}
	}

	/**
	 * Creates an array of type parameters given an array items as saved in a file.
	 * <p>
	 * In a network statistics file, the first line of a complex parameter looks like:<br/>
	 * <code>name type [typeparam]*</code><br/>
	 * This method does constructs an array that contains only the <code>typeparam</code>s given.
	 * </p>
	 * 
	 * @param aHeaderData
	 *            Complex parameter header data in the form of <code>String</code> array.
	 * @return Array of type parameters; an empty array if no type parameters are specified.
	 */
	private static String[] extractParams(String[] aHeaderData) {
		String[] params = new String[aHeaderData.length - 2];
		for (int i = 0; i < params.length; ++i) {
			params[i] = aHeaderData[i + 2];
		}
		return params;
	}

	/**
	 * Gets the full name of a specified type (class).
	 * 
	 * @param aShortName
	 *            short name for the type to get.
	 * @return Full name of the type, obtained by prepending the data package to it.
	 */
	private static String getTypeName(String aShortName) {
		String[] packageName = StatsSerializer.class.getName().split("\\.");
		StringBuffer type = new StringBuffer(64);
		for (int i = 0; i < packageName.length - 2; ++i) {
			type.append(packageName[i] + ".");
		}
		type.append(aShortName);
		return type.toString();
	}

	/**
	 * Gets the version of the netstats file by its header line.
	 * 
	 * @param aHeader
	 *            First line of the Netstats file. This string given must be <b>without</b> an end-of-line
	 *            delimiter.
	 * @return Version number of the Netstats file; <code>0</code> (zero) if the <code>String</code> passed is
	 *         not recognized as a Netstats header.
	 */
	private static int getVersion(String aHeader) {
		for (int i = 0; i < FILE_HEADERS.length; ++i) {
			if (FILE_HEADERS[i].equals(aHeader)) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * Array of acceptable header lines of a &quot;.netstats&quot; files.
	 * <p>
	 * Note that the elements of this array are strings that do <b>not</b> include the end-of-line delimiter.
	 * </p>
	 */
	private static final String[] FILE_HEADERS = new String[] {
			"#org.mpii.bio.networkanalyzer Network Analysis 1.0",
			"#de.mpg.mpi_inf.bioinf.netanalyzer Network Analysis 2.0" };

	/**
	 * Line in the file indicating simple parameters follow.
	 * <p>
	 * Note that this string does <b>not</b> include the end-of-line delimiter.
	 * </p>
	 */
	private static final String SIMPLE_HEADER = "#simple";

	/**
	 * Line in the file indicating complex parameters follow.
	 * <p>
	 * Note that this string does <b>not</b> include the end-of-line delimiter.
	 * </p>
	 */
	private static final String COMPLEX_HEADER = "#complex";
}
