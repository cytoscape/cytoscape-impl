/*
 File: ZipMultipleFiles.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.app.internal.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compression-related methods mainly for Session Writer.<br>
 * The created zip files can be decompressed by any zip utilities.
 *
 * @version 0.9
 * @since Cytoscape 2.3
 * @see cytoscape.data.writers.CytoscapeSessionWriter
 * @author kono
 *
 */
public class ZipUtil {
	/*
	 * Default Compression Level. Range is 0-9. Basically, 0 is no compression,
	 * and 9 will make the most space-efficeint zip file. However, it takes
	 * long!
	 */

	/**
	 *
	 */
	public static final int DEF_COMPRESSION_LEVEL = 1;
	private String zipArchiveName;
	private String[] inputFiles;
	private String inputFileDir;
	private int fileCount;
	private String sessionDirName;
	private HashMap appFileMap = null;

	/**
	 * For zip file, file separator is always "/" in all platforms inclding Win,
	 * Mac, and Unix.
	 */
	private static final String FS = "/";

	/**
	 * Constructor.<br>
	 *
	 * @param zipFile
	 *            Output zip file name.
	 * @param fileNameList
	 *            List of file names to be compressed.
	 * @param sessionDir
	 *            Root dir created in the zip archive.
	 *
	 */
	public ZipUtil(final String zipFile, final String[] fileNameList, final String sessionDir) {
		this(zipFile, fileNameList, sessionDir, "");
	}

	/**
	 * Constructor.<br>
	 *
	 * @param zipFile
	 *            Output zip file name.
	 * @param fileNameList
	 *            List of file names to be compressed.
	 * @param sessionDir
	 *            Root dir created in the zip archive.
	 * @param fileDir
	 *            root directory of files in fileList.
	 *
	 */
	public ZipUtil(final String zipFile, final String[] fileNameList, final String sessionDir,
	               final String fileDir) {
		this.zipArchiveName = zipFile;
		this.fileCount = fileNameList.length;
		this.inputFiles = new String[fileCount];
		this.sessionDirName = sessionDir;
		this.inputFileDir = fileDir;

		System.arraycopy(fileNameList, 0, inputFiles, 0, fileCount);
	}

	/**
	 * Delete input files.
	 *
	 */
	private void clean() {
		for (int i = 0; i < fileCount; i++) {
			final File tempFile = new File(inputFileDir + inputFiles[i]);
			tempFile.delete();
		}
	}

	/**
	 * Faster version of compression method.<br>
	 *
	 * @param compressionLevel
	 *            Level of compression. Range = 0-9. 0 is no-compression, and 9
	 *            is most space-efficeint. However, 9 is slow.
	 * @param cleanFlag
	 *            If true, remove all imput files.
	 * @throws IOException
	 */
	public void compressFast(final int compressionLevel, final boolean cleanFlag)
	    throws IOException {
		// For time measurement
		// final double start = System.currentTimeMillis();

		// FileInputStream fileIS;
		final CRC32 crc32 = new CRC32();
		final byte[] rgb = new byte[5000];
		BufferedOutputStream bos = null;

		try {
			ZipOutputStream zipOS = null;
			
			bos = new BufferedOutputStream(new FileOutputStream(zipArchiveName));
			try {
				zipOS = new ZipOutputStream(bos);
				// Tuning performance
				zipOS.setMethod(ZipOutputStream.DEFLATED);

				if ((compressionLevel >= 0) && (compressionLevel <= 9)) {
					zipOS.setLevel(compressionLevel);
				} else {
					zipOS.setLevel(DEF_COMPRESSION_LEVEL);
				}

				String targetName = "";

				for (int i = 0; i < fileCount; i++) {
					final File file = new File(inputFileDir + inputFiles[i]);
					targetName = sessionDirName + FS + inputFiles[i];
					addEntryToZip(file, targetName, zipOS, crc32, rgb);
				}

				if ((appFileMap != null) && (appFileMap.size() > 0)) {
					Set<String> appSet = appFileMap.keySet();

					for (String appName : appSet) {
						List<File> theFileList = (List<File>) appFileMap.get(appName);

						if ((theFileList == null) || (theFileList.size() == 0))
							continue;

						for (File theFile : theFileList) {
							if ((theFile == null) || (!theFile.exists()))
								continue;

							targetName = sessionDirName + FS + "apps" + FS + appName + FS
										 + theFile.getName();
							addEntryToZip(theFile, targetName, zipOS, crc32, rgb);
						}
					}
				}
			}
			finally {
				if (zipOS != null) {
					zipOS.close();
				}
			}
		}
		finally {
			if (bos != null) {
				bos.close();
			}
		}

		// final double stop = System.currentTimeMillis();
		// final double diff = stop - start;
		// CyLogger.getLogger().info("Compression time 3 = " + diff / 1000 + " sec.");
		if (cleanFlag) {
			clean();
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param pMap DOCUMENT ME!
	 */
	public void setAppFileMap(HashMap pMap) {
		appFileMap = pMap;
	}

	private void addEntryToZip(File srcFile, String targetName, ZipOutputStream zipOS, CRC32 crc32,
	                           byte[] rgb) throws IOException {
		int numRead;

		// Set CRC
		FileInputStream fileIS = null;

        try {
			fileIS = new FileInputStream(srcFile);
            while ((numRead = fileIS.read(rgb)) > -1) {
                crc32.update(rgb, 0, numRead);
            }
        }
        finally {
            if (fileIS != null) {
                fileIS.close();
            }
        }

		fileIS = null;
        try {
			final ZipEntry zipEntry = new ZipEntry(targetName);
			zipEntry.setSize(srcFile.length());
			zipEntry.setTime(srcFile.lastModified());
			zipEntry.setCrc(crc32.getValue());
			zipOS.putNextEntry(zipEntry);

            // Write the file
            try {
				fileIS = new FileInputStream(srcFile);

                while ((numRead = fileIS.read(rgb)) > -1) {
                    zipOS.write(rgb, 0, numRead);
                }
            }
            finally {
                if (fileIS != null) {
                    fileIS.close();
                }
            }
        }
        finally {
            if (zipOS != null) {
                zipOS.closeEntry();
            }
        }
	}

	/**
	 * Reads a file contained within a zip file and returns an InputStream for the
	 * specific file you want from within the zip file.
	 *
	 * @param zipName
	 *            The name of the zip file to read.
	 * @param fileNameRegEx
	 *            A regular expression that identifies the file to be read. In
	 *            general this should just be the file name you're looking for.
	 *            If more than one file matches the regular expression, only the
	 *            first will be returned. If you're looking for a specific file
	 *            remeber to build your regular expression correctly. For
	 *            example, if you're looking for the file 'vizmap.props', make
	 *            your regular expression '.*vizmap.props' to accomodate any
	 *            clutter from the zip file.
	 * @return An InputStream of the zip entry identified by the regular
	 *         expression or null if nothing matches.
     * @throws IOexception
     * @deprecated
	 */
    @Deprecated
	public static InputStream readFile(String zipName, String fileNameRegEx)
	    throws IOException {
		final ZipFile sessionZipFile = new ZipFile(zipName);
        final Enumeration zipEntries = sessionZipFile.entries();

        try {
            while (zipEntries.hasMoreElements()) {
                final ZipEntry zent = (ZipEntry) zipEntries.nextElement();

                if (zent.getName().matches(fileNameRegEx)) {
                    return sessionZipFile.getInputStream(zent);
                }
            }
        }
        catch (Exception e) {
            // This is not an ideal way to address bug 1988 for this method,
            // as sessionZipFile must remain open for the stream on the entry
            // to be usable.
            // Therefore sessionZipFile can only be closed explicitly when an
            // exception has occured.
            // More importantly sessionZipFile cannot otherwise be closed and
            // so will remain open until the finalize is called on it.
            // This is the reason for deprecating this method for one that
            // allows the enclosing method to timely close the ZipFile object
            // and otherwise properly manage it's lifetime.
            if (sessionZipFile != null) {
                sessionZipFile.close();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else if (e instanceof IOException) {
                throw (IOException)e;
            }
            else {
                throw new RuntimeException(e);
            }
        }

		return null;
	}
	
	/**
	 * Reads a file contained within a zip file and returns an InputStream for the
	 * specific file you want from within the zip file.
	 *
	 * @param sessionZipFile
	 *            The ZipFile in which to look for an entry matching
     *            fileNameRegEx.
	 * @param fileNameRegEx
	 *            A regular expression that identifies the file to be read. In
	 *            general this should just be the file name you're looking for.
	 *            If more than one file matches the regular expression, only the
	 *            first will be returned. If you're looking for a specific file
	 *            remeber to build your regular expression correctly. For
	 *            example, if you're looking for the file 'vizmap.props', make
	 *            your regular expression '.*vizmap.props' to accomodate any
	 *            clutter from the zip file.
	 * @return An InputStream of the zip entry identified by the regular
	 *         expression or null if nothing matches.
     * @throws IOexception
	 */
	public static InputStream readFile(ZipFile sessionZipFile, String fileNameRegEx)
	    throws IOException {
		//final ZipFile sessionZipFile = new ZipFile(zipName);
        final Enumeration zipEntries = sessionZipFile.entries();

        while (zipEntries.hasMoreElements()) {
            final ZipEntry zent = (ZipEntry) zipEntries.nextElement();

            if (zent.getName().matches(fileNameRegEx)) {
                return sessionZipFile.getInputStream(zent);
            }
        }

		return null;
	}

	/**
	 * Reads zip file, returns a list of all entries that match the given 
	 * regular expression
	 * @param zipName
	 * @param fileNameRegEx
	 * @return
	 * @throws IOException
	 */
	public static List<ZipEntry> getAllFiles(String zipName, String fileNameRegEx) throws IOException {
		List<ZipEntry> Matching = new ArrayList<ZipEntry>();
        Pattern p;
		Matcher m;

        p = Pattern.compile(fileNameRegEx);
        m = p.matcher("");

		ZipFile Zip = null;
        try {
			Zip = new ZipFile(zipName);
            Enumeration Entries = Zip.entries();

            while (Entries.hasMoreElements()) {
                ZipEntry CurrentEntry = (ZipEntry) Entries.nextElement();
                m.reset(CurrentEntry.getName());
                if (m.matches()) {
                    Matching.add(CurrentEntry);
                }
            }
        }
        finally {
            if (Zip != null) {
                Zip.close();
            }
        }

        return Matching;
	}


	
	/**
	* Unzips the given zip file and returns a list of all files unzipped
	* @param is
	*   InputStream for a zip file
	*/
	public static List<String> unzip(String zipName, String unzipDir, TaskMonitor taskMonitor) throws java.io.IOException {
		ArrayList<String> UnzippedFiles = new ArrayList<String>();
		
		if (unzipDir != null) {
			unzipDir = unzipDir + File.separator;
		} else {
			unzipDir = "";
		}
		
		int maxCount = 0; // -1 if unknown
		int progressCount = 0;
		double percent = 0.0d;
		
		ZipFile Zip = null;
        try {
			Zip = new ZipFile(zipName);
            Enumeration Entries = Zip.entries();
            int BUFFER = 2048;

            while(Entries.hasMoreElements()) {
                ZipEntry CurrentEntry = (ZipEntry)Entries.nextElement();
                File ZipFile = new File(unzipDir + CurrentEntry.getName());
                if (!CurrentEntry.isDirectory()) {
                    if (ZipFile.getParent() != null) {
                        File ParentDirs = new File(ZipFile.getParent());

                        if (!ParentDirs.exists()) {
                            ParentDirs.mkdirs();
                        }
                    }
                } else { // entry is directory, create and move on
                    if (!ZipFile.exists()) {
                        ZipFile.mkdirs();
                    }
                    continue;
                }

                InputStream zis = null;
                try {
					zis = Zip.getInputStream(CurrentEntry);
                    maxCount = zis.available();

					BufferedOutputStream dest = null;

					try {
						dest = new BufferedOutputStream(new FileOutputStream(ZipFile), BUFFER);
						// write the files to the disk
						int count;
						byte[] data = new byte[BUFFER];

						while ((count = zis.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
							//  Report on Progress
							if (taskMonitor != null) {
								percent = ((double) progressCount / maxCount) * 100.0;
								if (maxCount == -1) { // file size unknown
									percent = -1;
								}

								//JTask jTask = (JTask) taskMonitor;
								// TODO erm...how?
								//if (jTask.haltRequested()) { //abort
								//	taskMonitor.setStatus("Canceling the unzip ...");
								//	taskMonitor.setPercentCompleted(100);
								//	break;
								//}
								taskMonitor.setProgress((int) percent);
							}
						}

						dest.flush();
					}
					finally {
						if (dest != null) {
							dest.close();
						}
					}
                }
                finally {
                    if (zis != null) {
                        zis.close();
                    }
                }

                UnzippedFiles.add(ZipFile.getAbsolutePath());
            }
        }
        finally {
            if (Zip != null) {
                Zip.close();
            }
        }

		return UnzippedFiles;
	}



}
