package org.cytoscape.biopax.internal.util;

/**
 * Indicates if Cytoscape Session is in Progress.
 */
public class CySessionUtil {
    private static boolean sessionReadingInProgress;

    /**
     * Is Session Reading in Progress?
     * @return true or false.
     */
    public static boolean isSessionReadingInProgress() {
        return sessionReadingInProgress;
    }

    /**
     * Sets if Session Reading is in Progress.
     * @param inProgress true or false.
     */
    public static void setSessionReadingInProgress(boolean inProgress) {
        sessionReadingInProgress = inProgress;
    }
}