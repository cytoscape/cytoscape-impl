/**
 *
 */
package org.cytoscape.app.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginVersionUtils {

  private static final Logger logger = LoggerFactory.getLogger(PluginVersionUtils.class);
  
  public static final String versionMatch = "^\\d+\\.\\d+";
  public static final String versionSplit = "\\.";
  public static final int MAJOR = 1;
  public static final int MINOR = 2;
  public static final int BUGFIX = 3;

  public static boolean isVersion(String vers, int vt) {
    String[] version = vers.split("\\.");
    if (version.length == 2 && version[1].equals("0"))
      version = new String[]{version[0]};

    return vt == version.length;
  }

  /**
	 * Return the newer of the two versions.
	 *
	 * @param arg0
	 * @param arg1
	 * @return
	 */
	public static String getNewerVersion(String arg0, String arg1) {
		String MostRecentVersion = null;
		int max = 3;

		String[] SplitVersionA = arg0.split(versionSplit);
		String[] SplitVersionB = arg1.split(versionSplit);

		for (int i = 0; i < max; i++) {
			int a = 0;
			int b = 0;

			if (i == (max - 1)) {
				logger.debug("A length: " + SplitVersionA.length + " B length: " + SplitVersionB.length);
				a = (SplitVersionA.length == max) ? Integer
						.valueOf(SplitVersionA[i]) : 0;
				b = (SplitVersionB.length == max) ? Integer
						.valueOf(SplitVersionB[i]) : 0;
			} else {
				a = Integer.valueOf(SplitVersionA[i]);
				b = Integer.valueOf(SplitVersionB[i]);
			}

			if (a != b) {
				MostRecentVersion = (a > b) ? arg0 : arg1;
				break;
			}
		}
    return MostRecentVersion;
	}
  

  // this just checks the downloadable object version and the cytoscape version
  protected static boolean versionOk(String version, boolean downloadObj) {
	  // TODO: do not check the downloadObj for now.
	  if (downloadObj){
		  return true;
	  }
	  
	  
    // \d+.\+d ok
    String Match = versionMatch;
    String Split = versionSplit;

    if (downloadObj) {
      Match = Match + "$";
    } else { // cytoscape version
      Match = Match + "(\\.\\d+)?$";
      Split = "\\.|-";
    }

    if (!version.matches(Match)) {
      return false;
    }

    String[] SplitVersion = version.split(Split);

    int max = 2;
    if (!downloadObj) {
      max = 3; // cytoscape version numbers
      // if there's a fourth is must be alpha
      if (SplitVersion.length == 4) {
        if (!SplitVersion[3].matches("[a-z]+")) {
          return false;
        }
      }
    }

    // can't be longer than the accepted version types
    if (SplitVersion.length > max) {
      return false;
    }

    // must be digits
    for (int i = 0; i < max && i < SplitVersion.length; i++) {
      if (!SplitVersion[i].matches("\\d+")) {
        return false;
      }
    }
    return true;
  }


}
