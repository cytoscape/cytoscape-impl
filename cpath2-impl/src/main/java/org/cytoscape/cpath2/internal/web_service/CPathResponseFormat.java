package org.cytoscape.cpath2.internal.web_service;

/**
 * CPath Response Format.
 */
public class CPathResponseFormat {
    private String format;

    /**
     * BioPAX Format.
     */
    public static CPathResponseFormat BIOPAX = new CPathResponseFormat ("biopax");

    /**
     * Simplified Binary SIF Format.
     */
    public static CPathResponseFormat BINARY_SIF = new CPathResponseFormat ("binary_sif");

    /**
     * Generic XML Format.
     */
    public static CPathResponseFormat GENERIC_XML = new CPathResponseFormat ("xml");

    /**
     * Gets the Proper cPath Response Format.
     * @param format    Format String.
     * @return          CPathResponseFormat Object.
     */
    public static CPathResponseFormat getResponseFormat (String format) {
        if (format.equals(BIOPAX.getFormatString())) {
            return BIOPAX;
        } else if (format.equals(BINARY_SIF.getFormatString())) {
            return BINARY_SIF;
        } else if (format.equals(GENERIC_XML.getFormatString())) {
            return GENERIC_XML;
        }
        else {
            throw new IllegalArgumentException ("Format:  " + format + " is not valid.");
        }
    }

    /**
     * Private Constructor.
     * @param format    Format String.
     */
    private CPathResponseFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the format string.
     * @return format string.
     */
    public String getFormatString() {
        return this.format;
    }
}
